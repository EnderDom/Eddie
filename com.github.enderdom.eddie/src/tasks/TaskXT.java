package tasks;

import org.apache.log4j.Logger;

import ui.UI;

/*
 * Task with additional tools as I'm lazy
 */

public class TaskXT extends Task{

	protected Logger logger = Logger.getLogger("TaskLogger");
	protected Checklist checklist;
	protected UI ui;
	
	public void buildOptions(){
		super.buildOptions();
	}
	
	public void openChecklist(){
		checklist = new Checklist(ui.getPropertyLoader().getWorkspace(), this.getClass().getName());
		if(checklist.check()){
			logger.trace("Moved to recovering past Task");
			int userinput = ui.requiresUserYNI("There is an unfinished task, Details: "+checklist.getLast()+" Would you like to recover it (yes), delete it (no) or ignore it (cancel)?","Recovered Unfinished Task...");
			if(userinput == 1){
				if(!checklist.closeLastTask()){
					logger.error("Failed to delete last task");
				}
				else{
					logger.debug("Cleared Task");
				}
				checklist.start(this.args);
			}
			if(userinput == 0){
				args = checklist.getArgs();
				if(args != null){
					super.parseArgs(args.split(" "));
					checklist.recoverLast();
				}
				else{
					logger.error("An error occured, Comment does not contain arguments");
				}
			}
			else{
			}
		}
		else{
			checklist.start(this.args);
		}
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
	public boolean isKeepArgs(){
		return true;
	}
}