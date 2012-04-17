package tasks;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

/*
 * Task with additional tools as I'm lazy
 */

public class TaskXT extends Task{

	protected String input;
	protected String output;
	protected boolean overwrite;
	protected Logger logger = Logger.getLogger("TaskLogger");
	protected String filetype;
	
	public void parseArgsSub(CommandLine cmd){
		if(!cmd.hasOption("input")){
			logger.warn("Input file has not been set.");
		}
		else{
			setInput(cmd.getOptionValue("input"));
		}
		if(!cmd.hasOption("output")){
			logger.debug("Output file has not been set.");
		}
		else{
			setOutput(cmd.getOptionValue("output"));
		}
		if(cmd.hasOption("filetype")){
			setFileType(cmd.getOptionValue("filetype").toUpperCase());
			//TODO check this has supports filetype 
			logger.debug("Forced Filetype to be ->" + getFileType());
		}
		setOverwrite(cmd.hasOption("w"));
	}
	
	/*
	 * should exist
	 * 
	 * returns non-null object if
	 * 0) File does not already exists
	 * 1) File does exist
	 * 2) File does exist specifically as file
	 * 3) File does exist specifically as directory
	 * 4) Doesn't matter if it exists
	 */
	

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	public void setFileType(String filetype){
		this.filetype = filetype;
	}
	
	public String getFileType(){
		return this.filetype;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("i", "input", true, "Input"));
		options.addOption(new Option("o", "output", true, "Output"));
		options.addOption(new Option("w", "overwrite", false, "Overwrite output if it exists"));
		options.addOption(new Option("filetype", true, "Force specific filetype for Input"));
	}
	
	
	/*
	 * Very basic File Type detection
	 * 
	 */
	public String detectFileType(String filename){
		String[] filesuffix = new String[]{".ace",".sam",".fna",".fasta","fastq"};
		String[] filetype = new String[]{"ACE","SAM","FASTA","FASTA","FASTQ"};
		String filetype_val = "UNKNOWN";
		for(int i=0; i < filesuffix.length; i++){
			if(filename.endsWith(filesuffix[i])){
				filetype_val = filetype[i];
				break;
			}
		}
		logger.trace("Filetype detected as " + filetype_val);
		return filetype_val;
	}
}
