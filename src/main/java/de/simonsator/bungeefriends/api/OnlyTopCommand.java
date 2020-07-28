package de.simonsator.bungeefriends.api;

import net.md_5.bungee.api.event.TabCompleteEvent;

/**
 * @author Simonsator
 * @version 1.0.0 24.01.17
 */
public abstract class OnlyTopCommand extends TopCommand {
	/**
	 * @param pCommandNames The command name and the different aliases of this command.
	 *                      By these names the method can be called.
	 * @param pPermission   The permission which is needed to execute this command.
	 *                      If it is blank no permission is needed to execute this command.
	 * @param pPrefix       The prefix which should be used by all subcommands.
	 *                      The prefix gets returned by the method {@link #getPrefix()}.
	 */
	protected OnlyTopCommand(String[] pCommandNames, String pPermission, String pPrefix) {
		super(pCommandNames, pPermission, pPrefix);
	}

	@Override
	public void tabComplete(TabCompleteEvent pEvent) {
// Only in the extended version
	}

}
