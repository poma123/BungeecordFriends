package de.simonsator.bungeefriends.api;

import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;

/**
 * @author Simonsator
 * @version 1.0.0 07.05.17
 */
public interface TextReplacer {
	String onProecess(PAFPlayer pPlayer, String pMessage);
}
