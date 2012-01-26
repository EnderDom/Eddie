package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.bio.Tools_Blast;

public class Task_BlastLocal extends TaskXT{
	
	private String blast_db;
	private String blast_bin;
	private String blast_prg;
	private String blastparams;
	
	public Task_BlastLocal(){
		/*
		 * Blast is force set to core
		 * due to it being high cpu usage
		 */
		setCore(true);
	}
	

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
		options.addOption(new Option("p", "params", true, "Additional Parameters"));
	}
	
	public void runTest(){
		if(blastparams == null)blastparams = "";
		/*
		 * TODO remove test
		 * 
		 * But just incase I forget, will only run for me in debug
		 */
		if((System.getProperty("user.name").contains("dominic")) && Logger.getRootLogger().isDebugEnabled()){
			Logger.getRootLogger().debug("\nRUNNING TEST!\n");
			this.input = "/home/dominic/apps/eclipse/testfiles/fasta/test_single.fasta";
			this.output = "/home/dominic/apps/eclipse/testfiles/fasta/test_single_blast";
			this.overwrite = true;
			this.blast_prg = "blastn";
			this.blast_db = blast_db+"deroceras_combi";
		}
		File input = null;
		File output = null;
		if((input = getStdInput()) != null && (output = getStdOutput()) != null){
			Tools_Blast.runLocalBlast(input, blast_prg, blast_bin, blast_db, blastparams, output);
		}
		else if(input == null){
			Logger.getRootLogger().error("Input "+this.input+" does not exist! " );
		}
		else{
			Logger.getRootLogger().error("Output "+this.output+" exists! Change or set overwrite");
		}
	}

}
