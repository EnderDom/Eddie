package enderdom.eddie.modules.bio.niche;



import org.apache.log4j.Logger;
import enderdom.eddie.tasks.database.niche.Task_ContigComparison;
import enderdom.eddie.gui.EddieGUI;
import enderdom.eddie.cli.EddieCLI;

import enderdom.eddie.modules.Module_Basic;

public class Module_Niche extends Module_Basic{

	//Change to Options ...?
	protected String[] tasks = new String[]{"contigcompare"}; 
	protected String[] taskinfo = new String[]{"Run the Contig Comparison Analysis"};
	
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{Task_ContigComparison.class.getName()};
	
	protected String[] actions;
	
	public Module_Niche(){
		
	}

	//Action mechanism needs to be changed for non-persistant modules....
	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(this.getModuleName()+0)){
			Logger.getRootLogger().debug("Unimplemented Actionables");
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
	
	public String getModuleName(){
		return this.getClass().getName();
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}
}

