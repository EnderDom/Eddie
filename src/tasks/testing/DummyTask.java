package tasks.testing;

import tasks.Task;

public class DummyTask extends Task {

	//Class for testing purposes
	
	public DummyTask(){
		complete = -1;
	}
	
	public void run(){
		complete = 0;
		System.out.println("Hello World " + getID());
		complete = 1;
	}
	

	
}
