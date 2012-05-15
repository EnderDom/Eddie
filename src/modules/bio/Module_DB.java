package modules.bio;

import gui.EddieGUI;

import org.apache.log4j.Logger;

import tasks.database.Task_Assembly2DB;
import tasks.database.Task_BioSQLDB;
import tasks.database.Task_Blast;
import cli.EddieCLI;
import modules.Module_Basic;

public class Module_DB extends Module_Basic{
	
	//Change to Options ...?
	protected String[] tasks = new String[]{"sqladmin", "sqluploader", "uploadblast"}; 
	protected String[] taskinfo = new String[]{"Builds/Modifies the Default bioSQL Database for Eddie", "Upload Stuff to the Database", "Upload blast hit data to database"};
	
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{Task_BioSQLDB.class.getName(), Task_Assembly2DB.class.getName(), Task_Blast.class.getName()};
	
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
