package databases.bioSQL.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tools.Tools_System;
import databases.bioSQL.interfaces.BioSQLExtended;

public class MySQL_Extended implements BioSQLExtended{

	
	private static String assembly = "SEQUENCEASSEMBLY";
	private static String description = "Eddie Linker Reference, used to to link Assembly terms";
	private int assemblyid =-1;
	
	public boolean addEddie2Database(Connection con) {
		String insert = new String("INSERT INTO `biodatabase` (`name`, `authority`, `description`) VALUES ('"+programname+"', '"+authority+"', '"+description+"')");
		try{
			Statement st = con.createStatement();
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create biodatabase table", se);
			return false;
		}
	}
	
	public int getEddieFromDatabase(Connection con){
		String insert = new String("SELECT biodatabase_id FROM biodatabase WHERE name='"+programname+"';");
		int db =-1;
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				db = set.getInt("biodatabase_id");
			}
			st.close();
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create biodatabase table", se);
			
		}
		return db;
	}
	
	public boolean addLegacyVersionTable(Connection con, String version, String dbversion) {
		String info_table = "CREATE TABLE IF NOT EXISTS `info` ("+
		  "`Numb` int(11) NOT NULL AUTO_INCREMENT,"+
		  "`DerocerasVersion` varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL,"+
		  "`DatabaseVersion` varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL,"+
		  "`LastRevision` date DEFAULT NULL,"+
		  "PRIMARY KEY (`Numb`)";
		String insert = new String("INSERT INTO `info` (`Numb`, `DerocerasVersion`, `DatabaseVersion`, `LastRevision`) VALUES (1, 'v"+version+"', 'v"+dbversion+"', '"+Tools_System.getDateNow("yyyy-MM-dd")+"')"+
				" ON DUPLICATE KEY UPDATE `DerocerasVersion`='"+version+"', `DatabaseVersion`='"+dbversion+"', `LastRevision`='"+Tools_System.getDateNow("yyyy-MM-dd")+"' ;");
		try{
			Statement st = con.createStatement();
			st.executeUpdate(info_table);
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create INFO table", se);
			return false;
		}
		
	}

	public boolean addBioEntrySynonymTable(Connection con) {
		String table = "CREATE TABLE bioentry_synonym (" +
				"bioentry_synonym_id INT(10) UNSIGNED NOT NULL auto_increment," +
				"bioentry_id	    INT(10) UNSIGNED NOT NULL," +
			  	"identifier   	VARCHAR(40) BINARY, " +
				"PRIMARY KEY (bioentry_synonym_id)," +
			 	"UNIQUE (identifier)" +
			") TYPE=INNODB;";
		try{
			Statement st = con.createStatement();
			st.executeUpdate(table);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create bioentry_synonym table", se);
			return false;
		}
	}
	
	public boolean setupAssembly(MySQL_BioSQL boss, Connection con){
		this.assemblyid=boss.getOntology(con, assembly);
		if(this.assemblyid<0){
			return boss.addOntology(con, assembly, description) ?
					((this.assemblyid=boss.getOntology(con, assembly))> -1) : false;
		}
		else{
			return true;
		}
	}
	
	public boolean addAssemblyTerm(){
		
		return true;
	}

}
