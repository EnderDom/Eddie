package ui;

import gui.EddieGUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import modules.Module;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cli.EddieCLI;

import tools.Tools_XML;

public class ModuleManager {

	/*
	 * Replacement for ModuleLoader
	 * as I've decided as I dislike that method
	 */
	Document xmlModuleList;
	private String modulefolder;
	private boolean loaded;
	Module[] instantiated;
	HashMap<String, String> module_classpath;
	HashMap<String, String> module_actions;
	HashMap<String, String> module_tasks;
	HashMap<String, String> module_task_classes;
	
	
	public ModuleManager(String modulefolder){
		this.modulefolder = modulefolder;
	}
	
	public boolean loadXML(){
		File in = new File(modulefolder+"modules.xml");
		if(in.isFile()){
			try{
				Tools_XML.inputStreamToDocument(new FileInputStream(in));
				loaded = true;
				return true;
			}
			catch(IOException exe){
				Logger.getRootLogger().error("Failed to Load Modules", exe);
				return false;
			}
			catch(SAXException exe){
				Logger.getRootLogger().error("Failed to Load Modules", exe);
				return false;
			}
			catch(ParserConfigurationException exe){
				Logger.getRootLogger().error("Failed to Load Modules", exe);
				return false;
			}
		}
		else{
			Logger.getRootLogger().warn("No Module Xml file!!");
			Logger.getRootLogger().warn("Building default XML with standard modules");
			return false;
		}
	}
	
	//Go through xml and pulls out all the classes
	public void loadModules(){
		//TODO
		/*
		 * Load all default modules here
		 */
		
		//Load additional modules here
		if(loaded){
			NodeList modules = xmlModuleList.getElementsByTagName("MOD");
			
			for(int i =0;i < modules.getLength(); i++){
				NodeList module_info = modules.item(i).getChildNodes();
				String modname = new String("");
				for(int j =0;j < module_info.getLength(); j++){
					if(module_info.item(i).getNodeName().contentEquals("MOD_NAME")){
						modname = module_info.item(i).getTextContent();
					}
					else if(module_info.item(i).getNodeName().contentEquals("MOD_CLASSPATH")){
						String classpath = module_info.item(i).getTextContent();
						if(modname.length() > 0 && classpath.length() > 0){
							module_classpath.put(modname, classpath);
						}
						else{
							Logger.getRootLogger().error("Module XML file is corrupt");
						}
					}
				}
			}
		}
		else Logger.getRootLogger().error("Module XML not loaded. Why is this still being called???");
	}
	
	public void setupGUI(EddieGUI gui){
		for(String key : module_classpath.keySet()){
			try {
				Module temp =(Module)Class.forName(module_classpath.get(key)).getConstructor().newInstance();
				temp.addToGui(gui);
				//TODO add actions to list
				//TODO add tasks to list
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void setupCLI(EddieCLI cli){
		for(String key : module_classpath.keySet()){
			try {
				Module temp =(Module)Class.forName(module_classpath.get(key)).getConstructor().newInstance();
				temp.addToCli(cli);
				//TODO add actions to list
				//TODO add tasks to list
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void checkInstalls(){
		
	}
	
	public void addActions(){
		
	}
	
	public void addModule(){
		
	}
	
	public void defaultInstall(){
		
	}
	
}
