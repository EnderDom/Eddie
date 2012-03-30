package tasks;

import java.io.File;

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
	public static int NOT_FILE_OR_DIRECTORY = 0;
	public static int IS_FILE_OR_DIRECTORY = 1;
	public static int IS_FILE = 2;
	public static int IS_DIRECTORY = 3;
	public static int IS_OR_NOT = 4;
	protected Logger logger = Logger.getLogger("TaskLogger");
	boolean tocontinue = false;
	
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
	public File getFile(String input, int shouldexist){
		File tmp = new File(input);
		
		switch(shouldexist){
		case 0:
			if(tmp.exists()) return null;
			else return tmp;
		case 1: 
			if(tmp.exists()) return tmp;
			else return null;
		case 2: 
			if(tmp.isFile())return tmp;
			else return null;
		case 3: 
			if(tmp.isDirectory())return tmp;
			else return null;
		default:
			return tmp;
		}
	}
	
	
	public File getStdInput(){
		// Method assumes input should exist as a file (not folder)
		return getFile(this.input, 2);
	}
	
	public File getStdOutput(){
		//Method assumes output shouldn't be a file unless overwrite
		if(!overwrite){
			return getFile(this.output, 0); 
		}
		else return getFile(this.output, 4);
	}

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
	
	public void buildOptions(){
		super.buildOptions();
		
		options.addOption(new Option("i", "input", true, "Input"));
		options.addOption(new Option("o", "output", true, "Output"));
		options.addOption(new Option("w", "overwrite", false, "Overwrite output if it exists"));
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
		return filetype_val;
	}
}
