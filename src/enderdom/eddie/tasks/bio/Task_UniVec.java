package enderdom.eddie.tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.tools.Tools_Web;
import enderdom.eddie.tools.bio.Tools_Blast;
import enderdom.eddie.ui.UI;

public class Task_UniVec extends TaskXTwIO{

	private String uni_db;
	private String blast_bin;
	private String workspace;
	private boolean create;
	private static String univecsite = "ftp://ftp.ncbi.nih.gov/pub/UniVec/UniVec";
	private static String univeccom = "makeblastdb -title UniVec -dbtype nucl ";
	private static String key = "UNI_VEC_DB";
	private String filetype;
	
	public Task_UniVec(){
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(this.ui == null)
		if(!checkUniDB()){
			logger.error("Failed to establish UniVec database");
			return;
		}
		if(input == null){
			logger.error("No input file specified");
			return;
		}
		File file = new File(input);
		if(!file.isFile()){
			logger.error("Input file is not a file");
			return;
		}
		if(filetype == null)filetype = this.detectFileType(file.getName());
		if(output == null){
			output = workspace + Tools_System.getFilepathSeparator()+"out" + Tools_System.getFilepathSeparator();
		}
		File dir = new File(output);
		if(!dir.isDirectory()){
			
			dir = new File(output);
			logger.warn("Output file is not a folder, will save to default out folder " +output);
		}
		String outname = file.getName();
		int e =-1;
		if((e=outname.lastIndexOf(".")) != -1)outname = outname.substring(0, e);
		e=0;
		File out;
		while((out=new File(outname+e+".xml")).exists())e++;
		//See http://www.ncbi.nlm.nih.gov/VecScreen/VecScreen_docs.html for specs on vecscreen
		Tools_Blast.runLocalBlast(file, "blastn", blast_bin, uni_db, "-q -5 -G 3 -E 3 -F \"m D\" -e 700 -Y 1.75e12 ", out);
		
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input sequence file Fast(a/q)");
		options.getOption("o").setDescription("Output folder");
		options.addOption(new Option("u", "uni_db", true, "Set UniVec database location"));
		options.addOption(new Option("c", "create_db", false, "Downloads and creates the UniVec database with the makeblastdb"));
		options.addOption(new Option("bbb", "blast_bin", true, "Specify blast bin directory"));
		options.addOption(new Option("filetype", true, "Specify filetype (rather then guessing from ext)"));
	}
	
	public void parseOpts(Properties props){
		if(blast_bin == null){
			blast_bin = props.getProperty("BLAST_BIN_DIR");
		}
		workspace = props.getProperty("WORKSPACE");
		logger.trace("Parse Options From props");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("u"))uni_db=cmd.getOptionValue("u");
		if(cmd.hasOption("bbb"))blast_bin=cmd.getOptionValue("bbb");
		if(cmd.hasOption("c"))create=true;
		if(cmd.hasOption("i"))input=cmd.getOptionValue("i");
	}
	
	/**
	 * Admittedly a bit of a mess, but hopefully checks
	 * all eventualities.
	 * 
	 * @return true if uni_db is set and exists
	 */
	private boolean checkUniDB(){
		if(uni_db != null && create){
			return createUniVecDb(uni_db);
		}
		else if(create){
			if(createUniVecDb(this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec")){
				uni_db = this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec";
				return true;
			}
			else{
				logger.error("Failed to create univec database, create manually and add location to props");
				return false;
			}
		}
		else if(uni_db != null){
			File f = new File(uni_db);
			if(f.isFile()){
				ui.getPropertyLoader().setValue("key", uni_db);
				return true;
			}
			else{
				logger.error("UniVec database set as " + uni_db + " is not a file, closing");
				return false;
			}
		}
		else if(ui.getPropertyLoader().getValue(key) != null && ui.getPropertyLoader().getValue(key).length() > 0){
			uni_db = ui.getPropertyLoader().getValue(key);
			if(new File(uni_db).isFile() || new File(uni_db + ".nin").exists() || new File(uni_db+".nhr").exists()) return true;
			else return false;
		}
		else{
			logger.warn("No uni-vec set, nor create is set.");
			int value = ui.requiresUserYNI("Do you want to automatically create UniVec data?", "Create Univec Database?");
			if(value == UI.YES){
				if(createUniVecDb(this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec")){
					uni_db = this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec";
					return true;
				}
				else{
					logger.error("Failed to create univec database, create manually and add location to props");
					return false;
				}
			}
			else{
				logger.info("User chose not to create UniVec database");
				return false;
			}
		}
	}
	
	public boolean createUniVecDb(String filepath){
		logger.debug("About to create UniVec database at " + filepath);
		File file = new File(filepath);
		file.getParentFile().mkdirs();
		if(file.exists())logger.warn("Database already exists, overwriting...");
		if(Tools_Web.basicFTP2File(ui.getPropertyLoader().getValueOrSet("UNIVEC_URL", univecsite), filepath+".fasta")){
			StringBuffer univec = new StringBuffer();
			univec.append(blast_bin);
			if(!blast_bin.endsWith(Tools_System.getFilepathSeparator()))univec.append(Tools_System.getFilepathSeparator());
			univec.append("");
			univec.append(univeccom +"-in "+ filepath+ ".fasta -out "+ filepath+ " ");
			StringBuffer[] arr = Tools_Task.runProcess(univec.toString(), true);
			if(arr[0].length() > 0){
				logger.info("makeblastdb output:"+Tools_System.getNewline()+arr[0].toString().trim());
			}
			if(new File(file.getPath() + ".nin").exists() || new File(file.getPath()+".nhr").exists() || file.exists()){
				ui.getPropertyLoader().setValue(key, file.getPath());
				ui.getPropertyLoader().savePropertyFile(ui.getPropertyLoader().getPropertyFilePath(), 
						ui.getPropertyLoader().getPropertyObject());
				return true;
			}
			else return false;
		}
		else return false;
	}
}

