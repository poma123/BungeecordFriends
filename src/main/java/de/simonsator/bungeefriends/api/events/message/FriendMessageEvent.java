package de.simonsator.bungeefriends.api.events.message;

import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;

/**
 * @author simonbrungs
 * @version 1.0.0 17.11.16
 */
public class FriendMessageEvent extends SimpleMessageEvent {
	private final PAFPlayer RECEIVER;

	public FriendMessageEvent(PAFPlayer pReceiver, OnlinePAFPlayer sender, String message) {
		super(sender, message);
		RECEIVER = pReceiver;
	}

	public PAFPlayer getReceiver() {
		return RECEIVER;
	}
}
