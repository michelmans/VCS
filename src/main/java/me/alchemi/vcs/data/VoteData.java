package me.alchemi.vcs.data;

import java.sql.ResultSet;
import java.sql.SQLException;

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

	protected static Column userUuid = new Column("useruuid", DataType.VARCHAR, ColumnModifier.NOT_NULL, ColumnModifier.UNIQUE);
	protected static Column userName = new Column("username", DataType.VARCHAR, ColumnModifier.NOT_NULL);
	protected static Column voteCount = new Column("votecount", DataType.INT, ColumnModifier.DEFAULT) {
		{
			setDefValue(0);
		}
	};
	protected static Column lastSite = new Column("lastSite", DataType.VARCHAR, ColumnModifier.NOT_NULL);
	protected static Column votePeriod = new Column("mm/yyyy", DataType.VARCHAR, ColumnModifier.NOT_NULL);
	
	protected static Table votesTable = new Table("votes", userUuid, userName, voteCount, lastSite, votePeriod);
	
	public VoteData() {
		try {
			db = MySQLDatabase.newConnection(VCS.getInstance(), Database.HOST.asString(), Database.DATABASE.asString(), Database.USERNAME.asString(), Database.PASSWORD.asString());
			if (!db.doesTableExist(votesTable.getName())) db.createTable(votesTable);
			
		} catch (SQLException e) {
			Messenger.printStatic("Could not connect to database:", e.getMessage());
		}
	}
	
	public void addVote(Vote vote) {
		OfflinePlayer player = Library.getOfflinePlayer(vote.getUsername());
		if (player == null) return;
		
		String period = CarbonDating.getCurrentDateTime().month + CarbonDating.getCurrentDateTime().year;
		
		try {
			ResultSet set = db.getValues(votesTable, userUuid, player.getUniqueId(), voteCount, votePeriod, userName);
			if (set.first()) {
				int votes = set.getInt(voteCount.getName());
				
				if (set.getString(votePeriod.getName()).equals(period)) {
					db.updateValue(votesTable, column, newValue, conditionalValues)
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
