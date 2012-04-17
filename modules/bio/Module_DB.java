package modules.bio;

import gui.EddieGUI;

import org.apache.log4j.Logger;

import tasks.database.adminBioSQLDB;
import cli.EddieCLI;
import modules.Module_Basic;

public class Module_DB extends Module_Basic{
	
	//Change to Options ...?
	protected String[] tasks = new String[]{"bioSQLadmin"}; 
	protected String[] taskinfo = new String[]{"Builds/Modifies the Default bioSQL Database for Eddie"};
	
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{adminBioSQLDB.class.getName()};
	
	protected String[] actions;
	
	public Module_DB(){
		
	}
	
	
	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(this.getModuleName()+0)){
			Logger.getRootLogger().debug("Unimplemented Actionables");
		}
	}
	
	public void addToGui(EddieGUI biodesktopgui) {

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
