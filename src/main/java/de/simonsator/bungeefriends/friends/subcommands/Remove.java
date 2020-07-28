package de.simonsator.bungeefriends.friends.subcommands;

import de.simonsator.bungeefriends.api.friends.abstractcommands.FriendSubCommand;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.bungeefriends.main.Main;

import java.util.List;
import java.util.regex.Matcher;

import static de.simonsator.bungeefriends.utilities.PatterCollection.PLAYER_PATTERN;

/**
 * The command remove
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Remove extends FriendSubCommand {

	public Remove(List<String> pCommands, int pPriority, String pHelp, String pPermission) {
		super(pCommands, pPriority, pHelp, pPermission);
	}

	@Override
	public void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		if (!isPlayerGiven(pPlayer, args))
			return;
		PAFPlayer playerQuery = PAFPlayerManager.getInstance().getPlayer(args[1]);
		if (!isAFriendOf(pPlayer, playerQuery, args))
			return;
		pPlayer.removeFriend(playerQuery);
		pPlayer.sendMessage((PREFIX + PLAYER_PATTERN.matcher(Main.getInstance()
				.getMessages().getString("Friends.Command.Remove.Removed")).replaceAll(Matcher.quoteReplacement(playerQuery.getDisplayName()))));
		if (Main.getInstance().getGeneralConfig().getBoolean("Commands.Friends.SubCommands.Remove.UseFriendRemovedYouMessage")) {
			if (playerQuery.isOnline()) {
				playerQuery.sendMessage(PREFIX + Main.getInstance().getMessages().getString("Friends.Command.Remove.FriendRemovedYou").replace("[PLAYER]", pPlayer.getDisplayName()));
			}
		}
	}

}
