package enderdom.eddie.ui;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.ShutdownManager;
import enderdom.eddie.tasks.Task;
import enderdom.eddie.tasks.TaskLike;

public interface UI{
	
	Logger logger = Logger.getLogger("UILogger");
	
	public void exit();
	
	public void addTaskLike(TaskLike task);
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(TaskLike task);
	
	public boolean isGUI();
	
	public String requiresUserInput(String message, String title);
	
	public String requiresUserPassword(String message, String title);
	
	/**
	 * 
	 * @param message
	 * @param title
	 * @return integer value, responses:
	 * 0 = yes
	 * 1 = no
	 * 2 = cancel
	 */
	public UserResponse requiresUserYNI(String message, String title);
	
	public void sendAlert(String alert);
	
	public PropertyLoader getPropertyLoader();
	
	public TaskManager getTaskManager();
	
	/**
	 * 
	 * @param password  this can be null if no password is given, 
	 * the manager will ask the password from the user when connecting
	 * @return
	 */
	public DatabaseManager getDatabaseManager(String password);
	
	public DatabaseManager getDatabaseManager();
	
	public void setDatabaseManager(DatabaseManager manager);
	
	public void initShutdown();
	
	public ShutdownManager getShutdownManager();
	
}
