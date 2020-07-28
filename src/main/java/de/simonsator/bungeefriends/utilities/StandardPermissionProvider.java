package de.simonsator.bungeefriends.utilities;

import de.simonsator.bungeefriends.api.PermissionProvider;
import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Simonsator
 * @version 02.09.16.
 */
public class StandardPermissionProvider extends PermissionProvider {
	public StandardPermissionProvider() {
		super();
	}

	@Override
	public boolean hasPermission(PAFPlayer pPlayer, String pPermission) {
		if (pPlayer != null && pPlayer.isOnline()) {
			ProxiedPlayer player = ((OnlinePAFPlayer) pPlayer).getPlayer();
			return player != null && player.hasPermission(pPermission);
		}
		return false;
	}
}
