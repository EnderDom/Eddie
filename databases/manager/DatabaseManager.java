package databases.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.bioSQL.mysql.MySQL_BioSQL;
import databases.bioSQL.mysql.MySQL_Extended;
import databases.general.mysql.Tools_SQL_MySQL;
//import databases.bioSQL.pgsql.PgSQL_BioSQL;
//import databases.bioSQL.pgsql.PgSQL_Extended;
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
	private String dbtype;
	private BioSQL biosql;
	private BioSQLExtended biosqlext;
	private int biodatabase_id =-1;
	private static int databaseversion =2;
	
	public DatabaseManager(UI ui){
		this.ui = ui;
		this.loader= ui.getPropertyLoader();
	}

	public boolean open(){
		if(openDefaultConnection(true) != null){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Connection openConnection(String dbtype, String driver, String dbhost, String dbname, String dbuser, String dbpass, boolean dbnom){
		try{
			this.dbtype=dbtype;
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
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
		} 
		catch (InstantiationException e) {
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
		} 
		catch (IllegalAccessException e) {
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
		}
		catch (ClassNotFoundException e) {
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
		}
		return this.con;
	}

	public Connection openDefaultConnection(boolean db){
		return openDefaultConnection(loader.getDBSettings(), db);
	}
	
	private Connection openDefaultConnection(String[] mydb, boolean db){
		if(password == null)password = ui.requiresUserPassword("Password for access to "+mydb[2] + " database for user " + mydb[4], "Please enter the database password");
		return this.openConnection(mydb[0], mydb[1], mydb[2], mydb[3], mydb[4], password, db);
	}
	
	public void setDatabase(String s){
		this.database = s;
		openDefaultConnection(true);
	}
	
	public void createAndOpen(){
		String[] mydb = loader.getDBSettings();
		createNewDatabase(mydb[3]);
		openDefaultConnection(mydb, true);
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
	
	public BioSQL getBioSQL(){
		if(this.biosql == null){
			if(this.dbtype.equals("mysql")){
				this.biosql = new MySQL_BioSQL();
			}
			else if(this.biosql.equals("postgresql")){
				//this.biosql = new PgSQL_BioSQL();
			}
			else{
				logger.warn("Database type not set or not recognised");
			}
		}
		return this.biosql;
	}
	
	public BioSQLExtended getBioSQLXT(){
		if(this.biosqlext == null){
			if(this.dbtype.equals("mysql")){
				this.biosqlext = new MySQL_Extended();
			}
			else if(this.biosql.equals("postgresql")){
				//this.biosqlext = new PgSQL_Extended();
			}
			else{
				logger.warn("Database type not set or not recognised");
			}
		}
		return this.biosqlext;
	}
	
	/* Automatically adds Eddie if not already added
	 * Returns Eddies Id from the bioSQL table
	 */
	public int getEddieDBID(){
		if(this.biodatabase_id < 0){
			BioSQLExtended bsxt = getBioSQLXT();
			Connection con = getCon();
			if( (this.biodatabase_id= bsxt.getEddieFromDatabase(con)) <0 ){
				bsxt.addEddie2Database(con);
				this.biodatabase_id = bsxt.getEddieFromDatabase(con);
			}
		}
		return biodatabase_id;
	}

	public static int getDatabaseversion() {
		return databaseversion;
	}
	
	public int getTableCount(){
		if(this.dbtype.equals("mysql")){
			return Tools_SQL_MySQL.getTableCount(getCon());
		}
		else{
			logger.error("Unimplemented Procedure");
			return -1;
		}
	}
}
