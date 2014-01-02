package enderdom.eddie.databases.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tools.Tools_System;

public class MySQL_Update{

	public static boolean updbto24(DatabaseManager manager) throws SQLException {
		String alters[] = new String[]{
				"ALTER TABLE bioentry_dbxref DROP PRIMARY KEY, ADD PRIMARY KEY (bioentry_id,dbxref_id,rank,run_id,hit_no);",
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
	
	public static boolean updbto26(DatabaseManager manager) throws SQLException{
		Statement st = manager.getCon().createStatement();
		ResultSet set = st.executeQuery("SELECT COUNT(DISTINCT(bioentry_id)) AS COUNT FROM bioentry_dbxref");
		int size = 0;
		while(set.next())size=set.getInt(1);
		set.close();
		if(size ==0)return false;
		ArrayList<Integer> ins = new ArrayList<Integer>(size+10);
		set = st.executeQuery("SELECT DISTINCT(bioentry_id) AS id FROM bioentry_dbxref");
		int c=0;
		while(set.next()){
			ins.add(set.getInt(1));
		}
		for(Integer i : ins){
			set = st.executeQuery("SELECT bioentry_id, dbxref_id, rank, score, dbxref_startpos, dbxref_endpos FROM bioentry_dbxref WHERE bioentry_id="+i);
			int hitno=1;
			//THis is uber shit, please don't judge
			LinkedList<String> strs = new LinkedList<String>();
			StringBuffer b = null;
			while(set.next()){
				b = new StringBuffer();
				b.append("UPDATE bioentry_dbxref SET hit_no=");
				b.append(hitno);
				b.append(" WHERE bioentry_id=");
				b.append(set.getInt(1));
				b.append(" AND dbxref_id=");
				b.append(set.getInt(2));
				b.append(" AND rank=");
				b.append(set.getInt(3));
				b.append(" AND score=");
				b.append(set.getInt(4));
				b.append(" AND dbxref_startpos=");
				b.append(set.getInt(5));
				b.append(" AND dbxref_endpos=");
				b.append(set.getInt(6));
				strs.add(b.toString());
				hitno++;
			}
			set.close();
			for(String s : strs){
				st.execute(s);
			}
			c++;
			System.out.print("\r"+c+" of "+size);
		}
		System.out.println();
		updateDatabase(manager);
		return true;
	}
	
	public static void updateDatabase(DatabaseManager manager) throws SQLException{
		Statement st = manager.getCon().createStatement();
		st.executeUpdate("UPDATE info SET DatabaseVersion='"+DatabaseManager.getDatabaseversion()+"', LastRevision='"+Tools_System.getDateNow("yyyy-MM-dd")+"';");
		st.close();
	}

	public static void updbto27(DatabaseManager manager) throws SQLException {
			String alters[] = new String[]{
					"ALTER TABLE "+BioSQLExtended.runtable+" ADD parent_id INT(10) UNSIGNED AFTER runtype",
					"ALTER TABLE "+BioSQLExtended.runtable+" ADD CONSTRAINT FKrun_parent FOREIGN KEY (parent_id) REFERENCES run(run_id) ON DELETE CASCADE;"
			};
			Statement st = manager.getCon().createStatement();
			for(String s: alters)st.executeUpdate(s);
			st.close();
			updateDatabase(manager);
	}
	
	public static void updbto28(DatabaseManager manager)throws SQLException{
		String s = "ALTER TABLE assembly ADD UNIQUE (contig_bioentry_id,read_bioentry_id,run_id)";
		Statement st = manager.getCon().createStatement();
		st.executeUpdate(s);
		st.close();
		updateDatabase(manager);
	}
	
	public static void updbto29(DatabaseManager manager)throws SQLException{
		String s = "ALTER TABLE dbxref ADD description TEXT NOT NULL AFTER ncbi_taxon_id";
		Statement st = manager.getCon().createStatement();
		st.executeUpdate(s);
		st.close();
		updateDatabase(manager);
	}
	
	public static void updbto30(DatabaseManager manager)throws SQLException{
		String s = "ALTER TABLE bioentry_dbxref DROP PRIMARY KEY, ADD PRIMARY KEY (bioentry_id,dbxref_id,rank,hit_no,run_id)";
		Statement st = manager.getCon().createStatement();
		st.executeUpdate(s);
		st.close();
		updateDatabase(manager);
	}
	
	
	
}
