package enderdom.eddie.tasks.bio;


import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.ui.EddieProperty;
import enderdom.eddie.ui.UI;

public class Task_ESTScan extends TaskXT{
	
	
	private String ESTScanBin;
	private String matrix;
	private boolean bioen;
	private String input;
	private String output;
	private String params;
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		this.ESTScanBin=getOption(cmd, "b", null);
		this.matrix=getOption(cmd, "m", null);
		this.bioen=cmd.hasOption("e");
		this.input = getOption(cmd, "i", null);
		this.output = getOption(cmd, "o", null);
		this.params = getOption(cmd, "a", "");
	}
	
	public void parseOpts(Properties props){
		logger.debug("Parse Options From props");
		if(ESTScanBin == null){
			ESTScanBin = props.getProperty(EddieProperty.ESTSCAN_BIN.toString());
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "ESTScan", true, "ESTScan executable path"));
		options.addOption(new Option("m", "matrix", true, "filepath to matrix file"));
		options.addOption(new Option("e","bioentries", false, "Use bioentry ids for ESTscan !Advised!"));
		options.addOption(new Option("i", "input", true, "Input sequence file Fasta"));
		options.addOption(new Option("o", "output", true, "Output files, suffixes will be added"));
		options.addOption(new Option("a", "params", true, "Additional parameters for estscan"));
		options.removeOption("w");
		options.removeOption("filetype");
	}
	
	
	public void run() {
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(ESTScanBin != null && matrix != null){
			if(new File(matrix).isFile()){
				SequenceList list = null;
				if(this.output == null){
					this.output = FilenameUtils.getFullPath(new File(input).getPath())+FilenameUtils.getBaseName(new File(input).getPath());
					logger.debug("Output not set, setting as " + output+ "*");
				}
				if(bioen){
					logger.info("Building Fasta File from database...");
					DatabaseManager man = ui.getDatabaseManager(password);
					try {
						if(man.open()){
							list = man.getBioSQLXT().getContigsAsList(man, new Fasta(), -1);
							logger.debug("Writing as temporary file...");
							File in = File.createTempFile("tempfasta", ".fasta");
							input = in.getPath();
							logger.debug("Writing as temporary file to "  + input);
						}
						else{
							logger.error("Failed to open databse");
							return;
						}
					} catch (Exception e) {
						logger.error(e);
						setCompleteState(TaskState.ERROR);
						return;
					}
				}
				else if(!bioen){
					try {
						list = SequenceListFactory.getSequenceList(input);
						File in = File.createTempFile("tempfasta", ".fasta");
						input = in.getPath();
					} catch (Exception e) {
						logger.error("Failed to parse sequende list", e);
					}
				}
				else{
					if(input == null){
						logger.error("Input file isn't a file and database settings not set");
						setCompleteState(TaskState.ERROR);
						return;
					}
				}
				if(this.output.indexOf(".") != -1){
					this.output.substring(0, this.output.lastIndexOf("."));
				}
				try{
					list.saveFile(new File(input), BioFileType.FASTA);
					String ex = ESTScanBin+" "+params+" -M "+matrix + " -o " + this.output+"_est.fasta -t " + this.output+"_prot.fasta " + input;
					ex = ex.replaceAll(" +", " ");
					Tools_Task.runProcess(new String[]{ex}, true);
				}
				catch(Exception e){
					logger.error("Failed to execute ESTscan", e);
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
	    setCompleteState(TaskState.FINISHED);
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

