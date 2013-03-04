package enderdom.eddie.tasks.bio;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.ui.UI;

public class Task_ESTScan extends TaskXT{
	
	
	private String ESTScanBin;
	private String matrix;
	private boolean bioen;
	private String input;
	private String output;
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))this.ESTScanBin=cmd.getOptionValue("b");
		if(cmd.hasOption("m"))this.matrix=cmd.getOptionValue("m");
		if(cmd.hasOption("e"))this.bioen=true;
		if(cmd.hasOption("i"))this.input = cmd.getOptionValue("i");
		if(cmd.hasOption("o"))this.output = cmd.getOptionValue("o");
	}
	
	public void parseOpts(Properties props){
		logger.debug("Parse Options From props");
		if(ESTScanBin == null){
			ESTScanBin = props.getProperty("ESTSCAN_BIN");
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "ESTScan", true, "ESTScan executable path"));
		options.addOption(new Option("m", "matrix", true, "filepath to matrix file"));
		options.addOption(new Option("e","bioentries", false, "Use bioentry ids for ESTscan !Advised!"));
		options.addOption(new Option("i", "input", true, "Input sequence file Fasta"));
		options.addOption(new Option("o", "output", true, "Output files, suffixes will be added"));
		options.removeOption("w");
		options.removeOption("filetype");
	}
	
	
	public void run() {
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(ESTScanBin != null && matrix != null && output != null){
			if(new File(matrix).isFile()){
				if(bioen){
					logger.info("Building Fasta File...");
					DatabaseManager man = ui.getDatabaseManager();
					man.createAndOpen();
					man.getBioSQLXT().getContigsAsFasta(man, new Fasta(), -1);
					try {
						logger.debug("Writing as temporary file...");
						File in = File.createTempFile("tempfasta", ".fasta");
						input = in.getPath();
						logger.debug("Writing as temporary file to "  + input);
					} catch (IOException e) {
						logger.error(e);
						return;
					}
				}
				else{
					if(input == null){
						logger.error("Input file isn't a file and database settings not set");
						return;
					}
				}
				
				if(this.output.indexOf(".") != -1){
					this.output.substring(0, this.output.lastIndexOf("."));
				}
				String[] exec = new String[]{ESTScanBin+" -M "+matrix + " -o " + this.output+"_est.fasta -t " + this.output+"_prot.fasta " + input}; 
				StringBuffer[] buffer = Tools_Task.runProcess(exec, true);
				for(int i =0; i < buffer.length; i++){
					if(i==0)logger.info("STDOUT:"+buffer[i]);
					else logger.info("ERROUT:"+buffer[i]);
				}
			}
			else{
				if(!new File(input).isFile())logger.error(input + " not a file");
				if(!new File(matrix).isFile())logger.error(matrix + " not a file");
			}
		}
		else{
			logger.error("Please make sure each of these are set: output, ESTScan exe/bin, matrix filepath");
		}
	    logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public String getESTScanBin(){
		return this.ESTScanBin;
	}
	
	public void setESTScanBin(String path){
		this.ESTScanBin = path;
	}
	
	public String getMatrix() {
		return matrix;
	}

	public void setMatrix(String matrix) {
		this.matrix = matrix;
	}
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		logger.debug("UI "+ui.getClass().getName()+" was given to me " + this.getClass().getName());
		this.ui = ui;
	}
	
	protected UI getUI(){
		return ui;
	}
}

