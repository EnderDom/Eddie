

package ui;

import org.apache.log4j.Logger;

import tasks.Task;

public interface UI {
	
	Logger logger = Logger.getLogger("UILogger");

	public void exit();
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(Task task);
	
	public boolean isGUI();
	
	public void sendAlert(String alert);
		
}
