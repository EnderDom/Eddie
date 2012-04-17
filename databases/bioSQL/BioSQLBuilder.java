package databases.bioSQL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

/**
 * See http://www.biosql.org/
 */

public class BioSQLBuilder {

	DatabaseManager manager;
	Connection con;
	Logger logger = Logger.getRootLogger();
	private static String mysqldb = "biosqldb-mysql.sql";
	
	public BioSQLBuilder(DatabaseManager manager){
		this.manager = manager;
		this.con = manager.getCon();
	}
	
	/**
	 * Builds the Default bioSQL 1.0.1 schema
	 */
	public boolean buildBioSQL4MySQL(){
		
		try{
			String[] sarr = getStatements(mysqldb);
			Statement st = con.createStatement();
			int i =0;
			System.out.print("Building Tables...");
			for(String s : sarr){
				if(s.trim().length() > 0){
					i +=st.executeUpdate(s);
					System.out.print(".");
				}
			}
			System.out.println();
			logger.debug("Returned " + i + " value for " + sarr.length + " queries");
			return true;
		}
		catch(SQLException sexe){
			logger.error("Failed to build bioSQL Table", sexe);
			return false;
		}
		catch(Exception e){
			logger.error("Failed to build bioSQL Table", e);
			return false;
		}
	}
	
	//Assumes the file is within this package
	public String[] getStatements(String fiel) throws IOException{
		InputStream str = this.getClass().getResourceAsStream(fiel);
		logger.debug("Loading File: " + this.getClass().getPackage()+"/"+fiel);
		BufferedReader reader = new BufferedReader(new InputStreamReader(str));
		String line = "";
		StringBuilder currentstate = new StringBuilder("");
		while((line = reader.readLine()) != null){
			if(!line.trim().startsWith("--")){
				currentstate.append(line + " ");
			}
		}
		return currentstate.toString().split(";");
	}
	
	
}
