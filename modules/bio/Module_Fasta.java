package modules.bio;

import tasks.Task;
import tasks.bio.Task_Fasta;
import gui.EddieGUI;
import gui.FileAdderer;
import cli.EddieCLI;

import modules.ModuleBasic;

public class Module_Fasta extends ModuleBasic{
	
	String modulename = "MOD_modules.bio.Module_Fasta";
	public String menustring = "Tools";
	public String menuItemName = "Fasta Tools";
	protected String[] tasks = new String[]{"convertFasta"}; 
	protected String[] taskinfo = new String[]{"converts fasta & qual to fastq"};
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{"tasks.bio.Task_Fasta"};
	
	public Module_Fasta(){
		
	}
	
	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(actions[0])){
			
			FileAdderer fileinput = new FileAdderer(gui);
			
			gui.add2Desktop(fileinput);
			
			Task task = new Task_Fasta();
			
			gui.addTask(task);
		}
	}
	
	public void addToCli(EddieCLI cli) {
		
	}
	
	public String getMenuString(){
		return this.menustring;
	}
	
	public String getMenuItemName(){
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
	
	
	
}
