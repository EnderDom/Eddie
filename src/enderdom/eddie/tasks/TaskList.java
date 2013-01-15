package enderdom.eddie.tasks;

import org.apache.log4j.Logger;

import enderdom.eddie.tasks.bio.Task_Assembly;
import enderdom.eddie.tasks.bio.Task_BioTools;
import enderdom.eddie.tasks.bio.Task_BlastAnalysis;
import enderdom.eddie.tasks.bio.Task_BlastLocal;
import enderdom.eddie.tasks.bio.Task_Convert;
import enderdom.eddie.tasks.bio.Task_Fasta_Tools;
import enderdom.eddie.tasks.bio.Task_UniVec;
import enderdom.eddie.tasks.bio.Task_WebInterPro;
import enderdom.eddie.tasks.database.Task_AddRunData;
import enderdom.eddie.tasks.database.Task_Assembly2DB;
import enderdom.eddie.tasks.database.Task_BioSQLDB;
import enderdom.eddie.tasks.database.Task_Blast;
import enderdom.eddie.tasks.database.Task_dbTools;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.ui.UI;

/*
 * Part of my attempt to clear out the cludge of Modules
 * 
 * This class holds all the Tasks classes. I'm really not sure how else
 * to do this. There is a method for searching methods for all classes, but
 * I really don't like the idea. So add all Tasks in variable here and the UI
 * will retrieve them and act appropriately.
 */
public class TaskList {

	private static String[][] tasks;
	private static int taskpad = 30;
	private static int descriptionpad = 50;
	
	/**
	 * @return returns a string matrix of length
	 * [3][x], with the first array containing all Task class names
	 * the second the task call value, and the third a brief description
	 * of the class. The length x may be longer than the list of classes
	 * so a null check will need to be done.
	 * 
	 * Additionally this sets the static value for tasks which is then returned in
	 * all future cases
	 */
	public static String[][] getTasklist(){
		if(tasks != null) return tasks;
		else{
			//0 class, 1 name, 2 description
			tasks = new String[3][30];
			
			//Local Blast
			tasks[0][0] = Task_BlastLocal.class.getName();
			tasks[1][0] = "blast";
			tasks[2][0] = "run a local blast program";
			
			//File Converter
			tasks[0][1] = Task_Convert.class.getName();
			tasks[1][1] = "converter";
			tasks[2][1] = "Convert various bio formats  [WIP]";
			
			//Assembly
			tasks[0][2] = Task_Assembly.class.getName();
			tasks[1][2] = "assemblytools";
			tasks[2][2] = "various assembly tools [WIP]";
			
			//Fasta Tools
			tasks[0][3] = Task_Fasta_Tools.class.getName();
			tasks[1][3] = "fastatools";
			tasks[2][3] = "various fasta tools [WIP]";
			
			//WebInterpro
			tasks[0][4] = Task_WebInterPro.class.getName();
			tasks[1][4] = "iprscanweb";
			tasks[2][4] = "send sequences to iprscan web service";
			
			//BioTools 
			tasks[0][5] = Task_BioTools.class.getName();
			tasks[1][5] = "biotools";
			tasks[2][5] = "general sequence manipulation tools";
			
			//Blast Analysis
			tasks[0][6] = Task_BlastAnalysis.class.getName();
			tasks[1][6] = "blastanalysis";
			tasks[2][6] = "analyse fasta and blast files";
			
			// Empty
			//tasks[0][7] = 
			//tasks[1][7] = 
			//tasks[2][7] = 
			
			//SQL admin tools
			tasks[0][8] = Task_BioSQLDB.class.getName();
			tasks[1][8] = "sqladmin";
			tasks[2][8] = "build/modify the default bioSQL database for Eddie";
			
			//SQL Upload tools
			tasks[0][9] = Task_Assembly2DB.class.getName();
			tasks[1][9] = "sqluploader";
			tasks[2][9] = "upload sequence/assembly data to the SQL database";
	
			//Upload Run Info
			tasks[0][10] = Task_AddRunData.class.getName();
			tasks[1][10] = "uploadrun";
			tasks[2][10] = "add Program run information to database, (needed for other data upload)";
			
			//Blast Upload
			tasks[0][11] = Task_Blast.class.getName();
			tasks[1][11] = "uploadblast";
			tasks[2][11] = "upload blast hit data to database";
			
			//DBtools
			tasks[0][12] = Task_dbTools.class.getName();
			tasks[1][12] = "dbtools";
			tasks[2][12] = "tools for downloading various data from database";
	
			// Empty
			//tasks[0][13] = 
			//tasks[1][13] = 
			//tasks[2][13] =
			
			//Contig Comparison
			tasks[0][14] = Task_UniVec.class.getName();
			tasks[1][14] = "univec";
			tasks[2][14] = "Run UniVec screen on dataset";
			
			//Empty
			//tasks[0][15] = 
			//tasks[1][15] = 
			//tasks[2][15] = 
			
			return tasks;
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return returns true if the task exists in the tasklist,
	 * returns false otherwise
	 */
	public static boolean isTask(String task){
		String[][] tasklist = getTasklist();
		for(int i =0; i < tasklist[1].length; i++){
			if(tasklist[1][i] != null){			
				if(tasklist[1][i].equalsIgnoreCase(task)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param s name of task
	 * @param cli a UI object to attach the task to
	 * @return returns true if successfully created task, else returns false
	 */
	public static boolean actOnTask(String s, UI cli) {
		String[][] tasks = getTasklist();
		Logger.getRootLogger().debug("Task "+ s + " sent");
		for(int i =0; i < tasks[1].length; i++){
			if(tasks[1][i] != null){
				if(s.equalsIgnoreCase(tasks[1][i])){
					try {
						Task t = (Task) Class.forName(tasks[0][i]).getConstructor().newInstance();
						if(t != null){
							cli.addTask(t);
							return true;
						}
					} 					
					catch (Exception e) {
						Logger.getRootLogger().fatal("Task class failed, please report this bug:", e);
						return false;
					}
				}
			}
		}
		return false;
	}
	
	public static void printAllTasks(){
		String[][] tasks = getTasklist();
		StringBuilder build = new StringBuilder();
		System.out.println("--Task List--");
		for(int i =0; i < tasks[1].length; i++){
			if(tasks[1][i] != null){
				build = new StringBuilder();
				build.append(Tools_String.padString("-task "+tasks[1][i] + " [args]", taskpad, false));
				build.append("    ");
				build.append(Tools_String.padString(tasks[2][i], descriptionpad, false));
				System.out.println(build.toString());
			}
		}
		System.out.println("Usage Example: -task taskname -arg1 one -arg2 two");
	}
	

	
	
}
