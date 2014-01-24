package enderdom.eddie.tasks.internal;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;

public class Task_Update extends TaskXT{

	public double update1;
	public double update2;
	
	public Task_Update(double d, double d2){
		update1=d;
		update2=d;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		
	}
	
	public void parseOpts(Properties props){
		logger.debug("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
	}
	
	
	public void run() {
		setCompleteState(TaskState.STARTED);
		logger.error("No update available");
		
		setCompleteState(TaskState.FINISHED);
	}
	
}
