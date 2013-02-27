package enderdom.eddie.ui;

import java.util.Stack;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.Task;

public interface UI{
	
	Logger logger = Logger.getLogger("UILogger");
	
	public static int YES =0;
	public static int NO =1;
	public static int CANCEL =2;
	
	public void exit();
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(Task task);
	
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
	public int requiresUserYNI(String message, String title);
	
	public void sendAlert(String alert);
	
	public PropertyLoader getPropertyLoader();
	
	/**
	 * 
	 * @param password  this can be null if no password is given, 
	 * the manager will ask the password from the user when connecting
	 * @return
	 */
	public DatabaseManager getDatabaseManager(String password);
	
	public DatabaseManager getDatabaseManager();
	
	public void setDatabaseManager(DatabaseManager manager);
	
	public void fireUIEvent(UIEvent evt);

	
	/**
	 * This is primarily for the GUI, to alert the user of an 
	 * error as they may not see the logs
	 * @param message
	 * @param t
	 */
	public void throwError(String message, Throwable t);

	public void throwError(String message, String[] details);
	
	public void error(String message, Throwable t);

	public void error(String message);
	
	public Stack<String> requisitionTasker();
	
}
