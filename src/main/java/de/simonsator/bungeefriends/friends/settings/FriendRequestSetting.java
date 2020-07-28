package de.simonsator.bungeefriends.friends.settings;

import de.simonsator.bungeefriends.api.SimpleSetting;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.main.Main;

import java.util.List;

/**
 * @author Simonsator
 * @version 1.0.0 28.03.17
 */
public class FriendRequestSetting extends SimpleSetting {
	public FriendRequestSetting(List<String> pSettingNames, String pPermission, int pPriority) {
		super(pSettingNames, pPermission, pPriority);
	}

	@Override
	protected String getMessage(OnlinePAFPlayer pPlayer) {
		String identifier;
		if (pPlayer.getSettingsWorth(0) == 0) {
			identifier = "Friends.Command.Settings.FriendRequestSettingNobody";
		} else {
			identifier = "Friends.Command.Settings.FriendRequestSettingEveryone";
		}
		return Main.getInstance().getMessages().getString(identifier);
	}

	@Override
	public void changeSetting(OnlinePAFPlayer pPlayer, String[] pNewSettingState) {
		int worthNow = pPlayer.changeSettingsWorth(0);
		if (worthNow == 0) {
			pPlayer.sendMessage((Friends.getInstance().getPrefix() + Main.getInstance()
					.getMessages().getString("Friends.Command.Settings.NowYouAreNotGoneReceiveFriendRequests")));
		} else {

			pPlayer.sendMessage((Friends.getInstance().getPrefix() + Main.getInstance()
					.getMessages().getString("Friends.Command.Settings.NowYouAreGoneReceiveFriendRequests")));
		}
	}
}
