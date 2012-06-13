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
	
	public Task_BioSQLDB(){
		setHelpHeader("--This is the Help Message for the the AdminBioSQLDB Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("setup"))setup=true;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("setup", false, "Perform default setup"));
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
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		manager.createAndOpen();
		int biodatabase_id = manager.getEddieDBID();
		if(biodatabase_id > -1){
			if(setup){
				setup(manager);
				logger.info("Done setting Up");
			}
			else{
				logger.warn("No option set");
			}
		}
		else{
			logger.error("Failed to get the database_id for Eddie :(");
		}
		manager.close();
		
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
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
			double d = manager.getBioSQLXT().getDatabaseVersion(manager.getCon());
			if(d < 0){
				if(manager.getBioSQLXT().getEddieFromDatabase(manager.getCon()) > 0){
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
			bsxt.addLegacyVersionTable(manager.getCon(),new String(PropertyLoader.getFullVersion()+""), new String(DatabaseManager.getDatabaseversion()+""));
			bsxt.addBioEntrySynonymTable(manager.getCon());
			bsxt.addBioentryDbxrefCols(manager.getCon());
			return bsxt.setupAssembly(manager.getBioSQL(), manager.getCon());
		}
		else return false;
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
			double d = manager.getBioSQLXT().getDatabaseVersion(manager.getCon());
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
