package cli;

import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;
import tasks.Task;
import tools.Tools_CLI;
import ui.PropertyLoader;
import ui.UI;
import ui.UIEvent;

public class EddieLite implements UI{
	
	PropertyLoader loader;
	Logger logger = Logger.getRootLogger();
	DatabaseManager manager;
	
	//Come to think of it, not sure how this will work?????
	
	public EddieLite(PropertyLoader loader){
		this.loader = loader;
	}

	public void exit() {
		// TODO Auto-generated method stub
		
	}

	public void addTask(Task task) {
		Logger.getRootLogger().debug("Lite recieve task, sending to task manager...");
		if(task.wantsUI())task.addUI(this);
		if(loader.args != null)task.parseArgs(loader.args);
		task.parseOpts(this.loader.getProps());
		task.run();
	}

	
	public void buildTaskManager() {
		//
	}

	public void update(Task task) {
		// TODO Auto-generated method stub
		
	}

	public boolean isGUI() {
		return false;
	}

	public String requiresUserInput(String message, String title) {
		logger.error("This doesn't work with EddieLite");
		return null;
	}

	public String requiresUserPassword(String message, String title) {
		logger.error("Eddie lite mode, you must manually set password");
		return null;
	}

	public int requiresUserYNI(String message, String title) {
		return Tools_CLI.cancel;
	}

	public void sendAlert(String alert) {
		// TODO Auto-generated method stub
	}

	public PropertyLoader getPropertyLoader() {
		return loader;
	}

	public DatabaseManager getDatabaseManager(String password) {
		return manager;
	}

	public void setDatabaseManager(DatabaseManager manager) {
		this.manager = manager;
	}

	public void fireUIEvent(UIEvent evt) {
		// TODO Auto-generated method stub
		
	}
	
}
