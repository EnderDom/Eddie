package tasks;


import gui.EddieGUI;

public class Task_Test extends Task{

	
	
	public Task_Test(){
		complete = -1;
		this.testmode = true;
	}
	
	public void runTest(){
		/*
		 * Testing long print
		 */
		
		System.out.println("Test has completed");
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	
	
}
