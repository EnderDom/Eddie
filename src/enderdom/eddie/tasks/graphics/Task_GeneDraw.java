package enderdom.eddie.tasks.graphics;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;

public class Task_GeneDraw extends TaskXT{

	public String input;
	
	public Task_GeneDraw(){
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		input = getOption(cmd, "i", null);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("i", "input", true,  "Input Script to draw protein"));
		options.addOption(new Option("e", "examplescript", true, "Outputs an example script to this location"));
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Taxonomy Task @ "+Tools_System.getDateNow());
		if(input != null){
			//Do something
		}
		logger.debug("Finished running Taxonomy Task @ "+Tools_System.getDateNow());
		setCompleteState(TaskState.FINISHED);
	}
}
