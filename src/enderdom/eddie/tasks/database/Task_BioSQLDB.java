package enderdom.eddie.tasks.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.EddieProperty;
import enderdom.eddie.ui.EddiePropertyLoader;

public class Task_BioSQLDB extends TaskXT{

	private boolean setup;
	private String table;
	private String setDB;
	private boolean showDB;
	
	public Task_BioSQLDB(){
		setHelpHeader("--This is the Help Message for the the AdminBioSQLDB Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		setup=cmd.hasOption("setup");
		table = getOption(cmd, "table", null);
		setDB = getOption(cmd, "setDB", null);
		showDB = cmd.hasOption("showDB");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("setup", false, "Perform default setup"));
		options.addOption(new Option("setDB", true, "Switch to a different database"));
		options.addOption(new Option("showDB", false, "Show current database in use"));
		options.addOption(new Option("table", true,  "Add a specific non-biosql table or table mod, use -table list for list of tables"));
	}
	
	public Options getOptions(){
		return this.options;
	}

	public void run(){
		setCompleteState(TaskState.STARTED);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(testmode){
			runTest();
			return;
		}
		if(showDB || setDB != null){
				System.out.println("------");
				System.out.println("Current Database is:");
				System.out.println(ui.getPropertyLoader().getValue(EddieProperty.DBNAME.toString()));
				if(setDB != null){
					ui.getPropertyLoader().setValue(EddieProperty.DBNAME.toString(), setDB);
					ui.getPropertyLoader().update();
					System.out.println("Now set to:");
					System.out.println(setDB);
				}
				System.out.println("------");
		}
		if(table != null && table.contentEquals("list") ){
			System.out.println("---List of Tables Available---");
			System.out.println("legacy");
			//System.out.println("synonym");
			System.out.println("xrefs");
			System.out.println("assembly");
			System.out.println("run");
			System.out.println("dbxtax");
			System.out.println("bioentry_run");
			System.out.println("---								 ---");
			return;
		}
		if(table != null || setup){
			DatabaseManager manager = this.ui.getDatabaseManager(password);
			try{
				if(!manager.open()){
					logger.error("Failed to create Database...Terminating.");
					return;
				}
				if(setup){
					logger.debug("Setting up database...");
					setup(manager);
					logger.info("Done setting Up");
				}
				else if(table != null){
					int biodatabase_id = manager.getEddieDBID();
					if(biodatabase_id > -1){
						logger.debug("Identifying table...");
						if(createDefaultTable(manager, table)){
							logger.info("Successfully setup table or at least listed tables to be setup.");
						}
						else{
							logger.info("Failed to setup table");
						}
					}
					else{
						logger.error("Failed to get the database_id for Eddie :(");
					}
				}
				//Other tasks here
				else{
					logger.warn("No option set");
				}
				manager.close();
			}
			catch(Exception e){
				logger.error("Error occured", e);
				this.setCompleteState(TaskState.ERROR);
			}
			
		}
		else{
			logger.info("No option selected");
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}

	
	public static boolean canProceedwithSetup(DatabaseManager manager) throws Exception{
		manager.open();
		int j = manager.getTableCount();
		Logger.getRootLogger().debug("Database contains " + j + " tables");
		if(j > 0){
			double d = manager.getBioSQLXT().getDatabaseVersion(manager);
			if(d < 0){
				if(manager.getBioSQLXT().getEddieFromDatabase(manager) > 0){
					Logger.getRootLogger().warn("Eddie uses this database, but has not added a the  info table ??? >:S");
					return true;
				}
				else{
					Logger.getRootLogger().error("This database has not been used by Eddie, run an integration script if they've been written yet?");
					return false;
				}
			}
			else if(d < DatabaseManager.getDatabaseversion()){
				Logger.getRootLogger().error("Database version is too old v"+d+", run update to v"+DatabaseManager.getDatabaseversion()+" if available or manually remove database");
				return false;
			}
			else{
				return true;
			}
		}
		else{
			Logger.getRootLogger().debug("0 Tables found, building database");
			manager.getBioSQL().buildDatabase(manager.getCon());
			return true;
		}
	}
	
	public static boolean setup(DatabaseManager manager) throws Exception{
		if(canProceedwithSetup(manager)){
			Logger.getRootLogger().debug("Extending database with Eddie additions");
			BioSQLExtended bsxt = manager.getBioSQLXT();
			bsxt.addLegacyVersionTable(manager,new String(EddiePropertyLoader.getFullVersion()+""), new String(DatabaseManager.getDatabaseversion()+""));
			//bsxt.addBioEntrySynonymTable(manager);
			bsxt.addBioentryDbxrefCols(manager);
			bsxt.addRunTable(manager);
			bsxt.addRunBioentryTable(manager);
			bsxt.addDbxTaxons(manager);
			return bsxt.addAssemblyTable(manager);
		}
		else return false;
	}

	public boolean createDefaultTable(DatabaseManager manager, String table) throws Exception{
		if(table.contentEquals("legacy")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addLegacyVersionTable(manager,new String(EddiePropertyLoader.getFullVersion()+""), new String(DatabaseManager.getDatabaseversion()+""));
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
		else if(table.contentEquals("dbxtax")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addDbxTaxons(manager);
			}
		}
		else if(table.contentEquals("assembly")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addAssemblyTable(manager);
			}
		}
		else if(table.contentEquals("run")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addRunTable(manager);
			}
		}
		else if(table.contentEquals("bioentry_run")){
			if(manager.isOpen()){
				return manager.getBioSQLXT().addRunBioentryTable(manager);
			}
		}
		else{
			logger.warn("Table: " + table + " not recognised as a table used by Eddie");
			return false;
		}
		return false;
	}
	
}
