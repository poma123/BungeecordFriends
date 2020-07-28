package de.simonsator.bungeefriends.main;

import de.simonsator.bungeefriends.admin.commands.PAFAdminCommand;
import de.simonsator.bungeefriends.api.PAFExtension;
import de.simonsator.bungeefriends.api.PAFPluginBase;
import de.simonsator.bungeefriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.bungeefriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.bungeefriends.communication.sql.MySQLData;
import de.simonsator.bungeefriends.communication.sql.pool.PoolData;
import de.simonsator.bungeefriends.friends.commands.Friends;
import de.simonsator.bungeefriends.friends.commands.MSG;
import de.simonsator.bungeefriends.friends.commands.Reply;
import de.simonsator.bungeefriends.main.listener.JoinEvent;
import de.simonsator.bungeefriends.main.listener.PlayerDisconnectListener;
import de.simonsator.bungeefriends.main.startup.error.BootErrorType;
import de.simonsator.bungeefriends.main.startup.error.ErrorReporter;
import de.simonsator.bungeefriends.pafplayers.manager.PAFPlayerManagerMySQL;
import de.simonsator.bungeefriends.pafplayers.mysql.PAFPlayerMySQL;
import de.simonsator.bungeefriends.utilities.*;
import de.simonsator.bungeefriends.utilities.disable.Disabler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/***
 * The main class
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Main extends PAFPluginBase implements ErrorReporter {
	/**
	 * The main instance of this plugin
	 */
	private static Main instance;
	/**
	 * The configuration
	 */
	private ConfigLoader config;
	/**
	 * The messages.yml
	 */
	private MessagesLoader messages = null;
	/**
	 * The party prefix
	 */
	private String partyPrefix;
	/**
	 * The language
	 */
	private Language language;
	private Friends friendCommand;
	private List<PAFExtension> pafExtensions = new ArrayList<>();

	public static Main getInstance() {
		return instance;
	}


	@Deprecated
	public static PAFPlayerManager getPlayerManager() {
		return PAFPlayerManager.getInstance();
	}

	public ConfigurationCreator getGeneralConfig() {
		return config;
	}

	/**
	 * Will be execute on enable
	 */
	@Override
	public void onEnable() {
		instance = (this);
		loadConfiguration();
		try {
			initPAFClasses();
			registerCommands();
			registerListeners();
			//new Metrics(this, 508);
			/*if (getConfig().getBoolean("General.CheckForUpdates")) {
				UpdateSearcher searcher = new UpdateSearcher("Party-and-Friends-Free", getDescription().getVersion());
				ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(searcher.checkForUpdate()));
			}*/
		} catch (SQLException e) {
			if (e.getMessage().contains("Unable to load authentication plugin 'caching_sha2_password'."))
				initError(e, BootErrorType.SHA_ENCRYPTED_PASSWORD);
			else
				initError(e, BootErrorType.MYSQL_CONNECTION_PROBLEM);
		}
	}

	private void initError(Exception e, BootErrorType pType) {
		if (!getGeneralConfig().getBoolean("Commands.Party.TopCommands.Party.Disabled"))
			registerCommand(new BootErrorCommand(
					(getGeneralConfig().getStringList("Commands.Party.TopCommands.Party.Names").toArray(new String[0])), pType));
		Command partyChatCommand = new BootErrorCommand(
				(getGeneralConfig().getStringList("Commands.Party.TopCommands.PartyChat.Names").toArray(new String[0])), pType);
		if (!getGeneralConfig().getBoolean("Commands.Party.TopCommands.PartyChat.Disabled"))
			registerCommand(partyChatCommand);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.Friend.Disabled"))
			registerCommand(new BootErrorCommand(getGeneralConfig().getStringList("Commands.Friends.TopCommands.Friend.Names").toArray(new String[0]), pType));
		BootErrorCommand msg = new BootErrorCommand(
				(getGeneralConfig().getStringList("Commands.Friends.TopCommands.MSG.Names").toArray(new String[0])), pType);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.MSG.Disabled"))
			registerCommand(msg);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.Reply.Disabled"))
			registerCommand(new BootErrorCommand(
					(getGeneralConfig().getStringList("Commands.Friends.TopCommands.Reply.Names").toArray(new String[0])), pType));
		CommandSender console = ProxyServer.getInstance().getConsole();
		reportError(console, pType);
		e.printStackTrace();
	}

	private void initPAFClasses() throws SQLException {
		PoolData poolData = new PoolData(Main.getInstance().getGeneralConfig().getInt("MySQL.Pool.MinPoolSize"),
				Main.getInstance().getGeneralConfig().getInt("MySQL.Pool.MaxPoolSize"),
				Main.getInstance().getGeneralConfig().getInt("MySQL.Pool.InitialPoolSize"), Main.getInstance().getGeneralConfig().getInt("MySQL.Pool.IdleConnectionTestPeriod"), Main.getInstance().getGeneralConfig().getBoolean("MySQL.Pool.TestConnectionOnCheckin"));
		MySQLData mySQLData = new MySQLData(getGeneralConfig().get("MySQL.Host").toString(),
				getGeneralConfig().get("MySQL.Username").toString(), getGeneralConfig().get("MySQL.Password").toString(),
				getGeneralConfig().getInt("MySQL.Port"), getGeneralConfig().get("MySQL.Database").toString(),
				getGeneralConfig().get("MySQL.TablePrefix").toString(), getGeneralConfig().getBoolean("MySQL.UseSSL"), getGeneralConfig().getBoolean("MySQL.Cache"));
		new PAFPlayerManagerMySQL(mySQLData, poolData);
		if (getGeneralConfig().getBoolean("General.MultiCoreEnhancement")) {
			PAFPlayerMySQL.setMultiCoreEnhancement(true);
			getProxy().getConsole().sendMessage(new TextComponent("Multi Core Enhancement is activated."));
		}
		new StandardPermissionProvider();
		new ServerDisplayNameCollection(getGeneralConfig());
	}

	@Override
	public void onDisable() {
		ProxyServer.getInstance().getPluginManager().unregisterListeners(this);
		ProxyServer.getInstance().getPluginManager().unregisterCommands(this);
		Disabler.getInstance().disableAll();
		getProxy().getScheduler().cancel(this);
	}

	/**
	 * Loads the configuration files(config.yml and messages.yml)
	 */
	private void loadConfiguration() {
		try {
			config = new ConfigLoader(new File(Main.getInstance().getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			language = Language.valueOf(getGeneralConfig().getString("General.Language").toUpperCase());
		} catch (IllegalArgumentException e) {
			getProxy().getConsole().sendMessage(new TextComponent("&4The given language is not supported by Party and Friends. English will be used instead."));
			language = Language.ENGLISH;
			e.printStackTrace();
		}
		try {
			messages = new MessagesLoader(language, getGeneralConfig().getBoolean("General.UseOwnLanguageFile"), new File(getDataFolder(), "messages.yml"), this);
			if (getGeneralConfig().getBoolean("General.UseOwnLanguageFile"))
				language = Language.OWN;
		} catch (IOException e) {
			e.printStackTrace();
		}
		partyPrefix = (getMessages().getString("Party.General.PartyPrefix"));
		System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
	}

	/**
	 * Registers the listeners
	 */
	private void registerListeners() {
		BukkitBungeeAdapter.getInstance().registerListener(new PlayerDisconnectListener(), this);
		//BukkitBungeeAdapter.getInstance().registerListener(new ServerSwitchListener(), this);
		JoinEvent joinEventListener;
		BukkitBungeeAdapter.getInstance().registerListener(joinEventListener = new JoinEvent(), this);
		Exception e = joinEventListener.verify();
		if (e != null)
			initError(e, BootErrorType.TOO_OLD_VERSION);
	}

	/**
	 * Registers the commands
	 */
	private void registerCommands() {
		String friendsPrefix = (getMessages().getString("Friends.General.Prefix"));
		friendCommand = new Friends(getGeneralConfig().getStringList("Commands.Friends.TopCommands.Friend.Names"), friendsPrefix);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.Friend.Disabled"))
			registerTopCommand(friendCommand);
		MSG friendsMSGCommand = new MSG(
				(getGeneralConfig().getStringList("Commands.Friends.TopCommands.MSG.Names").toArray(new String[0])), friendsPrefix);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.MSG.Disabled"))
			registerTopCommand(friendsMSGCommand);
		if (!getGeneralConfig().getBoolean("Commands.Friends.TopCommands.Reply.Disabled"))
			registerTopCommand(new Reply(
					(getGeneralConfig().getStringList("Commands.Friends.TopCommands.Reply.Names").toArray(new String[0])), friendsPrefix));
		if (getGeneralConfig().getBoolean("Commands.PAFAdmin.Enabled"))
			registerCommand(new PAFAdminCommand(getGeneralConfig().getStringList("Commands.PAFAdmin.Names").toArray(new String[0])));
	}


	@Deprecated
	public MSG getFriendsMSGCommand() {
		return MSG.getInstance();
	}

	@Deprecated
	public Friends getFriendsCommand() {
		return friendCommand;
	}

	/**
	 * @return Returns the normal Main config on Bungeecord, but on Spigot it returns the GUI config.
	 * For that reason it should not be used and instead getGeneralConfig() should be used.
	 */
	@Deprecated
	public Configuration getConfig() {
		return config.getCreatedConfiguration();
	}

	@Deprecated
	public String getFriendsPrefix() {
		return Friends.getInstance().getPrefix();
	}

	public Language getLanguage() {
		return language;
	}

	public LanguageConfiguration getMessages() {
		return messages;
	}

	@Deprecated
	public LanguageConfiguration getMessagesYml() {
		return getMessages();
	}


	public void registerExtension(PAFExtension pPAFExtension) {
		pafExtensions.add(pPAFExtension);
	}

	public void unregisterExtension(PAFExtension pPAFExtension) {
		pafExtensions.remove(pPAFExtension);
	}

	public void reload() {
		onDisable();
		onEnable();
		List<PAFExtension> toReload = new ArrayList<>(pafExtensions);
		pafExtensions.clear();
		for (PAFExtension extension : toReload)
			extension.reload();
	}
}
