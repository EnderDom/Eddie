package modules.bio;

import gui.EddieGUI;
import modules.ModuleBasic;

public class Module_Assembly extends ModuleBasic {
	String modulename = "MOD_modules.bio.Module_Fasta";
	public String menustring = "Tools";
	public String menuItemName = "Assembly Tools";
	protected String[] tasks = new String[]{"sam2ace", "ace2sam"}; 
	protected String[] taskinfo = new String[]{"converts SAM file to ACE file", "converts ACE file to SAM file"};
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{"tasks.bio.assembly."};
	
	public Module_Assembly(){
		
	}
	
	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(actions[0])){

		}
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
