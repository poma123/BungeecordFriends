package de.simonsator.bungeefriends.main.listener;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.api.events.OnlineStatusChangedMessageEvent;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.main.Main;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import static de.simonsator.bungeefriends.utilities.PatterCollection.PLAYER_PATTERN;

/**
 * The class with the ServerSwitchEvent
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class ServerSwitchListener implements Listener {
	private static ServerSwitchListener instance;
	/**
	 * The list of the servers which the party will not join.
	 */
	private final List<String> notJoinServers;

	/**
	 * Initials the object
	 */
	public ServerSwitchListener() {
		notJoinServers = Main.getInstance().getGeneralConfig().getStringList("General.PartyDoNotJoinTheseServers");
		instance = this;
	}

	public static ServerSwitchListener getInstance() {
		return instance;
	}

	/**
	 * Will be executed if a player switches the server
	 *
	 * @param pEvent The ServerSwitchEvent event
	 */
	@EventHandler
	public void onServerSwitch(final ServerSwitchEvent pEvent) {
		final UUID uuid = pEvent.getPlayer().getUniqueId();
		BukkitBungeeAdapter.getInstance().runAsync(Main.getInstance(), () -> playerSwitchedServer(pEvent, uuid));
	}

	/*private void moveParty(ServerSwitchEvent pEvent) {
		ServerInfo server = pEvent.getPlayer().getServer().getInfo();
		if (notJoinServers.contains(server.getName()))
			return;
		OnlinePAFPlayer player = PAFPlayerManager.getInstance().getPlayer(pEvent.getPlayer());
		PlayerParty party = PartyManager.getInstance().getParty(player);
		if (party != null && party.isLeader(player) && !party.getPlayers().isEmpty()) {
			for (OnlinePAFPlayer p : party.getPlayers())
				p.connect(server);
			party.sendMessage((PartyCommand.getInstance().getPrefix()
					+ Main.getInstance().getMessages().getString("Party.Command.General.ServerSwitched")
					.replace("[SERVER]", ServerDisplayNameCollection.getInstance().getServerDisplayName(server))));
		}
	}*/


	private void playerSwitchedServer(ServerSwitchEvent pEvent, UUID pUUID) {
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
