package enderdom.eddie.tasks;

import enderdom.eddie.tasks.bio.Task_Assembly;
import enderdom.eddie.tasks.bio.Task_BioTools;
import enderdom.eddie.tasks.bio.Task_BlastAnalysis;
import enderdom.eddie.tasks.bio.Task_BlastLocal;
import enderdom.eddie.tasks.bio.Task_ChimeraAnalysis;
import enderdom.eddie.tasks.bio.Task_Convert;
import enderdom.eddie.tasks.bio.Task_Fasta_Tools;
import enderdom.eddie.tasks.bio.Task_WebInterPro;
import enderdom.eddie.tasks.database.Task_AddRunData;
import enderdom.eddie.tasks.database.Task_Assembly2DB;
import enderdom.eddie.tasks.database.Task_BioSQLDB;
import enderdom.eddie.tasks.database.Task_Blast;
import enderdom.eddie.tasks.database.Task_dbTools;
import enderdom.eddie.tasks.database.niche.Task_ContigComparison;

/*
 * Part of my attempt to clear out the cludge of Modules
 * 
 * This class holds all the Tasks classes. I'm really not sure how else
 * to do this. There is a method for searching methods for all classes, but
 * I really don't like the idea. So add all Tasks in variable here and the UI
 * will retrieve them and act appropriately.
 */
public class TaskList {

	public static String[][] getTasklist(){
		//0 class, 1 name, 2 description
		String[][] tasks = new String[3][30];
		
		//Local Blast
		tasks[0][0] = Task_BlastLocal.class.getName();
		tasks[1][0] = "blast";
		tasks[2][0] ="	run a local blast program";
		
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
		
		//Chimera Analysis
		tasks[0][7] = Task_ChimeraAnalysis.class.getName();
		tasks[1][7] = "chimera";
		tasks[2][7] = "run chimera analysis using ACE and blast files";
		
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

		//Contig Comparison
		tasks[0][13] = Task_ContigComparison.class.getName();
		tasks[1][13] = "contigcompare";
		tasks[2][13] = "contig comparison analysis [WIP]";
		
		return tasks;
	}
	
}
