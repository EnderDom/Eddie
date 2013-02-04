package enderdom.eddie.tasks.bio;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACERecord;
import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.fasta.FastaParser;
import enderdom.eddie.bio.interfaces.BioFileType;

import enderdom.eddie.tasks.Checklist;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Blast;
import enderdom.eddie.tools.bio.Tools_Fasta;

public class Task_BlastLocal extends TaskXTwIO{
	
	private String blast_db;
	private String blast_bin;
	private String blast_prg;
	private String blastparams;
	private String workspace;
	protected boolean keepargs = true;
	private HashMap<String, String> sequences;
	int start;
	int blastcomplete;
	private boolean clipname;
	boolean err;
	
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
		if(cmd.hasOption("p"))blastparams=cmd.getOptionValue("p").replaceAll("/_", " ");
		if(cmd.hasOption("clip"))clipname=true;
		if(cmd.hasOption("pf")){
			File fie = new File(cmd.getOptionValue("pf"));
			if(fie.isFile()){
				blastparams = Tools_File.quickRead(fie, false);
			}
			else{
				logger.error("Blast parameter file does not exist");
				blastparams = "";
				err =true;
			}
		}
		if(blastparams == null){
			blastparams +=" -outfmt 5";
		}
		else if(!blastparams.contains("outfmt")){
			blastparams +=" -outfmt 5";
		}
	}
	
	public void parseOpts(Properties props){
		if(blast_db == null){
			blast_db = props.getProperty("BLAST_DB_DIR");
		}
		else{
			if(blast_db.indexOf(Tools_System.getFilepathSeparator()) == -1){
				blast_db = props.getProperty("BLAST_DB_DIR") + blast_db;
			}
		}
		if(blast_bin == null){
			blast_bin = props.getProperty("BLAST_BIN_DIR");
		}
		workspace = props.getProperty("WORKSPACE");
		logger.trace("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("bdb", "blast_db", true, "Blast database"));
		options.getOption("i").setDescription("Input sequence file (Ace/Fast(a/q)");
		options.getOption("o").setDescription("Output folder");
		options.addOption(new Option("bbb", "blast_bin", true, "Specify blast bin directory"));
		options.addOption(new Option("bpr", "blast_prog", true, "Specify blast program"));
		options.addOption(new Option("x", "outputformat", true, "Set Output Format, else defaults to xml"));
		options.addOption(new Option("p", "params", true, "Additional Parameters separate with '/_' not space"));
		options.addOption(new Option("pf", "paramater file", true, "Additional blast Parameters in external file"));
		options.addOption(new Option("filetype", true, "Specify filetype (rather then guessing from ext)"));
		options.addOption(new Option("clip", false, "Clip output file name to whitespace in input"));
	}
	
	public void runTest(){
		if(blastparams == null)blastparams = "";
		File in =new File(input);
		File out =  new File(output);
		if(in.exists() && (out.exists() || overwrite)){
			Tools_Blast.runLocalBlast(in, blast_prg, blast_bin, blast_db, blastparams, out);
		}
		else if(!in.exists()){
			logger.error("Input "+this.input+" does not exist! " );
		}
		else{
			logger.error("Output "+this.output+" exists! Change or set overwrite");
		}
	}

	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		this.checklist = openChecklist(ui);
		if(input != null && output != null && !err){
			File in = new File(input);
			File out = new File(output);
			if(in.isFile() && out.isDirectory() && this.blast_bin !=null && this.blast_db != null && this.blast_prg != null){
				start = out.listFiles().length;
				if(filetype == null)filetype = this.detectFileType(in.getName());
				logger.debug("Filetype is set to " + this.filetype.toString());
				if(filetype == BioFileType.FASTQ || filetype == BioFileType.FASTA){
					//Load Fasta-->
					Fasta fasta = new Fasta();
					if(filetype== BioFileType.FASTQ)fasta.setFastq(true);
					FastaParser parser = new FastaParser(fasta);
					boolean cont = false;
					try {
						if(filetype == BioFileType.FASTQ)parser.parseFastq(in);
						if(filetype == BioFileType.FASTA)parser.parseFasta(in);
						Logger.getRootLogger().debug("File Parsed");
						cont = true;
					}
					catch (Exception e) {
						logger.error("Error parsing Fasta/q file", e);
					}
					if(cont){
						this.sequences = fasta.getSequences();
						if(checklist.inRecovery()){
							trimRecovered(checklist.getData());
						}
						logger.debug("About to start running blasts");
						runAutoBlast(out, checklist);
						int l = out.listFiles().length-start;
						if(l == this.blastcomplete){
							logger.debug("Blast Output matches File increase within folder");
						}
						else{
							logger.error("Post Check: The output folder has less files then estimated. Blasts Run: " + this.blastcomplete + " Change in outfolder since start: "+l);
						}
					}
					checklist.complete();
				}
				else if(filetype == BioFileType.ACE){//TODO test new ACE File More thoroughly
					boolean cont = false;
					try {
						ACEFileParser parser = new ACEFileParser(new FileInputStream(this.input));
						while(parser.hasNext()){
							ACERecord record = (ACERecord) parser.next();
							this.sequences.put(record.getContigName(), record.getConsensus().getSequence());
						}
						Logger.getRootLogger().debug("File Parsed");
						cont = true;
					}
					catch (Exception e) {
						logger.error("Error parsing Fasta/q file", e);
					}
					if(cont){
						
						if(checklist.inRecovery()){
							trimRecovered(checklist.getData());
						}
						logger.debug("About to start running blasts");
						runAutoBlast(out, checklist);
						int l = out.listFiles().length-start;
						if(l == this.blastcomplete){
							logger.debug("Blast Output matches File increase within folder");
						}
						else{
							logger.error("Post Check: The output folder has less files then estimated. Blasts Run: " + this.blastcomplete + " Change in outfolder since start: "+l);
						}
					}
					checklist.complete();
				}
				else{
					logger.error("Filetype " + filetype + " not supported");
				}
			}
			else{
				if(!in.isFile()){
					logger.error("Input is not a file");
				}
				if(!out.isDirectory()){
					logger.error("Output Should be a directory");
				}
				if(this.blast_bin == null){
					logger.error("Blast Binary Directory not set");
				}
				if(this.blast_db == null){
					logger.error("Blast database Directory not set");
				}
				if(this.blast_prg == null){
					logger.error("Blast program not set");
				}
			}
		}
		else{
			logger.error("Null input/output");
			this.printHelpMessage();
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public boolean isKeepArgs(){
		return true;
	}
	
	private void trimRecovered(String[] data){
		int j=0;
		for(int i =0;i < data.length; i++){
			if(sequences.containsKey(data[i])){
				sequences.remove(data[i]);
				j++;
			}
		}
		logger.debug("Removed "+j+" of "+ data.length + " from list, as previously run");
	}
	
	public void runAutoBlast(File output, Checklist list){
		FileWriter fstream = null;
		BufferedWriter out = null;
		File temp = null;
		try{
			temp = File.createTempFile("TempBlast", ".fasta");
		}
		catch(IOException io){
			logger.error("Error saving Fasta to temporary directory");
			temp = new File(workspace +Tools_System.getFilepathSeparator()+"temp.fasta");
		}
		for(String seqname : sequences.keySet()){
			try{
				fstream = new FileWriter(temp, false);
				out = new BufferedWriter(fstream);
				Tools_Fasta.saveFasta(seqname,sequences.get(seqname),out);
				out.close();
				fstream.close();
			}
			catch(IOException io){
				logger.error("Error saving Fasta to temporary directory");
			}
			logger.debug("Saved "+ seqname + " to tempfile");
			String outname = seqname;
			if(outname.indexOf(" ") != -1 && clipname)outname = outname.substring(0, outname.indexOf(" "));
			else if (outname.indexOf(" ") != -1 && !clipname) outname = outname.replaceAll(" ", "_");
			File ou = new File(output.getPath()+Tools_System.getFilepathSeparator()+outname+".xml");
			Tools_Blast.runLocalBlast(temp, this.blast_prg, this.blast_bin, this.blast_db, this.blastparams,ou);
			this.blastcomplete++;
			list.update(seqname);
		}
	}
	
}
