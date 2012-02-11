package modules.bio;


import tasks.bio.Task_BlastLocal;
import modules.Module_Basic;
import gui.EddieGUI;
import cli.EddieCLI;

public class Module_Blast extends Module_Basic{
	String modulename = "MOD_modules.bio.Module_Blast";
	public String menustring = "Tools";
	public String menuItemName = "Blast Tools";
	protected String[] tasks = new String[]{"blast", "webblast", "blast2sql"}; 
	protected String[] taskinfo = new String[]{"run a blast program", "run web blast", "Upload blast results to mysql db"};

	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{Task_BlastLocal.class.getName(),Task_BlastLocal.class.getName(),Task_BlastLocal.class.getName()};
	
	
	
	public Module_Blast(){
		
	}
	
	public void actOnAction(String s, EddieGUI gui) {

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
	public String getModuleName(){
		return this.getClass().getName();
	}
	
}
