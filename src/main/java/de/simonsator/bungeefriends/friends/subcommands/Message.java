package de.simonsator.bungeefriends.friends.subcommands;

import de.simonsator.bungeefriends.api.friends.abstractcommands.FriendSubCommand;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.friends.commands.MSG;

import java.util.List;

/**
 * Will be executed on /friend msg
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Message extends FriendSubCommand {
	public Message(List<String> pCommands, int pPriority, String pHelp, String pPermission) {
		super(pCommands, pPriority, pHelp, pPermission);
	}

	@Override
	public void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		MSG.getInstance().send(pPlayer, args, 0);
	}
}
