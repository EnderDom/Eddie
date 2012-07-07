package modules;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import gui.EddieGUI;

import tasks.testing.Task_Test;
import tasks.testing.Task_Test_Biojavax;
import tasks.testing.Task_Test_Melting;
import tasks.testing.Task_Test_Report;
import tools.Tools_File;
import tools.Tools_System;
import ui.PropertyLoader;
import ui.UI;

/*
 * Basic Module
 * Just runs a task on 
 */

public class Module_Test extends Module_Basic{
	
	protected String[] tasks = new String[]{"testrun", "testReport", "Melting", "Biojavax"}; 
	protected String[] taskinfo = new String[]{"	run a test program", "generates the test Report", "	Testing the melting lib", "biojavax testing tools"};
	protected String[] classes = new String[]{Task_Test.class.getName(), Task_Test_Report.class.getName(), Task_Test_Melting.class.getName(), Task_Test_Biojavax.class.getName()};
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
		Properties testprops = ui.getPropertyLoader().getSubProperty(PropertyLoader.TestPrefs);
		if(testprops == null){
			ui.getPropertyLoader().initilaseSubProperty(PropertyLoader.TestPrefs);
			initTestProperties(ui.getPropertyLoader(), testprops);
			testprops = ui.getPropertyLoader().getSubProperty(PropertyLoader.TestPrefs);
		}
		if(!validateFiles(testprops)){
			Logger.getRootLogger().warn("Property file validation failed, reinitialising...");
			initTestProperties(ui.getPropertyLoader(), testprops);
			if(!validateFiles(testprops)) Logger.getRootLogger().warn("Property file validation failed again, file permissions?");
		}
		return testprops;
	}
		
	public static void initTestProperties(PropertyLoader load, Properties test){
		if(Tools_File.createFolderIfNotExists(new File(load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"))){
			test.setProperty("TestRootFolder",load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator());
		}
		if(Tools_File.createFolderIfNotExists(new File(load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"data"))){
			test.setProperty("TestRootFolder",load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator());
		}
		if(Tools_File.createFolderIfNotExists(new File(load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"out"))){
			test.setProperty("TestRootFolder",load.getWorkspace()+Tools_System.getFilepathSeparator()+"test"+Tools_System.getFilepathSeparator()+"out"+Tools_System.getFilepathSeparator());
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
