package tasks.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;

import tasks.Task;
import tools.Tools_System;
import ui.PropertyLoader;
import ui.UI;

public class Task_BioSQLDB extends Task{

	private UI ui;
	private Logger logger = Logger.getRootLogger();
	private boolean setup;
	private String table;
	
	public Task_BioSQLDB(){
		setHelpHeader("--This is the Help Message for the the AdminBioSQLDB Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("setup"))setup=true;
		if(cmd.hasOption("table"))table = cmd.getOptionValue("table");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("setup", false, "Perform default setup"));
		options.addOption(new Option("table", true,  "Add a specific non-biosql table or table mod, use -table list for list of tables"));
		options.getOption("test").setDescription("Test the database connection");
	}
	
	public Options getOptions(){
		return this.options;
	}

	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(testmode){
			runTest();
			return;
		}
		if(table != null && table.contentEquals("list") ){
			System.out.println("---List of Tables Available---");
			System.out.println("legacy");
			//System.out.println("synonym");
			System.out.println("xrefs");
			System.out.println("assembly");
			System.out.println("run");
			System.out.println("---								 ---");
			return;
		}
		if(table != null || setup){
			DatabaseManager manager = this.ui.getDatabaseManager(password);
			if(!manager.open()){
				logger.error("Failed to open Database manager");
				return;
			}
			int biodatabase_id = manager.getEddieDBID();
			if(biodatabase_id > -1){
				if(setup){
					logger.debug("Setting up database...");
					setup(manager);
					logger.info("Done setting Up");
				}
				else if(table != null){
					logger.debug("Identifying table...");
					if(createDefaultTable(manager, table)){
						logger.info("Successfully setup table or at least listed tables to be setup.");
					}
					else{
						logger.info("Failed to setup table");
					}
				}
				//Other tasks here
				else{
					logger.warn("No option set");
				}
			}
			else{
				logger.error("Failed to get the database_id for Eddie :(");
			}
			manager.close();
		}
		else{
			logger.info("No option selected");
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
	public boolean canProceedwithSetup(DatabaseManager manager){
		manager.createAndOpen();
		int j = manager.getTableCount();
		logger.debug("Database contains " + j + " tables");
		if(j > 0){
			double d = manager.getBioSQLXT().getDatabaseVersion(manager);
			if(d < 0){
				if(manager.getBioSQLXT().getEddieFromDatabase(manager) > 0){
					logger.warn("Eddie uses this database, but has not added a the  info table ??? >:S");
					return true;
				}
				else{
					logger.error("This database has not been used by Eddie, run an integration script if they've been written yet?");
					return false;
				}
			}
			else if(d < DatabaseManager.getDatabaseversion()){
				logger.error("Database version is too old v"+d+", run update to v"+DatabaseManager.getDatabaseversion()+" if available or manually remove database");
				return false;
			}
			else{
				return true;
			}
		}
		else{
			logger.debug("0 Tables found, building database");
			manager.getBioSQL().buildDatabase(manager.getCon());
			return true;
		}
	}
	
	public boolean setup(DatabaseManager manager){
		if(canProceedwithSetup(manager)){
			logger.debug("Setting up database");
			BioSQLExtended bsxt = manager.getBioSQLXT();
			bsxt.addLegacyVersionTable(manager,new String(PropertyLoader.getFullVersion()+""), new String(DatabaseManager.getDatabaseversion()+""));
			bsxt.addBioEntrySynonymTable(manager);
			bsxt.addBioentryDbxrefCols(manager);
			return bsxt.setupAssembly(manager);
		}
		else return false;
	}

	public boolean createDefaultTable(DatabaseManager manager, String table){
		if(table.contentEquals("legacy")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addLegacyVersionTable(manager,new String(PropertyLoader.getFullVersion()+""), new String(DatabaseManager.getDatabaseversion()+""));
			}
		}
//		else if(table.contentEquals("synonym")){
//			if(manager.isOpen()){
//				return manager.getBioSQLXT().addBioEntrySynonymTable(manager);
//			}
//		}
		else if(table.contentEquals("xrefs")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addBioentryDbxrefCols(manager);
			}
		}
		else if(table.contentEquals("assembly")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().setupAssembly(manager);
			}
		}
		else if(table.contentEquals("run")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addRunTable(manager);
			}
		}
		else{
			logger.warn("Table: " + table + " not recognised as a table used by Eddie");
			return false;
		}
		return false;
	}
	
	protected void runTest(){
		System.out.println("");
		System.out.println("--TEST MODE--");
		System.out.println("");
		System.out.println("Initialising Database Manager");
		DatabaseManager manager = new DatabaseManager(ui, password);
		System.out.println("Opening default connection...");
		boolean com = manager.open();
		if(!com)System.out.println("Failed to connect to the database");
		if(com){
			System.out.println("Successfully connected to database");
			double d = manager.getBioSQLXT().getDatabaseVersion(manager);
			if(d != -1){
				System.out.println("Although connection established, the database version could not be ascertained");
			}
			else{
				System.out.println("This database appears to be the correct version for eddie");
			}
			System.out.println("Closing database manager...");
			com= manager.close();
			if(com)System.out.println("Successfully closed the database manager");
			if(!com)System.out.println("Failed to close manager?");
		}
		
	}
}
