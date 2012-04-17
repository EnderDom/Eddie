package databases.bioSQL.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tools.Tools_System;

import databases.bioSQL.interfaces.Setup;

public class MySQL_Setup implements Setup{


	public boolean addEddie2Database(Connection con) {
		// TODO Auto-generated method stub
		return false;
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

}