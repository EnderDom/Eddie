

package ui;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import tasks.Task;

public interface UI {
	
	Logger logger = Logger.getLogger("UILogger");
	
	EventListenerList listenerList = new EventListenerList();

	public void exit();
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(Task task);
	
	public boolean isGUI();
	
	public void sendAlert(String alert);
	
	public void addUIEventListener(UIEventListener listener);

	public void removeUIEventListener(UIEventListener listener);	

	void fireUIEvent(UIEvent evt);
	
}
