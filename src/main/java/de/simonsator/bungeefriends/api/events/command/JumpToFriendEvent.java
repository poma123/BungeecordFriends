package de.simonsator.bungeefriends.api.events.command;

import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.friends.subcommands.Jump;

public class JumpToFriendEvent extends PAFSubCommandEvent<Jump> {
	private final OnlinePAFPlayer FRIEND_TO_JUMP_TO;

	public JumpToFriendEvent(OnlinePAFPlayer pExecutor, OnlinePAFPlayer pFriendToJumpTo, String[] args, Jump pCaller) {
		super(pExecutor, args, pCaller);
		FRIEND_TO_JUMP_TO = pFriendToJumpTo;
	}

	public OnlinePAFPlayer getFriendToJumpTo() {
		return FRIEND_TO_JUMP_TO;
	}
}
