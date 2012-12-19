package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.blast.MultiblastParser;
import enderdom.eddie.bio.blast.UniVecBlastObject;
import enderdom.eddie.bio.blast.UniVecRegion;
import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.SequenceList;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.tools.Tools_Web;
import enderdom.eddie.tools.bio.Tools_Bio_File;
import enderdom.eddie.tools.bio.Tools_Blast;
import enderdom.eddie.ui.UI;

public class Task_UniVec extends TaskXTwIO{

	private String uni_db;
	private String blast_bin;
	private String workspace;
	private String xml;
	private String qual;
	private boolean create;
	private static String univecsite = "ftp://ftp.ncbi.nih.gov/pub/UniVec/UniVec";
	private static String univeccom = "makeblastdb -title UniVec -dbtype nucl ";
	private static String strategyfolder = "resources";	
	private static String strategyfile = "univec_strategy";
	private static String key = "UNI_VEC_DB";
	private SequenceList fout;
	
	public Task_UniVec(){
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		/*
		* Check UI is being set, which sometimes happens if
		* I have extended the super class and not overwritten the
		* setUI functions. Must do better.
		*/
		if(this.ui == null)logger.error("UI has not been set for this class, code bug");
		if(!checkUniDB()){
			logger.error("Failed to establish UniVec database");
			return;
		}
		/*
		* Check Input
		*/
		if(input == null){
			logger.error("No input file specified");
			return;
		}
		File file = new File(input);
		if(!file.isFile()){
			logger.error("Input file is not a file");
			return;
		}
		else{
			try {
				if(qual != null &&  new File(qual).isFile()){
					fout = SequenceListFactory.getSequenceList(input, qual);
				}
				else{
					fout = SequenceListFactory.getSequenceList(input);
				}
				this.filetype = fout.getFileType();
			} catch (Exception e) {
				logger.error("Failed to parse input file",e);
				return;
			}
		}
		/*
		* Check Output
		*/
		if(output == null){
			output = workspace + Tools_System.getFilepathSeparator()+
					"out" + Tools_System.getFilepathSeparator();
		}
		File dir = new File(output);
		if(dir.isFile()){
			logger.warn("File named out present in folder ...ugh...");
			Tools_File.justMoveFileSomewhere(dir);
			dir = new File(output);
		}
		dir.mkdirs();

		if(xml == null){
			String outname = file.getName();
			int e =-1;
			if((e=outname.lastIndexOf(".")) != -1)outname = outname.substring(0, e);
			e=0;
			File out;
			while((out=new File(dir.getPath()+Tools_System.getFilepathSeparator()+outname+e+".xml")).exists())e++;
	
			/*
			* Build univec strategy file
			*/
			String strat = this.workspace + Tools_System.getFilepathSeparator()
					+strategyfolder+Tools_System.getFilepathSeparator()+ strategyfile +".asn";
			if(!new File(strat).exists()){
				if(!generateStrategyFile(strat))return;
			}
			
	
			/*
			* Actually run the blast program
			*
			* See http://www.ncbi.nlm.nih.gov/VecScreen/VecScreen_docs.html for specs on vecscreen
			*
			*/
			StringBuffer[] arr = Tools_Blast.runLocalBlast(file, "blastn", blast_bin, uni_db, "-import_search_strategy "+strat+" -outfmt 5 ", out);
			if(arr[0].length() > 0){
				logger.info("blastn output:"+Tools_System.getNewline()+arr[0].toString().trim());
			}
			if(arr[1].length() > 0){
				logger.info("blastn output:"+Tools_System.getNewline()+arr[0].toString().trim());
			}
			if(out.isFile()){
				xml = out.getPath();
			}
			else{
				logger.error("Search ran, but no outfile found at " + out.getPath());
				return;
			}
		}
		if(xml != null){
			File xm = new File(xml);
			if(xm.isFile()){
				parseBlastAndTrim(xm, fout, output, this.filetype);
			}
			else if(xm.isDirectory()){
				File[] files = xm.listFiles();
				int i =0;
				for(File f : files){
					if(Tools_Bio_File.detectFileType(f.getPath())==BioFileType.BLAST_XML){
						parseBlastAndTrim(f, fout, output, this.filetype);
						i++;
					}
				}
				if(i==0){
					logger.warn("No blast files in folder, attempting to try plain XML");
					for(File f: files){
						if(Tools_Bio_File.detectFileType(f.getPath())==BioFileType.XML){
							i++;
						}
					}
				}
			}
			else{
				logger.error("Error loading XML file");
			}
		}

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
		options.addOption(new Option("x","xml", true, "Skip running univec search and import previous blast xml"));
		options.addOption(new Option("q","qual", true, "Include quality file, this will also be trimmed"));
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
		if(cmd.hasOption("x"))xml=cmd.getOptionValue("x");
		if(cmd.hasOption("q"))qual=cmd.getOptionValue("q");
	}
	
	
	public static String[] parseBlastAndTrim(File xml, SequenceList seql, String outputfolder, BioFileType filetype){
		try {
			return parseBlastAndTrim(new MultiblastParser(MultiblastParser.UNIVEC, xml), seql, outputfolder, filetype);
		} 
		catch (Exception e) {
			Logger.getRootLogger().error("Error trimming file ", e);
			return null;
		}
	}
	
	public static String[] parseBlastAndTrim(MultiblastParser parser, SequenceList seql, String outputfolder, BioFileType filetype) throws Exception{
		SequenceList list = SequenceListFactory.buildSequenceList(filetype);
		while(parser.hasNext()){
			UniVecBlastObject obj = (UniVecBlastObject) parser.next();
			obj.reverseOrder();
			for(UniVecRegion r : obj.getRegions()){
				if(r.isLeftTerminal()){
					SequenceObject o = seql.getSequence(obj.getBlastTagContents("Iteration_query-def"));
					o.leftTrim(r.getStop(0));
					list.addSequenceObject(o);
				}
				else if(r.isRightTerminal()){
					SequenceObject o = seql.getSequence(obj.getBlastTagContents("Iteration_query-def"));
					o.rightTrim(o.getLength()-r.getStart(0));
					list.addSequenceObject(o);
				}
				else{
					Logger.getRootLogger().error("Not yet implemented mid section");
				}
			}
		}
		String name = outputfolder+Tools_System.getFilepathSeparator();
		name += seql.getFileName() !=null ? seql.getFileName()+"_trimmed" : "out_trimmed";  
		return seql.saveFile(new File(name), filetype);
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
	
	/**
	 * Method requires external execution of 'makeblastdb'
	 * program which should be available at blast_bin
	 * 
	 * @param filepath path which data is downloaded to. 
	 * Note: file may not actually appear at this location,
	 * data is downloaded to filepath + ".fasta" and 
	 * database generated should be ".nhr"/".nin" though
	 * this may change depending on size. 
	 * @return true if database is both downloaded and makeblastdb is
	 * run and the resulting files output are detected.
	 */
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
	
	public boolean generateStrategyFile(String strat){
		String resource = this.getClass().getPackage().getName();
		resource=resource.replaceAll("\\.", "/");
		resource = "/"+resource+"/"+strategyfolder+"/"+strategyfile;
		logger.debug("Creating resource from internal file at "+resource);
		//Create inputstream from resource
		InputStream str = this.getClass().getResourceAsStream(resource);
		//Check if it is null
		if(str == null){
			logger.error("Failed to create strategy file resource, please send bug to maintainer");
			return false;
		}
		//Generate folders for the strategy file
		File tmpfolder = new File(this.workspace + Tools_System.getFilepathSeparator()+strategyfolder);
		if(!tmpfolder.exists())tmpfolder.mkdirs();
		//Write to file
		if(Tools_File.stream2File(str, strat)){
			logger.error("Failed to create search strategy file at " + strat);
			return false;
		}
		else{
			return true;
		}
	}
}

