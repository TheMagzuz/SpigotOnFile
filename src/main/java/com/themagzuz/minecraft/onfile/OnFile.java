package com.themagzuz.minecraft.onfile;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheMagzuz on 2019-06-23.
 *
 * @author TheMagzuz
 */
public class OnFile extends JavaPlugin
{
	private BukkitScheduler scheduler;

	private List<String> commands = new ArrayList<>();
	private String listenFile;
	private long checkInterval;

	private int checkTask;

	@Override
	public void onEnable() {
		File configFile = new File(this.getDataFolder(), "config.yml");

		if (!configFile.exists()) {
			this.saveDefaultConfig();
		}

		loadConfig();

		final OnFile pl = this;

		scheduler = getServer().getScheduler();

		startInterval();

		this.getCommand("onfile").setExecutor((CommandSender commandSender, Command command, String commandName, String[] arguments) -> {
				commandSender.sendMessage("Reloading OnFile config");
				pl.reloadConfig();
				scheduler.cancelTask(checkTask);
				loadConfig();
				startInterval();
				commandSender.sendMessage("OnFile config reloaded");
				return true;
		});

	}

	private void loadConfig() {
		FileConfiguration config = getConfig();
		commands = config.getStringList("commands");
		listenFile = config.getString("filename");
		checkInterval = config.getLong("checkInterval");
	}

	private void startInterval() {
		final OnFile pl = this;
		checkTask = scheduler.scheduleSyncRepeatingTask(this, () -> {
			final File f = new File(pl.getDataFolder(), listenFile);
			if (f.exists()) {
				boolean success = f.delete();
				pl.onFile();
				if (!success) {
					System.err.println("[OnFile] Unable to delete file " + listenFile);
					System.err.println("[OnFile] Stopping listening for file");
					scheduler.cancelTask(checkTask);
				}
			}

		}, 0, checkInterval);
	}

	private void onFile() {
		for (String command : commands) {
			this.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		}
	}

	@Override
	public void onDisable() {

	}
}
