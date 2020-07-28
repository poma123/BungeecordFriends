package de.simonsator.bungeefriends.api;

import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class PAFPluginBase extends Plugin {
	private BukkitBungeeAdapter adapter = new BukkitBungeeAdapter(this);

	public void registerCommand(Object pCommand) {
		ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command) pCommand);
	}

	public void registerTopCommand(TopCommand<?> pCommand) {
		ProxyServer.getInstance().getPluginManager().registerCommand(this, pCommand);
	}

	public BukkitBungeeAdapter getAdapter() {
		return adapter;
	}

}
