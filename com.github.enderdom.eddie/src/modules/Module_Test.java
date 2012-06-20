package modules;

import gui.EddieGUI;

import tasks.testing.Task_Test;
import tasks.testing.Task_Test_Melting;
import tasks.testing.Task_Test_Report;

/*
 * Basic Module
 * Just runs a task on 
 */

public class Module_Test extends Module_Basic{
	
	protected String[] tasks = new String[]{"testrun", "testReport", "Melting"}; 
	protected String[] taskinfo = new String[]{"	run a test program", "generates the test Report", "	Testing the melting lib"};
	protected String[] classes = new String[]{Task_Test.class.getName(), Task_Test_Report.class.getName(), Task_Test_Melting.class.getName()};
	
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
	
	public boolean isTest(){
		return true;
	}
}
