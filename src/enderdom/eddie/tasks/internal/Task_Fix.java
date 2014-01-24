package enderdom.eddie.tasks.internal;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.exceptions.EddieDBException;
import enderdom.eddie.exceptions.EddieGenericException;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.ui.EddieProperty;

@Deprecated
public class Task_Fix extends TaskXTwIO{

	private String prefix;
	private String dbname;
	private String start;
	private String stop;
	private boolean trimdec;
	private String blastcmdpath;
	
	public Task_Fix(){
		
	}
	
	public void parseOpts(Properties props){
		blastcmdpath = props.getProperty(EddieProperty.BLAST_BIN_DIR.toString());
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		prefix = getOption(cmd, "a","gnl|BL_ORD_ID|");
		dbname = getOption(cmd, "b", null);
		start = getOption(cmd, "tf", null);
		stop = getOption(cmd, "tt", null);
		trimdec = cmd.hasOption("td");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.removeOption("w");
		options.removeOption("o");
		options.removeOption("filetype");
		options.getOption("i").setDescription("Filepath of blast database");
		options.addOption(new Option("a","id2accPrefix", true, "Prefix needed to convert acc to id something" +
				" like 'gnl|BL_ORD_ID|' "));
		options.addOption(new Option("b","dbname", true, "dbname used in the "));
		options.addOption(new Option("tf","trimfrom", true, "Trim accession from, default : \"|ref|\""));
		options.addOption(new Option("tt","trimto", true, "Trim accession to default : \"|\""));
		options.addOption(new Option("td","trimdecimal", true, "Trim any trailing decimals ie" +
				" NP_001019769.1 -> NP_001019769"));
	}
	
	public void printHelpMessage(){
		System.out.println("This task pulls accessions loaded in a database which are " +
				"based on local database accesions and renames them based on the accessions " +
				"found 'hopefully in the database entry name' check with blastdbcmd");
		super.printHelpMessage();
		System.out.println("-tt and -tf assume that their are unique indicators between " +
				"your accession, if not... you'll have to work around it yourself");
		System.out.println("-tt and -tf assume that their are unique indicators between " +
				"your accession, if not... you'll have to work around it yourself");
		System.out.println("Example header from blastdbcmd: gnl|BL_ORD_ID|274 gi|66912172|" +
				"ref|NP_001019769.1| transcription factor HES-3 [Homo sapiens]");
		System.out.println("In this case \"gnl|BL_ORD_ID|274 \" is removed");
		System.out.println("gi|66912172|ref|NP_001019769.1| transcription factor HES-3 [Homo sapiens]");
		System.out.println("-tf \"gi|\" -tt \"|\" will pull out the first gi|*| ie 66912172");
		System.out.println("-tf \"|ref|\" -tt \"|\" will pull out the first |ref|*| ie NP_001019769.1");
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		if( input == null){
			logger.error("Set database file locaiton");
			return;
		}
		logger.info("Prefix set to: " + prefix + " and dbname in database is " + dbname);
		logger.info("Trim start and stop set to: " + start + " and " +stop);
		logger.info("Trim decimal is "+trimdec);
			
		try {
			if(manager.open()){
				String[][] strs = manager.getSQLGeneral().getResults(manager.getCon(),
						new String[]{"accession", "dbxref_id"}, "dbxref", new String[]{"dbname", "version"}, 
						new String[]{dbname, "0"});
				logger.info("To change " + strs[0].length + " results");
				String query =null;
				for(int i=0;i < strs[0].length;i++){
					String entry = prefix+strs[0][i];
					query = blastcmdpath+"blastdbcmd -db "+input+" -entry \""+entry+"\"";
					StringBuffer[] ret = Tools_Task.runProcess(query, true, false);
					if(ret[0]!=null){
						String r = ret[0].toString();
						String[] rt = r.split(System.getProperty("line.separator"));
						r = rt[0];
						if(r.indexOf(entry) !=-1){
							try{
							String acc = Tools_String.cutLineBetween(start, stop, r);
							acc =acc.trim();
							int indend;
							if(trimdec && (indend = acc.lastIndexOf('.')) != -1)acc=acc.substring(0, indend);
							manager.getSQLGeneral().update(manager.getCon(), new String[]{"accession","version"},
									new String[]{acc, "1"}, "dbxref", new String[]{"dbxref_id", "version"}, 
									new String[]{strs[1][i], "0"});
							}
							catch(EddieGenericException e){
								logger.error("Failed to properly parse accession form string "+ r);
							}
						}
						else{
							logger.error("failed to find "+prefix+strs[0][i]+" in " + r);
						}
					}
					else{
						System.out.println("Failed: " + ret[0].toString());
						logger.error("Failed to work with "+strs[0][i] + " with dbxref_id " + strs[1][i] );
					}
				}
			}
			else{
				logger.error("Failed to open database");
				return;
			}
		} catch (InstantiationException e) {
			logger.error("Failed to open database", e);
		} catch (IllegalAccessException e) {
			logger.error("Failed to open database", e);
		} catch (ClassNotFoundException e) {
			logger.error("Failed to open database", e);
		} catch (SQLException e) {
			logger.error("Failed to open database", e);
		} catch (EddieDBException e) {
			logger.error("Failed to open database", e);
		} catch (InterruptedException e) {
			logger.error("Failed to open database", e);
		}
		
		logger.info("All changes have set version to 1, once okay, set version back to 0");
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
}
