package de.simonsator.bungeefriends.communication.sql;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.communication.sql.cache.LocalPlayerCache;
import de.simonsator.bungeefriends.communication.sql.cache.NoCache;
import de.simonsator.bungeefriends.communication.sql.cache.PlayerCache;
import de.simonsator.bungeefriends.communication.sql.pool.PoolData;
import de.simonsator.bungeefriends.communication.sql.pool.PoolSQLCommunication;
import de.simonsator.bungeefriends.utilities.disable.Disabler;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Simonsator
 * @version 2.0.0
 */
public class MySQL extends PoolSQLCommunication {

	private final String TABLE_PREFIX;
	private PlayerCache cache;

	/**
	 * Connects to the MySQL server
	 *
	 * @param pMySQLData The MySQL data
	 */
	public MySQL(MySQLData pMySQLData, PoolData pPoolData, Object pIgnore) throws SQLException {
		super(pMySQLData, pPoolData);
		this.TABLE_PREFIX = pMySQLData.TABLE_PREFIX;
		importDatabase();
		if (pMySQLData.CACHE)
			cache = new LocalPlayerCache();
		else cache = new NoCache();
		Disabler.getInstance().registerDeactivated(this);
	}

	public UUID getUUID(int pPlayerID) {
		UUID uuid = cache.getUUID(pPlayerID);
		if (uuid != null)
			return uuid;
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select player_uuid, player_name from " + TABLE_PREFIX
					+ "players WHERE player_id='" + pPlayerID + "' LIMIT 1");
			if (rs.next()) {
				uuid = UUID.fromString(rs.getString("player_uuid"));
				cache.add(rs.getString("player_name"), uuid, pPlayerID);
				return uuid;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return null;
	}

	private void importDatabase() {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "players` ("
					+ "`player_id` INT(8) NOT NULL AUTO_INCREMENT, " + "`player_name` VARCHAR(16) NOT NULL, "
					+ "`player_uuid` CHAR(38) NOT NULL, PRIMARY KEY (`player_id`));");
			prepStmt.executeUpdate();
			prepStmt.close();
			prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX
					+ "last_player_wrote_to` (`player_id` INT(8) NOT NULL, `written_to_id` INT(8) NOT NULL);");
			prepStmt.executeUpdate();
			prepStmt.close();
			prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX
					+ "friend_assignment` (`friend1_id` INT(8) NOT NULL, `friend2_id` INT(8) NOT NULL);");
			prepStmt.executeUpdate();
			prepStmt.close();
			prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX
					+ "settings` (`player_id` INT(8) NOT NULL, " + "`settings_id` TINYINT(2) NOT NULL, "
					+ " `settings_worth` TINYINT(1) NOT NULL);");
			prepStmt.executeUpdate();
			prepStmt.close();
			prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX
					+ "friend_request_assignment` (`requester_id` INT(8) NOT NULL, "
					+ "`receiver_id` INT(8) NOT NULL);");
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
		addColumnLastOnline();
	}

	private void addColumnLastOnline() {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("ALTER TABLE `" + TABLE_PREFIX + "players`" +
					" ADD COLUMN `last_online` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `player_uuid`;");
			prepStmt.executeUpdate();
			prepStmt.close();
			fixLastOnline();
		} catch (SQLException ignored) {
		} finally {
			close(con, prepStmt);
		}
	}

	private void fixLastOnline() {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select player_id from " + TABLE_PREFIX
					+ "players ");
			while (rs.next())
				updateLastOnline(rs.getInt("player_id"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
	}

	public int getPlayerID(ProxiedPlayer pPlayer) {
		if (BukkitBungeeAdapter.getInstance().isOnlineMode())
			return getPlayerID(pPlayer.getUniqueId());
		return getPlayerID(pPlayer.getName());
	}

	/**
	 * Returns the ID of a player
	 *
	 * @param pUUID The UUID of the player
	 * @return Returns the ID of a player
	 */
	public int getPlayerID(UUID pUUID) {
		Integer playerID = cache.getPlayerID(pUUID);
		if (playerID != null)
			return playerID;
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select player_id, player_name from " + TABLE_PREFIX
					+ "players WHERE player_uuid='" + pUUID + "' LIMIT 1");
			if (rs.next()) {
				playerID = rs.getInt("player_id");
				cache.add(rs.getString("player_name"), pUUID, playerID);
				return playerID;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return -1;
	}

	/**
	 * Returns the ID of a player
	 *
	 * @param pPlayerName Name of the player Returns the ID of a player
	 * @return Returns the ID of a player
	 */
	public int getPlayerID(String pPlayerName) {
		Integer playerID = cache.getPlayerID(pPlayerName);
		if (playerID != null)
			return playerID;
		Connection con = getConnection();
		ResultSet rs = null;
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("select player_id, player_uuid from " + TABLE_PREFIX
					+ "players WHERE player_name=? LIMIT 1");
			prepStmt.setString(1, pPlayerName);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("player_uuid"));
				playerID = rs.getInt("player_id");
				cache.add(pPlayerName, uuid, playerID);
				return playerID;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, prepStmt);
		}
		return -1;
	}

	/**
	 * Will be executed if a player joins the first time on the server
	 *
	 * @param pPlayer The player
	 */
	public int firstJoin(ProxiedPlayer pPlayer) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		int id = 0;
		try {
			prepStmt = con.prepareStatement(
					"insert into  " + TABLE_PREFIX + "players values (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			prepStmt.setNull(1, 1);
			prepStmt.setString(2, pPlayer.getName());
			prepStmt.setString(3, pPlayer.getUniqueId().toString());
			prepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			prepStmt.executeUpdate();
			ResultSet rs = prepStmt.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
				cache.add(pPlayer.getName(), pPlayer.getUniqueId(), id);
				setStandardSettings(id);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
		return id;
	}

	private void setStandardSettings(int pPlayerID) {
		setSetting(pPlayerID, 0, 1);
	}

	/**
	 * Gives out the IDs of the friends of a player
	 *
	 * @param pPlayerID The ID of the player
	 * @return Returns the IDs of the friends of a player
	 */
	public List<Integer> getFriends(int pPlayerID) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		List<Integer> list = new LinkedList<>();
		try {
			rs = (stmt = con.createStatement()).executeQuery("select friend2_id, friend1_id from " + TABLE_PREFIX
					+ "friend_assignment WHERE friend1_id='" + pPlayerID + "' OR friend2_id='" + pPlayerID + "'");
			while (rs.next()) {
				int friend1 = rs.getInt("friend1_id");
				int friend2 = rs.getInt("friend2_id");
				if (friend1 == pPlayerID)
					list.add(friend2);
				else list.add(friend1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return list;
	}

	/**
	 * Returns the name of a player
	 *
	 * @param pPlayerID The ID of the player
	 * @return Returns the name of a player
	 */
	public String getName(int pPlayerID) {
		String playerName = cache.getName(pPlayerID);
		if (playerName != null)
			return playerName;
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select player_name, player_uuid from " + TABLE_PREFIX
					+ "players WHERE player_id='" + pPlayerID + "' LIMIT 1");
			if (rs.next()) {
				playerName = rs.getString("player_name");
				cache.add(playerName, UUID.fromString(rs.getString("player_uuid")), pPlayerID);
				return playerName;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return "";
	}

	/**
	 * Updates the name of a player
	 *
	 * @param pPlayerID      The ID of the player
	 * @param pNewPlayerName New name of the player
	 */
	public void updatePlayerName(int pPlayerID, String pNewPlayerName) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "players set player_name=? WHERE player_id='" + pPlayerID + "' LIMIT 1");
			prepStmt.setString(1, pNewPlayerName);
			prepStmt.executeUpdate();
			cache.updateName(pPlayerID, pNewPlayerName);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	public void updateUUID(int pPlayerID, UUID pNewUUID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "players set player_uuid='"
					+ pNewUUID + "' WHERE player_id='" + pPlayerID + "' LIMIT 1");
			prepStmt.executeUpdate();
			cache.updateUUID(pPlayerID, pNewUUID);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	public boolean hasRequestFrom(int pReceiver, int pRequester) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select requester_id from "
					+ TABLE_PREFIX + "friend_request_assignment WHERE receiver_id='" + pReceiver + "' AND requester_id='"
					+ pRequester + "' LIMIT 1");
			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return false;
	}

	/**
	 * Returns the IDs of the friends from a player
	 *
	 * @param pPlayerID The ID of the player
	 * @return Returns the IDs of the friends from a player
	 */
	public ArrayList<Integer> getRequests(int pPlayerID) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Integer> requests = new ArrayList<>();
		try {
			rs = (stmt = con.createStatement()).executeQuery("select requester_id from "
					+ TABLE_PREFIX + "friend_request_assignment WHERE receiver_id='" + pPlayerID + "'");
			while (rs.next())
				requests.add(rs.getInt("requester_id"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return requests;
	}

	/**
	 * Adds a player to friends
	 *
	 * @param pIDRequester The sender of the command
	 * @param pIDReceiver  The new friend
	 */
	public void addFriend(int pIDRequester, int pIDReceiver) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement(
					"insert into " + TABLE_PREFIX + "friend_assignment values (?, ?)");
			prepStmt.setInt(1, pIDRequester);
			prepStmt.setInt(2, pIDReceiver);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	/**
	 * Removes a friend request
	 *
	 * @param pReceiverSender The ID of the command executor
	 * @param pRequesterID    The ID of the person who had send the friend request
	 */
	public void denyRequest(int pReceiverSender, int pRequesterID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement(
					"DELETE FROM " + TABLE_PREFIX + "friend_request_assignment WHERE requester_id = '"
							+ pRequesterID + "' AND receiver_id='" + pReceiverSender + "' Limit 1");
			prepStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	/**
	 * Deletes a friend
	 *
	 * @param pFriend1ID The ID of the command sender
	 * @param pFriend2ID The ID of the friend, which should be deleted
	 */
	public void deleteFriend(int pFriend1ID, int pFriend2ID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX
					+ "friend_assignment WHERE (friend1_id = '" + pFriend1ID + "' AND friend2_id='" + pFriend2ID
					+ "') OR (friend1_id = '" + pFriend2ID + "' AND friend2_id='" + pFriend1ID + "') Limit 1");
			prepStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	/**
	 * Send a friend request
	 *
	 * @param pSenderID The ID of Sender of the friend request
	 * @param pQueryID  The ID of the player, which gets the friend request
	 */
	public void sendFriendRequest(int pSenderID, int pQueryID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement(
					"insert into  " + TABLE_PREFIX + "friend_request_assignment values (?, ?)");
			prepStmt.setInt(1, pSenderID);
			prepStmt.setInt(2, pQueryID);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	/**
	 * Sets a setting
	 *
	 * @param pPlayer     The player
	 * @param pSettingsID The ID of the setting
	 * @return Returns the new worth
	 */
	public int changeSettingsWorth(ProxiedPlayer pPlayer, int pSettingsID) {
		int playerID = getPlayerID(pPlayer);
		int worth = getSettingsWorth(playerID, pSettingsID);
		if (worth == 1)
			worth = 0;
		else
			worth = 1;
		setSetting(playerID, pSettingsID, worth);
		return worth;
	}

	/**
	 * Checks if somebody is a friend of someone others
	 *
	 * @param pPlayer1 The player
	 * @param pPlayer2 The other player
	 * @return Returns if player one is a friend of player two true, otherwise
	 * false
	 */
	@Deprecated
	public boolean isAFriendOf(ProxiedPlayer pPlayer1, ProxiedPlayer pPlayer2) {
		return isAFriendOf(getPlayerID(pPlayer1), getPlayerID(pPlayer2));
	}

	public int getSettingsWorth(int pPlayerID, int pSettingsID) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery(
					"select settings_worth from " + TABLE_PREFIX + "settings WHERE player_id='"
							+ pPlayerID + "' AND settings_id='" + pSettingsID + "' LIMIT 1");
			if (rs.next()) {
				return rs.getInt("settings_worth");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return 0;
	}

	public void setSetting(int pPlayerID, int pSettingsID, int pNewWorth) {
		removeSetting(pPlayerID, pSettingsID);
		if (pNewWorth != 0) {
			Connection con = getConnection();
			PreparedStatement prepStmt = null;
			try {
				prepStmt = con.prepareStatement(
						"insert into  " + TABLE_PREFIX + "settings values (?, ?, ?)");
				prepStmt.setInt(1, pPlayerID);
				prepStmt.setInt(2, pSettingsID);
				prepStmt.setInt(3, pNewWorth);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				close(con, prepStmt);
			}
		}
	}

	private void removeSetting(int pPlayerID, int pSettingsID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("DELETE FROM  " + TABLE_PREFIX
					+ "settings WHERE player_id = '" + pPlayerID + "' AND settings_id='" + pSettingsID + "' Limit 1");
			prepStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	public boolean isAFriendOf(int pPlayerID1, int pPlayerID2) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("Select friend1_id FROM " + TABLE_PREFIX
					+ "friend_assignment WHERE (friend1_id = '" + pPlayerID1 + "' AND friend2_id='" + pPlayerID2
					+ "') OR (friend1_id = '" + pPlayerID2 + "' AND friend2_id='" + pPlayerID1 + "') LIMIT 1");
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return false;
	}

	/**
	 * Returns the last player who wrote to the given player
	 *
	 * @param pID The ID of the player
	 * @return Returns the last player who wrote to the given player
	 */
	public int getLastPlayerWroteTo(int pID) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select written_to_id from "
					+ TABLE_PREFIX + "last_player_wrote_to WHERE player_id='" + pID + "' LIMIT 1");
			if (rs.next()) {
				return rs.getInt("written_to_id");
			} else {
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return 0;
	}

	/**
	 * @param pPlayerID    The ID of the player
	 * @param pLastWroteTo The ID of the player who wrote to
	 * @param pI           The pass
	 */
	public void setLastPlayerWroteTo(int pPlayerID, int pLastWroteTo, int pI) {
		removeLastPlayerWroteFrom(pPlayerID);
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement(
					"insert into  " + TABLE_PREFIX + "last_player_wrote_to values (?, ?)");
			prepStmt.setInt(1, pPlayerID);
			prepStmt.setInt(2, pLastWroteTo);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
		if (pI == 0)
			setLastPlayerWroteTo(pLastWroteTo, pPlayerID, 1);
	}

	private void removeLastPlayerWroteFrom(int pPlayerID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX
					+ "last_player_wrote_to WHERE player_id = '" + pPlayerID + "' Limit 1");
			prepStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	public Timestamp getLastOnline(int pPlayerID) {
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			rs = (stmt = con.createStatement()).executeQuery("select last_online from "
					+ TABLE_PREFIX + "players WHERE player_id='" + pPlayerID + "' LIMIT 1");
			if (rs.next())
				return rs.getTimestamp("last_online");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, rs, stmt);
		}
		return null;
	}

	public void updateLastOnline(int pPlayerID) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "players set last_online=now() WHERE player_id='" + pPlayerID + "' LIMIT 1");
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}

	public void deletePlayerEntry(int pPlayerId) {
		Connection con = getConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = con.prepareStatement(
					"DELETE FROM " + TABLE_PREFIX + "friend_request_assignment WHERE requester_id = '"
							+ pPlayerId + "' OR receiver_id='" + pPlayerId + "';");
			prepStmt.execute();
			prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX
					+ "friend_assignment WHERE friend1_id = '" + pPlayerId + "' OR friend2_id='" + pPlayerId + "';");
			prepStmt.execute();
			prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX + "settings WHERE player_id='" + pPlayerId + "';");
			prepStmt.execute();
			prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX + "last_player_wrote_to WHERE player_id='" + pPlayerId + "' OR written_to_id='" + pPlayerId + "';");
			prepStmt.execute();
			cache.deletePlayer(pPlayerId);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, prepStmt);
		}
	}
}
