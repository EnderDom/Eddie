

package ui;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

import tasks.Task;

public interface UI {
	
	Logger logger = Logger.getLogger("UILogger");
	
	EventListenerList listenerList = new EventListenerList();

	public void exit();
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(Task task);
	
	public boolean isGUI();
	
	public String requiresUserInput(String message, String title);
	
	public String requiresUserPassword(String message, String title);
	
	public int requiresUserYNI(String message, String title);
	
	public void sendAlert(String alert);
	
	public void addUIEventListener(UIEventListener listener);

	public void removeUIEventListener(UIEventListener listener);	

	void fireUIEvent(UIEvent evt);
	
	public PropertyLoader getPropertyLoader();
	
	public DatabaseManager getDatabaseManager();
	
	public void setDatabaseManager(DatabaseManager manager);
	
}
