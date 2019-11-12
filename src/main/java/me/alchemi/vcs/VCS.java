package me.alchemi.vcs;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

import me.alchemi.al.configurations.Messenger;
import me.alchemi.al.objects.base.PluginBase;
import me.alchemi.vcs.data.VoteData;
import me.alchemi.vcs.listeners.Votes;

public class VCS extends PluginBase {
	
	private static VCS instance;
	
	private VoteData votedata;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		setMessenger(new Messenger(this));
		
		try {
			new Config();
			messenger.print("&6Configs enabled!");
		} catch (IOException | InvalidConfigurationException e) {
			System.err.println("[PLUGIN]: Could not enable config files.\nDisabling plugin...");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
		
		votedata = new VoteData();
		
		Bukkit.getPluginManager().registerEvents(new Votes(), this);
		
		enableCommands();
		
		messenger.print("ENABLED");
	}
	
	@Override
	public void onDisable() {
		messenger.print("DISABLED");
	}
	
	public void enableCommands() {
		//getCommand("COMMAND").setExecutor(new Command());
		
		//getCommand("clicker").setTabCompleter(new TabCompleter());
	}
	
	public static VCS getInstance() {
		return instance;
	}
	
	public Messenger getMessenger() {
		return messenger;
	}

	public VoteData getVotedata() {
		return votedata;
	}
	
}
