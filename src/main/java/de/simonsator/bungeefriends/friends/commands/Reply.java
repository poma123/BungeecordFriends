package de.simonsator.bungeefriends.friends.commands;

import de.simonsator.bungeefriends.api.OnlyTopCommand;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.main.Main;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.event.EventHandler;

/**
 * Will be executed on /r
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Reply extends OnlyTopCommand {
	public Reply(String[] aliases, String pPrefix) {
		super(aliases, Main.getInstance().getGeneralConfig().getString("Permissions.FriendPermission"), pPrefix);
	}

	@Override
	protected void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		if (!MSG.getInstance().messageGiven(pPlayer, args, 0))
			return;
		PAFPlayer queryPlayer = pPlayer.getLastPlayerWroteTo();
		if (!queryPlayer.doesExist()) {
			pPlayer.sendMessage((getPrefix()
					+ Main.getInstance().getMessages().getString("Friends.Command.MSG.NoOneEverWroteToYou")));
			return;
		}
		MSG.getInstance().send(pPlayer, args, queryPlayer, 0);
	}

	@EventHandler
	public void onTabComplete(TabCompleteEvent pEvent) {
		tabComplete(pEvent);
	}

}