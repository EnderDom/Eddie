package modules.bio;

import org.apache.log4j.Logger;
import tasks.bio.Task_Assembly;
import tasks.bio.Task_Assembly_Convert;
import tasks.bio.Task_BlastAnalysis;
import tasks.bio.Task_BlastLocal;
import tasks.bio.Task_ChimeraAnalysis;
import tasks.bio.Task_Fasta_Convert;
import tasks.bio.Task_Fasta_Tools;
import tasks.bio.Task_Sequence_Analysis;
import tasks.bio.Task_WebInterPro;
import gui.EddieGUI;
import cli.EddieCLI;

import modules.Module_Basic;

public class Module_Bio extends Module_Basic{
	
	public String menustring = "Tools";
	public String menuItemName = "Biology Tools";
	//Change to Options ...?
	protected String[] tasks = new String[]{"convertFasta","blast", "webblast", "blast2sql","assemblyconvert","seqanalysis", "assemblytools", "fastatools", 
			"iprscanweb", "chimera", "blastanalysis"}; 
	protected String[] taskinfo = new String[]{"converts fasta & qual to fastq","	run a blast program", "run web blast", 
			"Upload blast results to mysql db", "converts Assembly Files", "Analysis of Sequence files", "Assembly Tools", "Fasta Tools",
			"Send sequences to IPRScan Web Service", "	Run chimera analysis using ACE and blast files",  "Run analysis using fasta and blast files"};
	
	/*
	 * This needs to match the class ->
	 * Will not be changed if class name is changed!!!
	 */
	protected String[] classes = new String[]{Task_Fasta_Convert.class.getName(),Task_BlastLocal.class.getName(),Task_BlastLocal.class.getName(),
			Task_BlastLocal.class.getName(),Task_Assembly_Convert.class.getName(),Task_Sequence_Analysis.class.getName(), Task_Assembly.class.getName(),
			Task_Fasta_Tools.class.getName(), Task_WebInterPro.class.getName(), Task_ChimeraAnalysis.class.getName(), Task_BlastAnalysis.class.getName()};
	
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
