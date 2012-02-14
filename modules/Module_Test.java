package modules;

import gui.EddieGUI;

import tasks.Task_Test;

/*
 * Basic Module
 * Just runs a task on 
 * 
 */

public class Module_Test extends Module_Basic{
	
	protected String[] tasks = new String[]{"testrun"}; 
	protected String[] taskinfo = new String[]{"run a test program"};
	protected String[] classes = new String[]{Task_Test.class.getName()};
	
	public Module_Test(){
		
	}
	
	public String[] getTasks() {
		return tasks;
	}

	public String[] getTaskinfo() {
		return taskinfo;
	}
	
	public String[] getClasses() {
		return classes;
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
	
	}
}
