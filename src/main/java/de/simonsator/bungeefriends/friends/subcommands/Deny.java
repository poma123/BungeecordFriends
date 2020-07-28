package de.simonsator.bungeefriends.friends.subcommands;

import de.simonsator.bungeefriends.api.friends.abstractcommands.RequestReactionsCommands;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;

import java.util.List;
import java.util.regex.Matcher;

import static de.simonsator.bungeefriends.main.Main.getInstance;
import static de.simonsator.bungeefriends.utilities.PatterCollection.PLAYER_PATTERN;

/**
 * The command deny
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Deny extends RequestReactionsCommands {

	public Deny(List<String> pCommands, int pPriority, String pHelp, String pPermission) {
		super(pCommands, pPriority, pHelp, pPermission);
	}

	@Override
	public void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		if (!isPlayerGiven(pPlayer, args))
			return;
		PAFPlayer playerQuery = PAFPlayerManager.getInstance().getPlayer(args[1]);
		if (hasNoRequest(pPlayer, playerQuery))
			return;
		pPlayer.denyRequest(playerQuery);
		pPlayer.sendMessage((PREFIX + PLAYER_PATTERN.matcher(getInstance()
				.getMessages().getString("Friends.Command.Deny.HasDenied")).replaceAll(Matcher.quoteReplacement(args[1]))));
	}

}
