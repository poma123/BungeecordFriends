package de.simonsator.partyandfriends.api;

import java.util.ArrayList;

import de.simonsator.partyandfriends.main.Main;
import de.simonsator.partyandfriends.utilities.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public abstract class TopCommand<T extends SubCommand> extends Command {
	protected ArrayList<T> subCommands = new ArrayList<>();

	@Override
	public void execute(CommandSender pCommandSender, String[] args) {
		if (!(pCommandSender instanceof ProxiedPlayer)) {
			if (Main.getInstance().getLanguage().equalsIgnoreCase("own")) {
				Main.getInstance().loadConfiguration();
				pCommandSender.sendMessage(
						new TextComponent(Main.getInstance().getFriendsPrefix() + "Config and MessagesYML reloaded!"));
			} else {
				Main.getInstance().loadConfiguration();
				pCommandSender
						.sendMessage(new TextComponent(Main.getInstance().getFriendsPrefix() + "Config reloaded"));
			}
			return;
		}
		onCommand((ProxiedPlayer) pCommandSender, args);
	}

	protected abstract void onCommand(ProxiedPlayer pPlayer, String args[]);

	protected TopCommand(String[] pCommandNames, String pPermission) {
		super(pCommandNames[0], pPermission, pCommandNames);
	}

	protected TopCommand(String pCommandName, String pPermission, String[] pAliases) {
		super(pCommandName, pPermission, pAliases);
	}

	public void addCommand(T pCommand) {
		subCommands.add(pCommand);
		sort(subCommands);
	}

	protected void sort(ArrayList<T> pToSort) {
		if (pToSort.size() > 1) {
			ArrayList<T> smaller = new ArrayList<>();
			ArrayList<T> bigger = new ArrayList<>();
			T piviot = pToSort.get(0);
			pToSort.remove(0);
			while (!pToSort.isEmpty()) {
				T actual = pToSort.get(0);
				if (actual.getPriority() < piviot.getPriority()) {
					smaller.add(actual);
				} else {
					bigger.add(actual);
				}
				pToSort.remove(0);
			}
			sort(smaller);
			sort(bigger);
			pToSort.addAll(smaller);
			pToSort.add(piviot);
			pToSort.addAll(bigger);
		}
	}
}