package de.simonsator.bungeefriends.main.listener;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.api.events.OnlineStatusChangedMessageEvent;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.main.Main;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.regex.Matcher;

import static de.simonsator.bungeefriends.utilities.PatterCollection.PLAYER_PATTERN;

/**
 * The class with the PlayerDisconnectEvent event.
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class PlayerDisconnectListener implements Listener {

	/**
	 * Will be executed on player disconnect
	 *
	 * @param pEvent The disconnect event
	 */
	@EventHandler
	public void onPlayerDisconnect(final PlayerDisconnectEvent pEvent) {
		if (pEvent.getPlayer().getServer() == null)
			return;
		final UUID uuid = pEvent.getPlayer().getUniqueId();
		BukkitBungeeAdapter.getInstance().runAsync(Main.getInstance(), () -> playerDisconnected(pEvent, uuid));
	}

	private void playerDisconnected(PlayerDisconnectEvent pEvent, UUID pUUID) {
		PAFPlayer player = PAFPlayerManager.getInstance().getPlayer(pUUID);
		if (player.getSettingsWorth(3) != 1) {
			String message = Friends.getInstance().getPrefix() + PLAYER_PATTERN
					.matcher(Main.getInstance().getMessages()
							.getString("Friends.General.PlayerIsNowOffline"))
					.replaceAll(Matcher.quoteReplacement(player.getDisplayName()));
			OnlineStatusChangedMessageEvent event = new OnlineStatusChangedMessageEvent(player, message, player.getFriends());
			BukkitBungeeAdapter.getInstance().callEvent(event);
			if (!event.isCancelled())
				for (PAFPlayer friend : event.getFriends())
					friend.sendMessage((event.getMessage()));
			player.updateLastOnline();
		}
	}
}