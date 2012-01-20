package modules.bio;

import tasks.bio.fasta.fastaConverter;
import gui.EddieGUI;
import cli.EddieCLI;

import modules.ModuleBasic;

public class fastaTools extends ModuleBasic{
	
	String modulename = "MOD_modules.bio.fastaTools";
	public String menustring = "Tools";
	public String menuItemName = "Fasta Tools";
	protected String[] tasks = new String[]{"convert"}; 
	protected String[] taskinfo = new String[]{"converts fasta & qual to fastq"};
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{"tasks.bio.fasta.fastaConverter"};
	
	public fastaTools(){
		
	}
	
	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(actions[0])){
			gui.addTask(new fastaConverter());
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
