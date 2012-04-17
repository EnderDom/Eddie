package databases.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ui.PropertyLoader;
import ui.UI;

public class DatabaseManager {

	Connection con;
	UI ui;
	PropertyLoader loader;
	Logger logger = Logger.getRootLogger();
	String database;
	public static String default_database = "biosql_eddie";
	private String password;
	
	
	public DatabaseManager(UI ui){
		this.ui = ui;
		this.loader= ui.getPropertyLoader();
	}

	public Connection openConnection(String dbtype, String driver, String dbhost, String dbname, String dbuser, String dbpass, boolean dbnom){
		try{
			Class.forName(driver).newInstance();
			String mys = "jdbc:"+dbtype+"://"+dbhost;
			if(dbnom){
				mys = "jdbc:"+dbtype+"://"+dbhost+"/"+dbname+"";
			}
			logger.debug("About to run: " + mys);
			this.con = DriverManager.getConnection(mys, dbuser, dbpass);
			if(this.con == null){
				logger.error("Argh, failed to connect to database");
			}
		}
		catch(SQLException e){
			logger.error("Failed to open mysql Connection ", e);
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}");
		} 
		catch (InstantiationException e) {
			logger.error("Failed to open mysql Connection", e);
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}");
		} 
		catch (IllegalAccessException e) {
			logger.error("Failed to open mysql Connection", e);
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}");
		}
		catch (ClassNotFoundException e) {
			logger.error("Failed to open mysql Connection", e);
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}");
		}
		return this.con;
	}
	
	public Connection openDefaultConnection(boolean db){
		String[] mydb = loader.getDBSettings();
		if(password == null)password = ui.requiresUserPassword("Password for access to "+mydb[2] + " database for user " + mydb[4], "Please enter the database password");
		return this.openConnection(mydb[0], mydb[1], mydb[2], mydb[3], mydb[4], password, db);
	}
	
	public void setDatabase(String s){
		this.database = s;
		openDefaultConnection(true);
	}
	
	public void createAndOpen(String s){
		this.database = s;
		createNewDatabase(s);
		openDefaultConnection(true);
	}
	
	public Connection getCon(){
		if(this.con == null){
			openDefaultConnection(true);
		}
		return this.con;
	}
	
	public boolean createNewDatabase(String dbname){
		openDefaultConnection(false);
		try {
			Statement st = this.con.createStatement();
			st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
			return true;
		}
		catch (SQLException e) {
			logger.error("Failed to create new database "+dbname, e);
			return false;
		}
	}
	
	public boolean close(){
		try{
			this.con.close();
			return true;
		}
		catch(SQLException sqle){
			logger.error("Failed to close Connection"+sqle);
			return false;
		}
	}
	
}
