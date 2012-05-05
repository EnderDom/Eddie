package cli;

import java.util.Scanner;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

import tasks.Task;
import tools.Tools_CLI;
import tools.Tools_UI;
import tools.Tools_Fun;
import tools.Tools_String;
import ui.ModuleManager;
import ui.PropertyLoader;
import ui.TaskManager;
import ui.UI;
import ui.UIEvent;
import ui.UIEventListener;

public class EddieCLI implements UI {

	private PropertyLoader load;
	private ModuleManager modmanager;
	private TaskManager manager;
	private DatabaseManager dbmanager;
	private String[] args;
	private Options options;
	
	public  EddieCLI(PropertyLoader loader, boolean persist){
		System.out.println("Eddie v" + (PropertyLoader.engineversion+PropertyLoader.guiversion) + " by (S.C.Corp.)");
		load = loader;
		load.loadPropertiesCLI();
		/*
		 * Builds basic options, primarily grabbing "-task"
		 * Needs to be done before parseFurther is run
		 */
		options = new Options();
		buildOptions();
		
		//Module Build
		modmanager = new ModuleManager(load.getModuleFolder());
		modmanager.init();
		modmanager.setupCLI(this);
		modmanager.addPrebuiltModule("PROPERTYLOADER", load, this);
		modmanager.addPrebuiltModule("MYSELF", modmanager, this);

        /*
         * Adds relevant stuff to the Object
         */
        
        parseFurther(args);
        
        if(persist){
        	persist();
        }
	}
	
	private void persist(){
		Logger.getRootLogger().debug("Eddie Persisting Command Line Interface");
		System.out.println("/***********************/");
		System.out.println("/    SHARE AND ENJOY    /");
		System.out.println("/***********************/");
		
		System.out.println("Close at any time by inputing: exit");
		System.out.println("Control-C will usually close command line programs if you run into trouble");
		String response = "";
		Scanner sc = new Scanner(System.in);
		String user = System.getProperty("user.name");
		System.out.print(user+">");
		while(sc.hasNext()){
			response = sc.next();
			if(response.contentEquals("exit")){
				System.out.println("Eddie>"+Tools_Fun.getFunnyMessage());
				break;
			}
			else{
				//TODO parse tasks
				System.out.println("Eddie> err... hmm...");
			}
			System.out.print(user+">");
		}
	}
	
	private void parseFurther(String[] args) {
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("task")){
				String task = cmd.getOptionValue("task");
				if(task != null && task.length() > 0){
					logger.trace("Retrieved Task Class "+ task);
					if(modmanager.isTask(task)){
						modmanager.runTask(this,task);
					}
					else {
						Logger.getRootLogger().error("No Tasks with the name "+task+" available");
						printTaskList();
					}
				}
				else {
					Logger.getRootLogger().error("Empty Task Argument");
					printTaskList();
				}
			}
			else{
				Logger.getRootLogger().trace("No Task Set");
				printTaskList();
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
		task.parseOpts(this.load.getProps());
		this.manager.addTask(task);
	}

	public void buildTaskManager() {
		Integer core = Tools_String.parseString2Int(load.getCore());
		Integer auxil = Tools_String.parseString2Int(load.getAuxil());
		if(core == null){
			core = 1;
			logger.error("Something has gone horribly wrong");
		}
		if(auxil == null){
			auxil =5;
			logger.error("Something has gone horribly wrong");
		}
		this.manager = Tools_UI.buildTaskManager(core, auxil);
	}
	
	public void buildOptions(){
		options.addOption(new Option("task", true, "Select a task to run"));
	}

	public void printTaskList(){
		System.out.println("List of tasks available:");
		System.out.println();
		System.out.println();
		modmanager.printAllTasks();
		System.out.println();
		System.out.println();
		System.out.println("include after -task");
		System.out.println("Example: -task taskname -arg1 one -arg2 two");
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

	public void addUIEventListener(UIEventListener listener) {
		listenerList.add(UIEventListener.class, listener);
	}

	public void removeUIEventListener(UIEventListener listener) {
		listenerList.remove(UIEventListener.class, listener);
	}

	public void fireUIEvent(UIEvent evt) {
		Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==UIEventListener.class) {
                ((UIEventListener)listeners[i+1]).UIEventOccurred(evt);
            }
        }
	}

	public String requiresUserInput(String message, String title) {
		return Tools_CLI.showInternalInputDialog(title, message);
	}
	
	public String requiresUserPassword(String message, String title) {
		return Tools_CLI.showInternalPasswordDialog(message, title);
	}

	public int requiresUserYNI(String message, String title) {
		return Tools_CLI.showInternalConfirmDialog(title, message);
	}

	public PropertyLoader getPropertyLoader() {
		return this.load;
	}
	
	//Returns the default DatabaseManager
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
	
}