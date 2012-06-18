package databases.general.mysql;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

public class Tools_SQL_MySQL {

	public static int getTableCount(Connection con){
		
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery("SHOW TABLES");
			int i=0;
			while(set.next()){
				i++;
			}
			return i;
		}
		catch(SQLException sq){
			Logger.getRootLogger().warn("Failed to get table count" + sq);
			return -1;
		}
	}
	
}
