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

/**
 *
 * @author Dominic M. Wood
 *
 */
public class DatabaseManager {

	private Connection con;
	private UI ui;
	private PropertyLoader loader;
	Logger logger = Logger.getRootLogger();
	private String database;
	public static String default_database = "biosql_eddie";
	private String password;
	private String dbtype;
	private BioSQL biosql;
	private BioSQLExtended biosqlext;
	private int biodatabase_id =-1;
	private static double databaseversion =2.3;
	private boolean isOpen;
	/*
	 * Only one statement, thus only one query at a time
	 * Not sure if this is wise, but as this DatabaseManager object
	 * currently only works with one db at a time, I think it should be 
	 * fine to run in serial. And if other dbs are needed, a separate dbmanager 
	 * can be created.
	 * 
	 */
	private Statement st; 
	
	
	public DatabaseManager(UI ui){
		this.ui = ui;
		this.loader= ui.getPropertyLoader();
	}
	
	public DatabaseManager(UI ui, String password){
		this.ui = ui;
		this.loader= ui.getPropertyLoader();
		this.password = password;
	}
	
	public void setPassword(String pass){
		this.password = pass;
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
		}
		catch (SQLException e) {
			logger.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
			this.con = null;
		} 
		catch (InstantiationException e) {
			logger.error("Could not create driver "+driver+" sql class instance ",e);
			this.con = null;
		} 
		catch (IllegalAccessException e) {
			logger.error("Could not access sql class",e);
			this.con = null;
		} 
		catch (ClassNotFoundException e) {
			logger.error("Could not create sql class",e);
			this.con = null;
		}
		if(this.con !=null)isOpen=true;
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
			if(this.dbtype.equals("mysql")){
				this.getStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
			}
			//st.executeUpdate("CREATE GOD IF NOT EXISTS " + dbname);
			return true;
		}
		catch (SQLException e) {
			logger.error("Failed to create new database "+dbname, e);
			return false;
		}
	}
	
	public boolean close(){
		try{
			this.st.close();
			this.con.close();
			return true;
		}
		catch(SQLException sqle){
			logger.error("Failed to close Connection: ",sqle);
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
	
	/** Automatically adds Eddie if not already added
	 * @return Eddies Id from the bioSQL table
	 */
	public int getEddieDBID(){
		if(this.biodatabase_id < 0){
			BioSQLExtended bsxt = getBioSQLXT();
			if( (this.biodatabase_id= bsxt.getEddieFromDatabase(this)) <0 ){
				bsxt.addEddie2Database(this);
				this.biodatabase_id = bsxt.getEddieFromDatabase(this);
			}
		}
		return biodatabase_id;
	}

	public static double getDatabaseversion() {
		return databaseversion;
	}
	
	public int getTableCount(){
		if(this.dbtype.equals("mysql")){
			return Tools_SQL_MySQL.getTableCount(getCon());
		}
		else if(this.dbtype.equals("banana")){
			logger.error("That's not a type of database that's a fruit?");
			return -1;
		}
		else{
			logger.error("Unimplemented Procedure");
			return -1;
		}
	}
	
	public boolean isOpen(){
		return this.isOpen;
	}

	public String getDatabase() {
		return database;
	}

	public UI getUi() {
		return ui;
	}

	public void setUi(UI ui) {
		this.ui = ui;
	}

	public Statement getStatement() {
		if(st == null){
			try {
				st=con.createStatement();
			} 
			catch (SQLException e) {
				logger.error("Error, failed to create statement", e);
			}
		}
		return st;
	}

	public void setStatement(Statement st) {
		this.st = st;
	}
	
}
