package tools.bio;

import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

public class Tools_Assembly {

	private static Logger logger = Logger.getRootLogger();
	
	public static int getSingleRunId(DatabaseManager manager, String prg1, String runtype){
		int[] r= manager.getBioSQLXT().getRunId(manager, prg1, runtype);
		if(r.length > 1){
			logger.warn("Program not yet supporting picking runs where assembler is same, please find run id through database and force set it");
			return -1;
		}
		else if(r.length == 0){
			logger.error("Failed to retrieve any run ids for " + prg1 + " make sure assembly run was added");
			return -1;
		}
		else{
			return r[0];
		}
	}
}
