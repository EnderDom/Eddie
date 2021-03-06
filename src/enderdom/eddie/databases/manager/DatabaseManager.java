package enderdom.eddie.databases.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.bioSQL.interfaces.SQLGeneral;
import enderdom.eddie.databases.bioSQL.mysql.MySQL_BioSQL;
import enderdom.eddie.databases.bioSQL.mysql.MySQL_Extended;
import enderdom.eddie.databases.bioSQL.mysql.MySQL_General;
import enderdom.eddie.exceptions.EddieDBException;
//import databases.bioSQL.pgsql.PgSQL_BioSQL;
//import databases.bioSQL.pgsql.PgSQL_Extended;
import enderdom.eddie.tasks.internal.Task_DatabaseUpdate;
import enderdom.eddie.ui.EddieProperty;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.UI;
import enderdom.eddie.ui.UserResponse;

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
	private SQLGeneral sqlgen;
	private int biodatabase_id =-1;
	private static double databaseversion =3.0;
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
	
	public boolean open() throws InstantiationException,
		IllegalAccessException, ClassNotFoundException, 
			SQLException, EddieDBException, InterruptedException{
		if(openDefaultConnection() != null){
			return true;
		}
		else{
			return false;
		}
	}
	
	public synchronized Connection openConnection(String dbtype, String driver, String dbhost, String dbname, String dbuser, String dbpass) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, EddieDBException, InterruptedException{
		this.dbtype=dbtype;
		if(driver==null || driver.length() == 0){
			if(dbtype.equals("mysql"))driver="com.mysql.jdbc.Driver";
			else if(dbtype.equals("sqllite"))driver="org.sqlite.JDBC";
			loader.setValue(EddieProperty.DBDRIVER.toString(), driver);
		}
		Class.forName(driver).newInstance();
		String mys = "jdbc:"+dbtype+"://"+dbhost;
		logger.debug("Using to check database "+dbname+" exists: " + mys);
		this.con = DriverManager.getConnection(mys, dbuser, dbpass);
		boolean checkdb = true;
		if(!dbExists(dbname)){
			logger.info("Created new database at " + dbname+ " you will need to run -task sqladmin -setup");
			createNewDatabase(dbname);
			checkdb =false;
		}
		else logger.debug("Database does exist. Yay!");
		mys = "jdbc:"+dbtype+"://"+dbhost+"/"+dbname+"";
		logger.debug("Now using to run: " + mys);
		logger.debug("Params{ DBTYPE="+dbtype+" DBDRIVER="+driver+" DBHOST="+dbhost+" DBNAME="+dbname+" DBUSER=" + dbuser +" PASS=******}");
		this.con = DriverManager.getConnection(mys, dbuser, dbpass);
		if(checkdb)checkVersion();
		if(this.con !=null)isOpen=true;
		else {
			isOpen = false;
			this.password = null;//Flush pass, as most of the time this is the problem
		}
		
		return this.con;
	}

	public static String[] getDatabaseSettings(PropertyLoader loader){
		String[] defaultsets = new String[5];
		defaultsets[0]=loader.getValue(EddieProperty.DBTYPE.toString());
		defaultsets[1] =loader.getValue(EddieProperty.DBDRIVER.toString());
		defaultsets[2] =loader.getValue(EddieProperty.DBHOST.toString());
		defaultsets[3] =loader.getValue(EddieProperty.DBNAME.toString());
		defaultsets[4] =loader.getValue(EddieProperty.DBUSER.toString());
		return defaultsets;
	}
	
	public Connection openDefaultConnection() throws InstantiationException, 
		IllegalAccessException, ClassNotFoundException, SQLException, EddieDBException, InterruptedException{
		return openDefaultConnection(getDatabaseSettings(this.loader));
	}
	
	private Connection openDefaultConnection(String[] mydb) throws InstantiationException, 
		IllegalAccessException, ClassNotFoundException, SQLException, EddieDBException, InterruptedException{
		if(password == null)password = ui.requiresUserPassword("Password for access to "+mydb[2] + " database for user " + mydb[4], "Password Request");
		if(password != null && password.length() > 0){
			return this.openConnection(mydb[0], mydb[1], mydb[2], mydb[3], mydb[4], password);
		}
		else return null;
	}
	
	public void setDatabase(String s) throws InstantiationException,
		IllegalAccessException, ClassNotFoundException, SQLException,
			EddieDBException, InterruptedException{
		this.database = s;
		openDefaultConnection();
	}
	
	public Connection getCon(){
		if(this.con == null){
			try{
				openDefaultConnection();
			}
			catch(Exception e){
				logger.error("Failed to get connection",e);
			}
		}
		return this.con;
	}
	
	public boolean createNewDatabase(String dbname){
		try {
			if(this.dbtype.equals("mysql")){
				Statement st = con.createStatement();
				st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
				st.close();
			}
			return true;
		}
		catch (SQLException e) {
			logger.error("Failed to create new database "+dbname, e);
			return false;
		}
	}
	
	public boolean dbExists(String dbname) throws SQLException{
		if(this.dbtype.equals("mysql")){
			Statement st = this.con.createStatement();
			ResultSet set = st.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '"+dbname+"'");
			String shema =null;
			while(set.next())shema=set.getString(1);
			if(shema == null){
				return false;
			}
			else{
				return true;
			}
		}
		else if(this.dbtype.equals("sqllite")){
			
		}
		return false;
	}
	
	public boolean close(){
		try{
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
				//commit everything left in buffer
				this.biosql.largeInsert(this.getCon(), false);
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
	
	public SQLGeneral getSQLGeneral(){
		if(this.sqlgen == null){
			if(this.dbtype.equals("mysql")){
				this.sqlgen = new MySQL_General();
			}
			else if(this.dbtype.equals("postgresql")){
				//this.biosqlext = new PgSQL_Extended();
			}
			else{
				logger.warn("Database type not set or not recognised");
			}
		}
		return this.sqlgen;
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
	
	public int getTableCount() throws Exception{
		if(this.dbtype.equals("mysql")){
			return getSQLGeneral().getTableCount(getCon());
		}
		else if(this.dbtype.equals("banana")){
			logger.error("Fruit in database error.");
			return -1;
		}
		else{
			logger.error("Unimplemented Procedure");
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
			logger.error("Failed to get last insert, you sure you inserted stuff", sq);
			return -1;
		}
	}
	
	public synchronized boolean checkVersion() throws EddieDBException, InterruptedException{
		double vers = this.getBioSQLXT().getDatabaseVersion(this);
		if(vers < databaseversion && vers != -1){
			UserResponse i = ui.requiresUserYNI("Do you want to update this version of the database " +
					"(y)es update, (n)o do not do anything or " +
					"(c)ontinue and ignore this warning? (y/n/c)", "Database version is out of date");
			if(i == UserResponse.YES){
				Task_DatabaseUpdate update = new Task_DatabaseUpdate(vers, databaseversion, this);
				ui.getTaskManager().addTask(update);
				int timeout = 60000;
				while(!update.isDone()){
					this.wait(3000);
					timeout-=3000;
					if(timeout <0){
						UserResponse s = ui.requiresUserYNI("Update has timed out. Continue waiting? (y/n)", "Update timeout");
						if(s == UserResponse.YES) timeout = 120000;
						else {
							throw new EddieDBException("Timeout expired error thrown");
						}
					}
				}
			}
			else if(i == UserResponse.NO){
				throw new EddieDBException("User chose not continue the task, error thrown to break thread");
			}
			else{
				logger.warn("User chose to ignore warnings. Problems may ensue");
			}
		}
		else if(vers > databaseversion){
			logger.warn("This version of Eddie does not support databases > " + databaseversion);
		}
		return false;
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
	
	public String getDBTYPE(){
		if(this.dbtype ==null){
			this.dbtype =loader.getValue(EddieProperty.DBTYPE.toString());
			System.out.println(this.dbtype);
		}
		return this.dbtype;
	}

}
