	package modules.bio;

import org.apache.log4j.Logger;

import tasks.bio.Task_Assembly;
import tasks.bio.Task_BlastLocal;
import tasks.bio.Task_Fasta;
import gui.EddieGUI;
import cli.EddieCLI;

import modules.Module_Basic;

public class Module_Bio extends Module_Basic{
	
	public String menustring = "Tools";
	public String menuItemName = "Biology Tools";
	protected String[] tasks = new String[]{"convertFasta","blast", "webblast", "blast2sql","sam2ace", "ace2sam", "aceAnalysis"}; 
	protected String[] taskinfo = new String[]{"converts fasta & qual to fastq","run a blast program", "run web blast", 
			"Upload blast results to mysql db","converts SAM file to ACE file", "converts ACE file to SAM file", "Analysis of ACE files"};
	
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{Task_Fasta.class.getName(),Task_BlastLocal.class.getName(),Task_BlastLocal.class.getName(),
			Task_BlastLocal.class.getName(),Task_Assembly.class.getName(),Task_Assembly.class.getName(),Task_Assembly.class.getName()};
	
	protected String[] actions;
	
	public Module_Bio(){
		
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
}
