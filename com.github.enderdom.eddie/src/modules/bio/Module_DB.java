package modules.bio;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import gui.EddieGUI;
import gui.utilities.PropertyFrame;
import gui.utilities.TableInsertFrame;


import tasks.database.Task_AddRunData;
import tasks.database.Task_Assembly2DB;
import tasks.database.Task_BioSQLDB;
import tasks.database.Task_Blast;
import tasks.database.Task_dbTools;
import tools.Tools_Array;
import tools.Tools_Modules;
import cli.EddieCLI;
import databases.manager.DatabaseManager;
import modules.Module_Basic;

public class Module_DB extends Module_Basic {

	
	private Logger logger = Logger.getRootLogger();
	// Change to Options ...?
	protected String[] tasks = new String[] { "sqladmin", "sqluploader",
			"uploadblast", "dbtools", "uploadrun"};
	protected String[] taskinfo = new String[] {
			"Builds/Modifies the Default bioSQL Database for Eddie",
			"Upload Stuff to the Database",
			"Upload blast hit data to database",
			"	Tools for pulling various data from database", 
			"Manually add a program run reference to database" };

	public String menustring = "Database";
	private static String[] dbactions = new String[] { "Add Program Run...",
			"Run Database Setup", "Database Properties" };

	/*
	 * This needs to match the class -> Will not be changed if class name is
	 * changed!!!
	 */
	protected String[] classes = new String[] { Task_BioSQLDB.class.getName(),
			Task_Assembly2DB.class.getName(), Task_Blast.class.getName(),
			Task_dbTools.class.getName(), Task_AddRunData.class.getName()};

	protected String[] actions;
	
	protected boolean persistance = false;
	
	public static int ADD_PROGRAM = 0;
	public static int SETUP_DATABASE = 1;
	public static int DATABASE_PROPS = 2;
	public static String[] autopop = {"runtype", "program", "version", "dbname"};
	
	
	public Module_DB() {
		modulename = Module_DB.class.getName();
		setActions(new String[]{modulename+PropertyFrame.props_close, modulename+PropertyFrame.props_save} );
	}

	//Remember, not persistant, so none of the classes variables are initialised
	public void actOnAction(String s, EddieGUI gui) {
		boolean cont = true;
		
		if(s.contentEquals(getModuleName() + ADD_PROGRAM)){
			logger.debug("Action not hooked in");
			DatabaseManager manager = gui.getDatabaseManager();
			if(!manager.isOpen())manager.open();
			if(manager.isOpen()){
				TableInsertFrame frame = new TableInsertFrame("Insert Program Run Data", 
						gui, gui.getDatabaseManager().getBioSQLXT().getRunInsert(), autopop);
				gui.setTableInsertFrame(frame);
			}
			else{
				logger.error("Cannot connect to database");
			}
		}
		else if(s.contentEquals(getModuleName() + SETUP_DATABASE)){
			if(gui.getDatabaseManager() == null){
				String p = gui.requiresUserPassword("Please enter the password for the database.", "Database Password");
				if(p != null && p.length() > 0){
					cont = (gui.getDatabaseManager(p).getCon() != null);
				}
				else {
					cont = false;
					gui.sendAlert("Failed to establish connection to database, this may be due to incorrect password, check logs");
				}
			}
			if(cont){
				DatabaseManager dbman= gui.getDatabaseManager();
				if(!dbman.isOpen())dbman.open();
				if(dbman.isOpen()){
					double version = dbman.getBioSQLXT().getDatabaseVersion(dbman);
					//TODO better diagnostic mechanism, check if all expected tables are there
					if(version == -1){
						Task_BioSQLDB.setup(gui.getDatabaseManager());
					}
					else if(version != DatabaseManager.getDatabaseversion()){
						gui.sendAlert("Database is not correct version, but as yet there is no update mechanism," +
								" so revert to an older version of Eddie or DROP/CREATE a new database");
						//TODO update
					}
					else{
						int y = gui.requiresUserYNI("The database appears to already be setup, do you want to try anyway?", "User Action required");
						if(y == JOptionPane.YES_OPTION){
							Task_BioSQLDB.setup(gui.getDatabaseManager());
						}
					}
				}
			}
		}
		else if(s.contentEquals(getModuleName() + DATABASE_PROPS)){
			logger.debug("About to generate properties box");
			if(gui.getDatabaseManager() != null){
				PropertyFrame propsframe = new PropertyFrame();
				propsframe.build(this.modulename, gui, gui.getPropertyLoader().getFullDBsettings());
				gui.setPropertyFrame(propsframe);
			}
			else{
				
			}
		}//TODO issue, with propsframe being lost when this module is killed (not persistant)
		else if(s.contentEquals(modulename+PropertyFrame.props_close)){
			Logger.getRootLogger().debug("Closing General Properties Without Saving");
			if(gui.getPropertyFrame() != null)gui.getPropertyFrame().dispose();
		}
		else if(s.contentEquals(modulename+PropertyFrame.props_save)){
			Logger.getRootLogger().debug("Closing General Properties and Saving");
			String[][] states = gui.getPropertyLoader().getFullDBsettings();
			for(int i =0; i< states[0].length;i++){
				logger.debug("Setting Property "+states[0][i]+" to " + gui.getPropertyFrame().getInput(i));
				gui.getPropertyLoader().setPropertyValue(states[0][i], gui.getPropertyFrame().getInput(i));
			}
			if(gui.getPropertyFrame() != null)gui.getPropertyFrame().dispose();
		}
		else{
			Logger.getRootLogger().debug("Unattached action " + s);
		}
	}

	public void addToGui(EddieGUI biodesktopgui) {
		JMenuBar menu = biodesktopgui.getJMenuBar();
		build(menu, biodesktopgui);
	}

	public void build(JMenuBar menubar, EddieGUI gui) {
		
		String[] action1 = new String[dbactions.length];
		for (int i = 0; i < dbactions.length; i++) {
			JMenuItem menuItem = new JMenuItem(dbactions[i]);
			menuItem.setActionCommand(getModuleName() + i);
			action1[i] = getModuleName() + i;
			menuItem.addActionListener(gui);
			Tools_Modules
					.add2JMenuBar(menubar, menuItem, getMenuString(), true);
		}
		setActions(action1);
	}

	public void addToCli(EddieCLI cli) {

	}

	public String getMenuString() {
		return this.menustring;
	}

	public String getMenuItemName() {
		return this.menuItemName;
	}

	public String[] getTasks() {
		return tasks;
	}

	public void setTasks(String[] tasks) {
		this.tasks = tasks;
	}

	public String[] getTaskinfo() {
		return taskinfo;
	}

	public void setTaskinfo(String[] taskinfo) {
		this.taskinfo = taskinfo;
	}

	public String[] getClasses() {
		return classes;
	}

	public void setClasses(String[] classes) {
		this.classes = classes;
	}

	public String getModuleName() {
		return this.getClass().getName();
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}

	public void spawnAddRunFrame() {
		
	}
	
	public String[] getActions(){
		return this.actions;
	}
	
	public void setActions(String[] actions1) {
		if(actions == null)this.actions = actions1;
		else this.actions = Tools_Array.mergeStrings(this.actions, actions1);
	}

}
