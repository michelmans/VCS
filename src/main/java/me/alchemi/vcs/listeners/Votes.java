package me.alchemi.vcs.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.vexsoftware.votifier.model.VotifierEvent;

import me.alchemi.vcs.VCS;

public class Votes implements Listener {
	
	@EventHandler
	public void onVote(VotifierEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				VCS.getInstance().getVotedata().addVote(e.getVote());
			}
		}.runTaskAsynchronously(VCS.getInstance());
	}

}
