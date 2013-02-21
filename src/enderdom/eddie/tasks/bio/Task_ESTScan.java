package enderdom.eddie.tasks.bio;


import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;

public class Task_ESTScan extends TaskXTwIO{
	
	
	private String ESTScanBin;
	private String matrix;
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))this.ESTScanBin=cmd.getOptionValue("b");
		if(cmd.hasOption("m"))this.matrix=cmd.getOptionValue("m");
	}
	
	public void parseOpts(Properties props){
		logger.trace("Parse Options From props");
		if(ESTScanBin == null){
			ESTScanBin = props.getProperty("ESTSCAN_BIN");
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "ESTScan", true, "ESTScan executable path"));
		options.addOption(new Option("m", "matrix", true, "filepath to matrix file"));
		options.getOption("i").setDescription("Input sequence file Fasta");
		options.getOption("o").setDescription("Output folder");
	}
	
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		System.out.println("Method Incomplete...");
		if(ESTScanBin != null && matrix != null && output != null && input!= null){
			if(new File(input).isFile() && new File(matrix).isFile()){
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
			logger.error("Please make sure each of these are set: input, output, ESTScan exe/bin, matrix filepath");
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
}
