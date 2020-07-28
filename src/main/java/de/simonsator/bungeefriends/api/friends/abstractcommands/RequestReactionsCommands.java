package de.simonsator.bungeefriends.api.friends.abstractcommands;

import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.main.Main;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.regex.Matcher;

import static de.simonsator.bungeefriends.utilities.PatterCollection.PLAYER_PATTERN;

public abstract class RequestReactionsCommands extends FriendSubCommand {
	protected final Matcher PLAYER_MATCHER = PLAYER_PATTERN.matcher(Main.getInstance().getMessages()
			.getString("Friends.Command.Accept.ErrorNoFriendShipInvitation"));

	protected RequestReactionsCommands(String[] pCommands, int pPriority, String pHelp) {
		super(pCommands, pPriority, pHelp);
	}

	protected RequestReactionsCommands(List<String> pCommands, int pPriority, String pHelp, String pPermission) {
		super(pCommands, pPriority, pHelp, pPermission);
	}

	protected boolean hasNoRequest(OnlinePAFPlayer pPlayer, PAFPlayer pQueryPlayer) {
		if ((!pPlayer.hasRequestFrom(pQueryPlayer))) {
			sendError(pPlayer, new TextComponent(Friends.getInstance().getPrefix() + PLAYER_MATCHER.replaceFirst(pQueryPlayer.getName())));
			return true;
		}
		return false;
	}

}
