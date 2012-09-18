package enderdom.eddie.modules;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import enderdom.eddie.gui.EddieGUI;

import enderdom.eddie.tasks.testing.Task_Test;
import enderdom.eddie.tasks.testing.Task_Test_Melting;
import enderdom.eddie.tasks.testing.Task_Test_Report;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.PropertyLoaderXT;
import enderdom.eddie.ui.UI;

/*
 * Basic Module
 * Just runs a task on 
 */

public class Module_Test extends Module_Basic{
	
	protected String[] tasks = new String[]{"testrun", "testReport", "Melting"}; 
	protected String[] taskinfo = new String[]{"	run a test program", "generates the test Report", "	Testing the melting lib"};
	protected String[] classes = new String[]{Task_Test.class.getName(), Task_Test_Report.class.getName(), Task_Test_Melting.class.getName()};
	public static String TestRootFolder = "TestRootFolder";
	public static String TestDataFolder = "TestDataFolder";
	public static String TestOutFolder = "TestOutFolder";
	
	
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
	
	public static Properties getProperties(UI ui){
		Properties testprops = ((PropertyLoaderXT) ui.getPropertyLoader()).getSubProperty(PropertyLoaderXT.TestPrefs);
		if(testprops == null){
			((PropertyLoaderXT) ui.getPropertyLoader()).initilaseSubProperty(PropertyLoaderXT.TestPrefs);
			initTestProperties(ui.getPropertyLoader(), testprops);
			testprops = ((PropertyLoaderXT) ui.getPropertyLoader()).getSubProperty(PropertyLoaderXT.TestPrefs);
		}
		if(!validateFiles(testprops)){
			Logger.getRootLogger().warn("Property file validation failed, reinitialising...");
			initTestProperties(ui.getPropertyLoader(), testprops);
			if(!validateFiles(testprops)) Logger.getRootLogger().warn("Property file validation failed again, file permissions?");
		}
		return testprops;
	}
		
	public static void initTestProperties(PropertyLoader load, Properties test){
		if(Tools_File.createFolderIfNotExists(new File(load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"))){
			test.setProperty("TestRootFolder",load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator());
		}
		if(Tools_File.createFolderIfNotExists(new File(load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"data"))){
			test.setProperty("TestRootFolder",load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator());
		}
		if(Tools_File.createFolderIfNotExists(new File(load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"out"))){
			test.setProperty("TestRootFolder",load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"out"+Tools_System.getFilepathSeparator());
		}
	}
	
	private static boolean validateFiles(Properties props){
		for(String s : new String[]{TestRootFolder, TestDataFolder, TestOutFolder}){
			if(props.containsKey(s)){
				if(!new File(props.getProperty(s)).exists()) return false; 
			}
			else return false;
		}
		return false;
	}

}
