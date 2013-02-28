package enderdom.eddie.tasks.bio;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FilenameUtils;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

import enderdom.eddie.tasks.BasicTaskStack;
import enderdom.eddie.tasks.Checklist;
import enderdom.eddie.tasks.TaskStack;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tasks.subtasks.SubTask_Blast;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Blast;
import enderdom.eddie.ui.UI;

public class Task_BlastLocal extends TaskXTwIO{
	
	private String blast_db;
	private String blast_bin;
	private String blast_prg;
	private String blastparams;
	protected boolean keepargs = true;
	SequenceList sequences;
	int start;
	int blastcomplete;
	private boolean clipname;
	boolean err;
	private boolean remote;
	
	public Task_BlastLocal(){
		/*
		 * Blast is force set to core
		 * due to it being high cpu usage
		 */
		setCore(true);
	}
	

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("remote"))remote=true;
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
		options.addOption(new Option("p", "params", true, "Additional Parameters separate with '/_' not space, ie -num_threads/_3/_-evalue/_1e-3"));
		options.addOption(new Option("pf", "paramater file", true, "Additional blast Parameters in external file"));
		options.addOption(new Option("filetype", true, "Specify filetype (rather then guessing from ext)"));
		options.addOption(new Option("clip", false, "Clip output file name to whitespace in input"));
		options.addOption(new Option("remote", false, "Run remote blasts in parallel with local (WARN: Will send NCBI blast jobs)"));
	}
	
	public void runTest(){
		if(blastparams == null)blastparams = "";
		File in =new File(input);
		File out =  new File(output);
		if(in.exists() && (out.exists() || overwrite)){
			Tools_Blast.runLocalBlast(in, blast_prg, blast_bin, blast_db, blastparams, out, true);
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
				try{
					this.sequences = SequenceListFactory.getSequenceList(input); 
					if(checklist.inRecovery()){
						trimRecovered(checklist.getData());
					}
					logger.debug("About to start running blasts");
					runAutoBlast(out, checklist);
				}
				catch(IOException io){
					logger.error(io);
				} catch (UnsupportedTypeException e) {
					logger.error(e);
				}
					
				
			}
			else{
				logger.error("Check that in is file, out is directory and blast_bin/db/prg is set");
			}
		}
		else{
			logger.error("Null input/output");
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
			if(sequences.getSequence(data[i]) != null){
				sequences.removeSequenceObject(data[i]);
				j++;
			}
		}
		logger.debug("Removed "+j+" of "+ data.length + " from list, as previously run");
	}
	
	public void runAutoBlast(File output, Checklist list){
		if(BlastProgramEnum.valueOf(blast_prg) == null){
			logger.warn("Are you sure " + blast_prg + " is a program?");
		}	
		
		//TODO add option to use central server, thus 'sort of' allow parallelisation
		TaskStack stack = new BasicTaskStack();
		int i=0;
		for(String s : sequences.keySet()){
			stack.push(s);
			i++;
		}
		logger.debug(i+" sequences to blast");
		
		SubTask_Blast blast = new SubTask_Blast(sequences, ui.requisitionTasker(), false, list, this.output, this.clipname);
		blast.setBlastDetails(blast_prg, blast_bin, blast_db, blastparams);
		blast.setCore(true);
		ui.addTaskLike(blast);
		if(remote){
			SubTask_Blast blast2 = new SubTask_Blast(sequences, ui.requisitionTasker(), false, list, this.output, this.clipname);
			String db = FilenameUtils.getBaseName(blast_db);
			logger.info("Database was trimmed to " + blast_db + " for the remote");
			blast.setBlastDetails(blast_prg, blast_bin, db, blastparams);
			blast.setCore(false);
			ui.addTaskLike(blast2);
		}
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
}
