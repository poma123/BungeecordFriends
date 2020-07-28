package de.simonsator.bungeefriends.api;

import de.simonsator.bungeefriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.bungeefriends.main.Main;
import de.simonsator.bungeefriends.utilities.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class AdminSubCommand extends SubCommand {

	protected AdminSubCommand(String[] pCommands, int pPriority, String pHelp, String pPrefix) {
		super(pCommands, pPriority, pHelp, pPrefix);
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	/**
	 * Ignored
	 *
	 * @param ignored  Ignored
	 * @param ignored2 Ignored
	 */
	@Override
	public void onCommand(OnlinePAFPlayer ignored, String[] ignored2) {
	}

	public void sendError(CommandSender pCommandSender, TextComponent pMessage) {
		pCommandSender.sendMessage(pMessage);
		pCommandSender.sendMessage(HELP);
	}

	public void sendError(CommandSender pCommandSender, String pIdentifier) {
		sendError(pCommandSender, new TextComponent(PREFIX + Main.getInstance().getMessages().getString(pIdentifier)));
	}

}
