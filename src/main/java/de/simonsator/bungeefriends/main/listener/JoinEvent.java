package de.simonsator.bungeefriends.main.listener;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.api.events.OnlineStatusChangedMessageEvent;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.main.Main;
import de.simonsator.bungeefriends.utilities.PatterCollection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Listener for joins
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class JoinEvent implements Listener {
	private final boolean FRIEND_REQUEST_NOTIFICATION;
	private final int PLAYER_SPLIT_LENGTH = Main.getInstance().getMessages().getString("Friends.Command.List.PlayerSplit").length();

	public JoinEvent() {
		FRIEND_REQUEST_NOTIFICATION = Main.getInstance().getGeneralConfig().getBoolean("General.SendFriendRequestNotificationOnJoin");
	}

	public Exception verify() {
		try {
			ProxiedPlayer.class.getMethod("isConnected", (Class<?>[]) null);
		} catch (NoSuchMethodException | SecurityException e) {
			return e;
		}
		return null;
	}

	/**
	 * Will be execute if somebody logs in into server
	 *
	 * @param pEvent The pEvent
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostLogin(final PostLoginEvent pEvent) {
		if (pEvent.getPlayer().isConnected())
			BukkitBungeeAdapter.getInstance().runAsync(Main.getInstance(), () -> sbLoggedIn(pEvent));
	}

	private void sbLoggedIn(PostLoginEvent pEvent) {
		OnlinePAFPlayer player = PAFPlayerManager.getInstance().getPlayer(pEvent.getPlayer());
		if (!player.doesExist()) {
			player.createEntry();
			return;
		} else
			player.update();
		List<PAFPlayer> friends = player.getFriends();
		List<PAFPlayer> friendRequests = player.getRequests();
		if (friends.isEmpty() && friendRequests.isEmpty())
			return;
		boolean noFriends = friends.isEmpty();
		if (!friendRequests.isEmpty() && FRIEND_REQUEST_NOTIFICATION)
			deliverFriendRequests(player, friendRequests);
		if (player.getSettingsWorth(3) == 1)
			noFriends = true;
		if (!noFriends)
			sendNowOnline(player, friends);
	}

	private void deliverFriendRequests(OnlinePAFPlayer pPlayer, List<PAFPlayer> pFriendRequests) {
		StringBuilder content = new StringBuilder();
		for (PAFPlayer player : pFriendRequests) {
			content.append(Main.getInstance().getMessages().getString("Friends.General.RequestInfoOnJoinColor"));
			content.append(player.getDisplayName());
			content.append(Main.getInstance().getMessages().getString("Friends.General.RequestInfoOnJoinColorComma"));
			content.append(Main.getInstance().getMessages().getString("Friends.Command.List.PlayerSplit"));
		}
		pPlayer.sendMessage(PatterCollection.FRIEND_REQUEST_COUNT_PATTERN.matcher(PatterCollection.FRIEND_REQUEST_PATTERN.matcher(Friends.getInstance().getPrefix() + Main.getInstance()
				.getMessages().getString("Friends.General.RequestInfoOnJoin")).replaceAll(Matcher.quoteReplacement(content.substring(0, content.length() - PLAYER_SPLIT_LENGTH)))).
				replaceAll(Matcher.quoteReplacement(pFriendRequests.size() + "")));
	}

	private void sendNowOnline(OnlinePAFPlayer pPlayer, List<PAFPlayer> pFriends) {
		String message = Friends.getInstance().getPrefix()
				+ PatterCollection.PLAYER_PATTERN.matcher(Main.getInstance().getMessages().getString("Friends.General.PlayerIsNowOnline")).replaceAll(Matcher.quoteReplacement(pPlayer.getDisplayName()));
		OnlineStatusChangedMessageEvent event = new OnlineStatusChangedMessageEvent(pPlayer, message, pFriends);
		BukkitBungeeAdapter.getInstance().callEvent(event);
		if (!event.isCancelled())
			for (PAFPlayer friend : event.getFriends())
				friend.sendMessage((event.getMessage()));
	}
}
