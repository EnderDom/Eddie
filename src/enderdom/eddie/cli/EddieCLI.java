package enderdom.eddie.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.Task;
import enderdom.eddie.tasks.TaskList;
import enderdom.eddie.tools.Tools_CLI;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_UI;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.EddiePropertyLoader;
import enderdom.eddie.ui.TaskManager;
import enderdom.eddie.ui.UI;
import enderdom.eddie.ui.UIEvent;

public class EddieCLI implements UI {

	private EddiePropertyLoader load;
	private TaskManager manager;
	private DatabaseManager dbmanager;
	private String[] args;
	private Options options;
	
	public EddieCLI(EddiePropertyLoader loader){
		System.out.println("Eddie v" + (EddiePropertyLoader.engineversion+EddiePropertyLoader.subversion) + " by (S.C.Corp.)");
		this.load = loader;
		setArgs(loader.args);
		/*
		 * Builds basic options, primarily grabbing "-task"
		 * Needs to be done before parseFurther is run
		 */
		options = new Options();		
		buildOptions();

       /*
         * Adds relevant stuff to the Object
         */
        parseFurther(args);
	}
	
	private void parseFurther(String[] args) {
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("task")){
				String task = cmd.getOptionValue("task");
				if(task != null && task.length() > 0){
					logger.trace("Retrieved Task Class "+ task);
					if(TaskList.isTask(task)){
						TaskList.actOnTask(task, this);
					}
					else if(task.contentEquals("test")){
						printTaskList(true);
					}
					else {
						Logger.getRootLogger().error("No Tasks with the name "+task+" available");
						printTaskList(false);
					}
				}
				else {
					Logger.getRootLogger().error("Empty Task Argument");
					printTaskList(false);
				}
			}
			else{
				Logger.getRootLogger().trace("No Task Set");
				printTaskList(false);
			}
		} catch (ParseException e) {
			logger.error("Failed To Parse Input Options", e);
		}
	}
	
	public void setArgs(String[] args){
		this.args = args;
	}
	
	public String[] getArgs(){
		return this.args;
	}
	

	public void exit() {
		// TODO Auto-generated method stub
		
	}
	
	public void addTask(Task task) {
		Logger.getRootLogger().debug("CLI recieve task, sending to task manager...");
		if(this.manager == null){
			buildTaskManager();
		}
		if(task.wantsUI())task.addUI(this);
		if(getArgs() != null)task.parseArgs(getArgs());
		task.parseOpts(this.load.getPropertyObject());
		this.manager.addTask(task);
	}

	public void buildTaskManager() {
		Integer core = Tools_String.parseString2Int(load.getValueOrSet("CORETHREAD", "1"));
		Integer auxil = Tools_String.parseString2Int(load.getValueOrSet("AUXILTHREAD", "5"));
		if(core == null){
			core = 1;
			logger.error("Something has gone horribly wrong");
		}
		if(auxil == null){
			auxil =5;
			logger.error("Something has gone horribly wrong");
		}
		this.manager = Tools_UI.buildTaskManager(this, core, auxil);
	}
	
	public void buildOptions(){
		options.addOption(new Option("task", true, "Select a task to run"));
	}

	public void printTaskList(boolean test){
		TaskList.printAllTasks();
		System.out.println();
	}

	public void update(Task task) {
		// TODO Auto-generated method stub
		
	}

	public boolean isGUI() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void sendAlert(String str){
		Logger.getRootLogger().info(str);
	}

	public String requiresUserInput(String message, String title) {
		return Tools_CLI.showInternalInputDialog(title, message);
	}
	
	public String requiresUserPassword(String message, String title) {
		return Tools_CLI.showInternalPasswordDialog(message,title);
	}

	public int requiresUserYNI(String message, String title) {
		return Tools_CLI.showInternalConfirmDialog(title, message);
	}

	public PropertyLoader getPropertyLoader() {
		return this.load;
	}
	
	//Returns the default DatabaseManager
	public DatabaseManager getDatabaseManager(String password){
		if(this.dbmanager == null){
			this.dbmanager = new DatabaseManager(this, password);
		}
		return this.dbmanager;
	}
	
	public DatabaseManager getDatabaseManager(){
		if(this.dbmanager == null){
			this.dbmanager = new DatabaseManager(this);
		}
		return this.dbmanager;
	}
	
	public void setDatabaseManager(DatabaseManager dbmanager){
		if(this.dbmanager != null){
			logger.warn("This UI already has a manager, you know jsut warning you");
		}
		this.dbmanager = dbmanager;
	}
	
	public void fireUIEvent(UIEvent evt){
		logger.debug("Foo");
	}
		
	public void throwError(String message, Throwable t){
		logger.error(message,t);
	}
	
	public void error(String message, Throwable t){
		throwError(message, t);
	}
	
	public void error(String message){
		logger.error(message);
	}

	public void throwError(String message, String[] details) {
		logger.error(message);
		for(String s : details)System.out.println(s);
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		load.savePropertyFile(load.getPropertyFilePath(), load.getPropertyObject());
	}
}
