package ui;

import tasks.DummyTask;
import tasks.Task;

public class TaskFactory {

	//Creates tasks sent by the UI 
	
	//Holds lists of all the tasks
	
	public static Task createTask(String taskname, String[] args){
		if(taskname.contentEquals("DummyTask")){
			return new DummyTask();
		}
		else{
			return new DummyTask();
		}
	}
	
}
