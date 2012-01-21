package tasks.bio.blast;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.systemTools;

public class localBlast extends TaskXT{
	
	private String blast_db;
	private String blast_bin;
	private String blast_prg;
	
//	public void run(){
//		setComplete(started);
//		Logger.getRootLogger().debug("Started running task @ "+systemTools.getDateNow());
//		
//		
//		Logger.getRootLogger().debug("Finished running task @ "+systemTools.getDateNow());
//	    setComplete(finished);
//	}
	

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("bdb"))blast_db=cmd.getOptionValue("bdb");
		if(cmd.hasOption("bbb"))blast_bin=cmd.getOptionValue("bbb");
		if(cmd.hasOption("bpr"))blast_prg=cmd.getOptionValue("bpr");
	}
	
	public void parseOpts(Properties props){
		if(blast_db == null){
			blast_db = props.getProperty("BLAST_DB_DIR");
		}
		else{
			if(blast_db.indexOf(File.pathSeparator) == -1){
				blast_db = props.getProperty("BLAST_DB_DIR") + blast_db;
			}
		}
		if(blast_bin == null){
			blast_bin = props.getProperty("BLAST_BIN_DIR");
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("bdb", "blast_db", true, "Blast database"));
		options.getOption("i").setDescription("Input sequence file");
		options.getOption("o").setDescription("Output folder");
		options.addOption(new Option("bbb", "blast_bin", true, "Specify blast bin directory"));
		options.addOption(new Option("bpr", "blast_prog", true, "Specify blast program"));
	}

}
