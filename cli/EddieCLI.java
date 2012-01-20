package cli;

import java.util.Scanner;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import modules.Module;
import tasks.Task;
import tools.uiTools;
import tools.funTools;
import tools.stringTools;
import ui.ModuleLoader;
import ui.PropertyLoader;
import ui.TaskManager;
import ui.UI;

public class EddieCLI implements UI {

	private PropertyLoader load;
	private ModuleLoader modular;
	private Module modules[];
	private TaskManager manager;
	private String[] args;
	private Options options;
	
	public  EddieCLI(PropertyLoader loader, boolean persist){
		System.out.println("Eddie v" + PropertyLoader.version);
		load = loader;
		load.loadPropertiesCLI();
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
		System.out.println("/***********************/");
		System.out.println("/    SHARE AND ENJOY    /");
		System.out.println("/***********************/");
		
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
				int owner = -1;
				String task = cmd.getOptionValue("task");
				if(task != null && task.length() > 0){
					for(int i =0; i < modules.length; i++)if(modules[i].ownsThisTask(task)) owner =i;
					if(owner!=-1){
						modules[owner].actOnTask(task, this);
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
				printTaskList();
			}
		} catch (ParseException e) {
			Logger.getRootLogger().error("Failed To Parse Input Options", e);
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
		if(this.manager == null){
			buildTaskManager();
		}
		if(getArgs() != null)task.parseArgs(getArgs());
		task.parseOpts(this.load.getProps());
		this.manager.addTask(task);
	}

	public void buildTaskManager() {
		this.manager = uiTools.buildTaskManager(stringTools.parseString2Int(load.getCore()),  stringTools.parseString2Int(load.getAuxil()));
	}
	
	public void buildOptions(){
		options.addOption(new Option("task", true, "Select a task to run"));
	}

	public void printTaskList(){
		System.out.println("List of tasks available:");
		System.out.println();
		System.out.println();
		for(int i =0; i < this.modules.length; i++){
			modules[i].printTasks();
		}
		System.out.println();
		System.out.println();
		System.out.println("include after -task");
		System.out.println("Example: -task taskname -arg1 one -arg2 two");
	}

	public void update(Task task) {
		// TODO Auto-generated method stub
		
	}
	
}
