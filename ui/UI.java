package ui;

import tasks.Task;

public interface UI {

	public void exit();
	
	public void addTask(Task task);
	
	public void buildTaskManager();
	
	public void update(Task task);
		
}
