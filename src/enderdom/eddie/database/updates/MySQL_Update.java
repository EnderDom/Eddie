package enderdom.eddie.database.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Stack;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tools.Tools_System;

public class MySQL_Update{

	public static boolean updbto24(DatabaseManager manager) throws SQLException {
		String alters[] = new String[]{
				"ALTER TABLE bioentry_dbxref DROP PRIMARY KEY, ADD PRIMARY KEY (bioentry_id,dbxref_id,rank,run_id);",
				"ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKdbxref_bioentry_run FOREIGN KEY (run_id) REFERENCES run(run_id) ON DELETE CASCADE;",
				"ALTER TABLE "+BioSQLExtended.runtable+" MODIFY source VARCHAR(80) BINARY;",
		};
		Statement st = manager.getCon().createStatement();
		for(String s: alters)st.executeUpdate(s);
		st.close();
		updateDatabase(manager);
		return true;
	}	
	
	public static boolean updbto25(DatabaseManager manager) throws SQLException {
		manager.getBioSQLXT().addRunBioentryTable(manager);
		//Hack, but it is assumed I am the only one using a 2.4 database
		Logger.getRootLogger().info("Updating sequences...");
		Statement st = manager.getCon().createStatement();
		ResultSet set = st.executeQuery("SELECT bioentry_id FROM bioentry WHERE division='READ' AND bioentry_id<131755");
		int c=0;
		Stack<Integer> ids = new Stack<Integer>();
		//while(set.next()){
//			ids.push(set.getInt("bioentry_id"));
			//c++;
		//}
		//while(ids.size() !=0)st.executeUpdate("INSERT INTO bioentry_run (bioentry_id, run_id, rank) VALUES ("+ids.pop()+",8, 0)");
		//Logger.getRootLogger().info("Inserted "+c+ " entries into new bioentry_run table for digestive gland");
		set = st.executeQuery("SELECT bioentry_id FROM bioentry WHERE division='READ' AND bioentry_id>131754");
		c=0;
		while(set.next()){
			ids.push(set.getInt("bioentry_id"));
			c++;
		}
		while(ids.size() !=0)st.executeUpdate("INSERT INTO bioentry_run (bioentry_id, run_id, rank) VALUES ("+ids.pop()+",9, 0)");
		Logger.getRootLogger().info("Inserted "+c+ " entries into new bioentry_run table for digestive gland");
		
		if(c > 1000)updateDatabase(manager);
		return true;
	}	
	
	public static void updateDatabase(DatabaseManager manager) throws SQLException{
		Statement st = manager.getCon().createStatement();
		st.executeUpdate("UPDATE info SET DatabaseVersion='"+DatabaseManager.getDatabaseversion()+"', LastRevision='"+Tools_System.getDateNow("yyyy-MM-dd")+"';");
		st.close();
	}
}
