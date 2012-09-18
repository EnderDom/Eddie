package enderdom.eddie.tasks.testing;

import enderdom.eddie.gui.EddieGUI;

import org.apache.commons.cli.CommandLine;

import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.UI;

/**
 * Basic testing task, extend this, then
 * all you need to do to test is create constructor
 * with super() and runTest() method, everything else
 * is sorted 
 * 
 *  
 * @author dominic
 *
 */

public class Task_Test_Basic extends TaskXT {
	protected PropertyLoader load;
	protected CommandLine cmd;
	
	public Task_Test_Basic(){
		complete = -1;
		this.testmode = true;
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		this.cmd = cmd;
	}
	
	public void addUI(UI ui){
		logger.trace("UI added to " + this.getClass().getName());
		this.ui = ui;
		this.load = ui.getPropertyLoader();
	}
}
