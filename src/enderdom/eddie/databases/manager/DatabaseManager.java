package enderdom.eddie.databases.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.bioSQL.mysql.MySQL_BioSQL;
import enderdom.eddie.databases.bioSQL.mysql.MySQL_Extended;
import enderdom.eddie.databases.general.mysql.Tools_SQL_MySQL;
//import databases.bioSQL.pgsql.PgSQL_BioSQL;
//import databases.bioSQL.pgsql.PgSQL_Extended;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.UI;

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
		catch (InstantiationException e) {
			ui.error("Could not create driver "+driver+" sql class instance ",e);
			this.con = null;
		} 
		catch (IllegalAccessException e) {
			ui.error("Could not access sql class",e);
			this.con = null;
		} 
		catch (ClassNotFoundException e) {
			ui.error("Could not create sql class",e);
			this.con = null;
		}
		catch (SQLException e) {
			ui.error("Params{ DBTYPE:"+dbtype+" DBDRIVER:"+driver+" DBHOST:"+dbhost+" DBNAME:"+dbname+" DBUSER:" + dbuser +"}",e);
			this.con = null;
		} 
		if(this.con !=null)isOpen=true;
		else {
			isOpen = false;
			this.password = null;//Flush pass, as most of the time this is the problem
		}
		return this.con;
	}

	public Connection openDefaultConnection(boolean db){
		return openDefaultConnection(loader.getDBSettings(), db);
	}
	
	private Connection openDefaultConnection(String[] mydb, boolean db){
		if(password == null)password = ui.requiresUserPassword("Password for access to "+mydb[2] + " database for user " + mydb[4], "Password Request");
		if(password != null && password.length() > 0){
			return this.openConnection(mydb[0], mydb[1], mydb[2], mydb[3], mydb[4], password, db);
		}
		else return null;
	}
	
	public void setDatabase(String s){
		this.database = s;
		openDefaultConnection(true);
	}
	
	public boolean createAndOpen(){
		String[] mydb = loader.getDBSettings();
		createNewDatabase(mydb[3]);
		this.con = openDefaultConnection(mydb, true);
		return (con != null);
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
				Statement st = con.createStatement();
				st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
				st.close();
			}
			return true;
		}
		catch (SQLException e) {
			ui.error("Failed to create new database "+dbname, e);
			return false;
		}
	}
	
	public boolean close(){
		try{
			this.con.close();
			return true;
		}
		catch(SQLException sqle){
			ui.error("Failed to close Connection: ",sqle);
			return false;
		}
	}
	
	public BioSQL getBioSQL(){
		if(this.biosql == null){
			if(this.dbtype.equals("mysql")){
				this.biosql = new MySQL_BioSQL();
			}
			else if(this.dbtype.equals("postgresql")){
				//this.biosql = new PgSQL_BioSQL();
				logger.warn("Not yet supported :( , sorry");
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
			else if(this.dbtype.equals("postgresql")){
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
			ui.error("Fruit in database error.");
			return -1;
		}
		else{
			ui.error("Unimplemented Procedure");
			return -1;
		}
	}
	
	public int getLastInsert(){
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery("SELECT LAST_INSERT_ID()");
			while(set.next()){
				return set.getInt(1);
			}
			return -1;
		}
		catch(SQLException sq){
			ui.error("Failed to get last insert, you sure you inserted stuff");
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

	public ResultSet runSQLQuery(String arg0) throws SQLException{
		return this.con.createStatement().executeQuery(arg0);
	}
	
	public String getDBTYPE(){
		return this.dbtype;
	}
}
