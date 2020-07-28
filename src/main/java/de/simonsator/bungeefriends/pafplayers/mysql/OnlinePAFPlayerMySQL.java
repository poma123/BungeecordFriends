package de.simonsator.bungeefriends.pafplayers.mysql;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.api.events.PAFAccountCreateEvent;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.pafplayers.manager.PAFPlayerManagerMySQL;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.UUID;

public class OnlinePAFPlayerMySQL extends PAFPlayerMySQL implements OnlinePAFPlayer {
	private final ProxiedPlayer PLAYER;

	public OnlinePAFPlayerMySQL(int pID, ProxiedPlayer pPlayer) {
		super(pID);
		PLAYER = pPlayer;
	}

	@Override
	public void createEntry() {
		id = PAFPlayerManagerMySQL.getConnection().firstJoin(PLAYER);
		BukkitBungeeAdapter.getInstance().callEvent(new PAFAccountCreateEvent(this));
	}

	@Override
	public String getName() {
		return PLAYER.getName();
	}

	@Override
	public UUID getUniqueId() {
		return PLAYER.getUniqueId();
	}

	@Override
	public void connect(ServerInfo pInfo) {
		getServerConnector().connect(PLAYER, pInfo);
	}

	@Override
	public void sendMessage(TextComponent pTextComponent) {
		PLAYER.sendMessage(pTextComponent);
	}

	@Override
	public boolean teleportTo(OnlinePAFPlayer pPlayer) {
		ServerInfo serverInfo = pPlayer.getServer();
		if (serverInfo != null && !getServer().equals(serverInfo)) {
			connect(pPlayer.getServer());
			return true;
		}
		return false;
	}

	@Override
	public ServerInfo getServer() {
		return PLAYER.getServer() != null ? PLAYER.getServer().getInfo() : null;
	}

	@Override
	public boolean isOnline() {
		return true;
	}

	@Override
	public ProxiedPlayer getPlayer() {
		return PLAYER;
	}

	@Override
	public int changeSettingsWorth(int pSettingsID) {
		return PAFPlayerManagerMySQL.getConnection().changeSettingsWorth(PLAYER, pSettingsID);
	}

	@Override
	public void sendPacket(Chat chat) {
		PLAYER.unsafe().sendPacket(chat);
	}

	@Override
	public void sendPacket(TextComponent chat) {
		sendMessage(chat);
	}

	@Override
	public String getDisplayName() {
		return getDisplayNameProvider().getDisplayName(this);
	}

	@Override
	public void update() {
		if (BukkitBungeeAdapter.getInstance().isOnlineMode()) {
			PAFPlayerManagerMySQL.getConnection().updatePlayerName(getPlayerID(), PLAYER.getName());
		} else if (!PLAYER.getName().equals(PAFPlayerManagerMySQL.getConnection().getName(getPlayerID()))
				&& !PLAYER.getUniqueId().equals(PAFPlayerManagerMySQL.getConnection().getUUID(getPlayerID())))
			PAFPlayerManagerMySQL.getConnection().updateUUID(getPlayerID(), PLAYER.getUniqueId());
	}
}
