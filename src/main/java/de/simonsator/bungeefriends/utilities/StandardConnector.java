package de.simonsator.bungeefriends.utilities;

import de.simonsator.bungeefriends.api.friends.ServerConnector;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StandardConnector implements ServerConnector {

	@Override
	public void connect(ProxiedPlayer pPlayer, ServerInfo pServer) {
		pPlayer.connect(pServer);
	}

}
