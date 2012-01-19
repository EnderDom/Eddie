package cli;

import java.util.HashMap;
import java.util.Scanner;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import modules.Module;
import tasks.Task;
import tools.UITools;
import tools.funTools;
import tools.stringTools;
import ui.ModuleLoader;
import ui.PropertyLoader;
import ui.TaskManager;
import ui.UI;

public class EddieCLI implements UI {

	PropertyLoader load;
	ModuleLoader modular;
	Module modules[];
	TaskManager manager;
	String[] args;
	Options options;
	HashMap<String, String> tasklist;
	
	public  EddieCLI(PropertyLoader loader, boolean persist){
		System.out.println("Eddie CLI v" + PropertyLoader.version);
		load = loader;
		load.loadPropertiesCLI();
		
		tasklist = new HashMap<String, String>();
		/*
		 * Builds basic options, primarily grabbing "-task"
		 * Needs to be done before parseFurther is run
		 */
		options = new Options();
		buildOptions();
		
		modular = new ModuleLoader(load.getModuleFolder());
		/*
		 * ModuleLoader and PropertyLoader implements Module interface
		 * So they can add relevant parts to interface
		 */
		modules = new Module[]{load, modular};
		/*
		 * Extend GUI with more peripheral modules
		 */
        modules = modular.addModules(modules);
        
        /*
         * Adds relevant stuff to the Object
         */
        for(int i =0; i < modules.length; i++)modules[i].addToCli(this);
        
        parseFurther(args);
        
        if(persist){
        	persist();
        }
	}
	
	private void persist(){
		Logger.getRootLogger().debug("Eddie Persisting Command Line Interface");
		System.out.println("/******************/");
		System.out.println("/    SHARE AND ENJOY    /");
		System.out.println("/******************/");
		
		System.out.println("Close at any time by inputing: exit");
		System.out.println("Control-C will usually close command line programs if you run into trouble");
		String response = "";
		Scanner sc = new Scanner(System.in);
		//Tron protects the user...
		String user = System.getProperty("user.name");
		System.out.print(user+">");
		while(sc.hasNext()){
			response = sc.next();
			if(response.contentEquals("exit")){
				System.out.println("Eddie>"+funTools.getExitMessage());
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
				String task = tasklist.get(cmd.getOptionValue("task"));
				int owner = -1;
				for(int i =0; i < modules.length; i++)if(modules[i].ownsThisTask(task)) owner =i;
				if(owner!=-1)modules[owner].actOnTask(task);
				else {
					Logger.getRootLogger().error("No Tasks with the name "+task+" available");
					printTaskList();
				}
			}
			else{
				printTaskList();
			}
		} catch (ParseException e) {
			Logger.getRootLogger().error("Failed To Parse Input Options", e);
		}
	}
	
	public void setArgs(String[] args){
		this.args = args;
	}
	

	public void exit() {
		// TODO Auto-generated method stub
		
	}

	public void addTask(Task task) {
		// TODO Auto-generated method stub
		
	}

	public void buildTaskManager() {
		this.manager = UITools.buildTaskManager(stringTools.parseString2Int(load.getCore()),  stringTools.parseString2Int(load.getAuxil()));
	}
	
	public void buildOptions(){
		options.addOption(new Option("task", false, "Select a task to run"));
	}

	public void printTaskList(){
		System.out.println("List of tasks available:");
		for(String key : tasklist.keySet()){
			System.out.println(key +"		" + tasklist.get(key));
		}
		
		System.out.println("include after -task");
		System.out.println("Example: -task taskname -arg1 one -arg2 two");
	}
	
}
