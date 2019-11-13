package me.alchemi.vcs.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import com.vexsoftware.votifier.model.Vote;

import me.alchemi.al.Library;
import me.alchemi.al.configurations.Messenger;
import me.alchemi.al.database.Column;
import me.alchemi.al.database.ColumnModifier;
import me.alchemi.al.database.DataType;
import me.alchemi.al.database.Table;
import me.alchemi.al.database.mysql.MySQLDatabase;
import me.alchemi.al.objects.Callback;
import me.alchemi.al.objects.handling.CarbonDating;
import me.alchemi.vcs.Config.Database;
import me.alchemi.vcs.VCS;

public class VoteData {

	protected static MySQLDatabase db;
	
	//global column
	protected static Column userName = new Column("username", DataType.TEXT, ColumnModifier.NOT_NULL);
	
	//total columns
	protected static Column totalUUID = new Column("uuid", DataType.VARCHAR, ColumnModifier.NOT_NULL, ColumnModifier.UNIQUE) {
		{
			setValueLimit(38);
		}
	};
	protected static Column voteCount = new Column("votecount", DataType.INT, ColumnModifier.DEFAULT) {
		{
			setDefValue(0);
		}
	};
	protected static Column lastVoteID = new Column("lastVoteID", DataType.INT, ColumnModifier.NOT_NULL); 
	
	//logging columns
	protected static Column loggingUUID = new Column("uuid", DataType.VARCHAR, ColumnModifier.NOT_NULL) {
		{
			setValueLimit(38);
		}
	};
	protected static Column voteID = new Column("voteID", DataType.INT, ColumnModifier.UNIQUE, ColumnModifier.AUTO_INCREMENT);
	protected static Column site = new Column("site", DataType.TEXT, ColumnModifier.NOT_NULL);
	protected static Column votePeriod = new Column("period", DataType.VARCHAR, ColumnModifier.NOT_NULL) {
		{
			setValueLimit(7);
		}
	};
	private static Table loggingTable = new Table("votes_logging",  voteID, loggingUUID, userName, site, votePeriod);
	private static Table totalTable = new Table("votes_total", totalUUID, userName, voteCount, lastVoteID);
	
	public VoteData() {
		try {
			db = MySQLDatabase.newConnection(VCS.getInstance(), Database.HOST.asString(), Database.DATABASE.asString(), Database.USERNAME.asString(), Database.PASSWORD.asString());
			if (!db.doesTableExist(loggingTable.getName())) db.createTable(loggingTable);
			if (!db.doesTableExist(totalTable.getName())) db.createTable(totalTable);
			
		} catch (SQLException e) {
			Messenger.printStatic("Could not connect to database:", e.getMessage());
		}
	}
	
	public void addVote(Vote vote) {
		OfflinePlayer player = Library.getOfflinePlayer(vote.getUsername());
		if (player == null) return;
		
		String period = getPeriod();
		
		db.insertValues(loggingTable, new HashMap<Column, Object>(){
			{
				put(loggingUUID, player.getUniqueId().toString());
				put(userName, player.getName());
				put(site, vote.getServiceName());
				put(votePeriod, period);
			}
		});
		
		new BukkitRunnable() {
			@Override
			public void run() {
				db.getValueAsync(loggingTable, voteID, loggingUUID, player.getUniqueId().toString(), new Callback<ResultSet>() {
					@Override
					public void call(ResultSet callObject) {
						try {
							ResultSet set = db.getValues(totalTable, totalUUID, player.getUniqueId().toString(), voteCount, userName);
							
							if (!callObject.first()) {
								Messenger.printStatic(callObject);
								return;
							}
							int voteid = callObject.getInt(voteID.getName());
							while(callObject.next()) {
								int id = callObject.getInt(voteID.getName());
								if (id > voteid) voteid = id;
							}
							final int id = voteid;
							
							if (set.first()) {
								db.updateValues(totalTable, new HashMap<Column, Object>(){
									{
										put(lastVoteID, id);
										put(voteCount, set.getInt(voteCount.getName()) + 1);
									}
								}, totalUUID, player.getUniqueId().toString());
								
							} else registerVoter(player, id);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}.runTaskLaterAsynchronously(VCS.getInstance(), 30);
	}
	
	protected void registerVoter(OfflinePlayer player, int voteID) {
		db.insertValues(totalTable, new HashMap<Column, Object>(){
			{
				put(totalUUID, player.getUniqueId().toString());
				put(userName, player.getName());
				put(voteCount, 1);
				put(lastVoteID, voteID);
			}
		});
	}
	
	protected final String getPeriod() {
		return CarbonDating.getCurrentDateTime().month + CarbonDating.getCurrentDateTime().year;
	}
	
}
