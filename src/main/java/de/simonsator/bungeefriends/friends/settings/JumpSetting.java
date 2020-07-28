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
public class JumpSetting extends SimpleSetting {
	public JumpSetting(List<String> pSettingNames, String pPermission, int pPriority) {
		super(pSettingNames, pPermission, pPriority);
	}

	@Override
	protected String getMessage(OnlinePAFPlayer pPlayer) {
		String identifier;
		if (pPlayer.getSettingsWorth(4) == 0) {
			identifier = "Friends.Command.Settings.CanJump";
		} else {
			identifier = "Friends.Command.Settings.CanNotJump";
		}
		return Main.getInstance().getMessages().getString(identifier);
	}

	@Override
	public void changeSetting(OnlinePAFPlayer pPlayer, String[] pArgs) {
		int worthNow = pPlayer.changeSettingsWorth(4);
		if (worthNow == 0) {
			pPlayer.sendMessage((Friends.getInstance().getPrefix() + Main.getInstance()
					.getMessages().getString("Friends.Command.Settings.NowYourFriendsCanJump")));
		} else {
			pPlayer.sendMessage((Friends.getInstance().getPrefix() + Main.getInstance()
					.getMessages().getString("Friends.Command.Settings.NowYourFriendsCanNotJump")));
		}
	}

}
