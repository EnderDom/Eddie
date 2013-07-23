package enderdom.eddie.tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;

public class Task_SeqList extends TaskXTwIO{

	private int trimr;
	private int triml;
	private SequenceList list;
	private BioFileType t;
	
	public Task_SeqList(){
		
	}
	
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(input==null || output==null){
			logger.error("Output/Input not set, please set both tags");
		}
		else{
			 try {
				 list = SequenceListFactory.getSequenceList(input);
				 t = list.getFileType();
				 if(trimr+triml>-1){
					 for(String s : list.keySet()){
						 if(triml > -1){
							 logger.debug("Trimming sequence "+s+" by " + triml+ " on the left");
							 list.getSequence(s).leftTrim(triml, 0);
						 }
						 if(trimr > -1){
							 logger.debug("Trimming sequence "+s+" by " + trimr+ " on the right");
							 list.getSequence(s).rightTrim(trimr, 0);
						 }
					 }
				 }
				 else logger.warn("No changes made");
				 list.saveFile(new File(output), t);
			 }
			 catch (Exception e) {
				logger.error("Failed to parse "+ input, e);
			}
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("tl","trimLeft",true, "Trim left (5') all sequences by this value"));
		options.addOption(new Option("tr","trimRight",true, "Trim right (3') all sequences by this value"));
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		this.triml=getOption(cmd, "tl", -1);
		this.trimr=getOption(cmd, "tr", -1);
	}
	
	public Options getOptions(){
		return this.options;
	}
}
