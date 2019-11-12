package me.alchemi.vcs.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;

import com.vexsoftware.votifier.model.Vote;

import me.alchemi.al.Library;
import me.alchemi.al.configurations.Messenger;
import me.alchemi.al.database.Column;
import me.alchemi.al.database.ColumnModifier;
import me.alchemi.al.database.DataType;
import me.alchemi.al.database.Table;
import me.alchemi.al.database.mysql.MySQLDatabase;
import me.alchemi.al.objects.handling.CarbonDating;
import me.alchemi.vcs.Config.Database;
import me.alchemi.vcs.VCS;

public class VoteData {

	protected static MySQLDatabase db;
	
	protected static Column userUuid = new Column("useruuid", DataType.VARCHAR, ColumnModifier.NOT_NULL, ColumnModifier.UNIQUE) {
		{
			setValueLimit(38);
		}
	};
	protected static Column userName = new Column("username", DataType.TEXT, ColumnModifier.NOT_NULL);
	protected static Column voteCount = new Column("votecount", DataType.INT, ColumnModifier.DEFAULT) {
		{
			setDefValue(0);
		}
	};
	protected static Column lastSite = new Column("lastSite", DataType.TEXT, ColumnModifier.NOT_NULL);
	
	protected static Column votePeriod = new Column("period", DataType.VARCHAR, ColumnModifier.NOT_NULL) {
		{
			setValueLimit(7);
		}
	};
	private static Table templateTable = new Table("votes-", userUuid, userName, voteCount, lastSite, votePeriod);
	
	private Table actualTable;
	
	public VoteData() {
		try {
			db = MySQLDatabase.newConnection(VCS.getInstance(), Database.HOST.asString(), Database.DATABASE.asString(), Database.USERNAME.asString(), Database.PASSWORD.asString());
			actualTable = templateTable.rename(templateTable.getName() + getPeriod());
			if (!db.doesTableExist(actualTable.getName())) db.createTable(actualTable);
			
		} catch (SQLException e) {
			Messenger.printStatic("Could not connect to database:", e.getMessage());
		}
	}
	
	public void addVote(Vote vote) {
		OfflinePlayer player = Library.getOfflinePlayer(vote.getUsername());
		if (player == null) return;
		
		String period = getPeriod();
		
		try {
			ResultSet set = db.getValues(actualTable, userUuid, player.getUniqueId().toString(), voteCount, votePeriod, userName);
			if (set.first()) {
				int votes = set.getInt(voteCount.getName()) + 1;
				
				if (set.getString(votePeriod.getName()).equals(period)) {
					db.updateValues(actualTable, new HashMap<Column, Object>(){
						{
							put(voteCount, votes);
							put(lastSite, vote.getServiceName());
						}
					}, userUuid, player.getUniqueId().toString());
				} else {
					
					actualTable = templateTable.rename(templateTable.getName() + period);
					if (!db.doesTableExist(actualTable.getName())) db.createTable(actualTable);
					registerVoter(player, vote.getServiceName());
					
				}		
			} else registerVoter(player, vote.getServiceName());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void registerVoter(OfflinePlayer player, String site) {
		db.insertValues(actualTable, new HashMap<Column, Object>(){
			{
				put(userUuid, player.getUniqueId().toString());
				put(userName, player.getName());
				put(voteCount, 1);
				put(lastSite, site);
				put(votePeriod, getPeriod());
			}
		});
	}
	
	protected final String getPeriod() {
		return CarbonDating.getCurrentDateTime().month + CarbonDating.getCurrentDateTime().year;
	}
	
}
