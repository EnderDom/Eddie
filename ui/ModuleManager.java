package ui;

import gui.EddieGUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;



import modules.Module;
import modules.Module_Test;
import modules.bio.Module_Bio;
import modules.lnf.DefaultLNF;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cli.EddieCLI;

import tools.Tools_XML;

public class ModuleManager implements Module{

	/*
	 * Second attempt at Modularity stuff
	 * See previous github for ModuleLoader.
	 * It might actually be better???
	 * 
	 * Replacement for ModuleLoader
	 * as I've decided as I dislike that method
	 * 
	 * 
	 */
	Document xmlModuleList;
	private String modulefolder;
	private boolean loaded;
	/*
	* Key is the module name, holds either the classpath
	* or a id to the object if the object is persistant
	*/
	HashMap<String, String> module_classpath;
	/*
	 * Key is the action command, holds the classpath or 
	 * link to object which deals with the action.
	 */
	HashMap<String, String> module_actions;
	/*
	 * Like actions, only for tasks, tasks tend to be called
	 * for specific 'scripts' to be run, and example is 'testrun'
	 * which should call the Task_Test class
	 */
	HashMap<String, String> module_tasks;
	/*
	 * Holds a quick index of the persistant object
	 * I feel this is quicker than actually parsing the string
	 * for the intege, integer returned relates to the modules array 
	 */
	HashMap<String, Integer> persistedObjectIndex;
	/*
	 * Holds persistant objects for referencing
	 */
	Module[] modules;
	/*
	 * Current modulecount id, used so all modules have unique id
	 */
	int modulecount;
	/*
	 * Just a keyword to differentiate classpaths from
	 * objects
	 */
	private static String persistkeyword = "PERSIST";
	
	String modulename = this.getClass().getName();
	
	public ModuleManager(String modulefolder){
		modulecount=0;
		this.modulefolder = modulefolder;
		module_classpath = new HashMap<String, String>();
		module_actions = new HashMap<String, String>();
		module_tasks = new HashMap<String, String>();
		persistedObjectIndex = new HashMap<String, Integer>();
	}
	
	public void init(){

		addDefaultModules();
		//TODO implement external modules
		//loadXML();
		//loadModules();
	}
	
	
	/*
	 * Add all the default modules
	 */
	public void addDefaultModules(){
		addModule("LNF", DefaultLNF.class.getName());
		addModule("BIO", Module_Bio.class.getName());
		addModule("TEST", Module_Test.class.getName());
	}
	
	public boolean loadXML(){
		File in = new File(modulefolder+"modules.xml");/*
			This file should contain any additional modules
			this is mainly hypothetical
		*/
		if(in.isFile()){
			try{
				xmlModuleList = Tools_XML.inputStreamToDocument(new FileInputStream(in));
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
			return false;
		}
	}
	
	//TODO incomplete implementation
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
						if(module_classpath.containsKey(modname)){
							modname = modname + "X";
						}
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
	
	public void pullTaskAndActions(Module temp, String key){
		String[] actions = temp.getActions();
		if(actions != null){
			for(String act : actions)addAction(act, getModule(key));
		}
		String[] tasks = temp.getTasks();
		if(tasks != null){
			for(String task : tasks)addTask(task, getModule(key));
		}
	}
	
	public String getModule(String key){
		return this.module_classpath.get(key);
	}
	
	public boolean isTask(String task){
		return this.module_tasks.containsKey(task);
	}
	
	public boolean isAction(String action){
		return this.module_actions.containsKey(action);
	}
	
	public void setupGUI(EddieGUI gui){
		for(String key : module_classpath.keySet()){
			if(!module_classpath.get(key).startsWith(persistkeyword)){
				try {
					Module temp =(Module)Class.forName(module_classpath.get(key)).getConstructor().newInstance();
					if(temp.isPersistant()){
						addPrebuiltModule(key, temp, gui);
					}
					else{
						temp.addToGui(gui);
						pullTaskAndActions(temp, key);
						temp = null;
					}
				} catch (IllegalArgumentException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (SecurityException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (InstantiationException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (IllegalAccessException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (InvocationTargetException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (NoSuchMethodException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (ClassNotFoundException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				}
			}
		}
	}
	
	public void setupCLI(EddieCLI cli){
		for(String key : module_classpath.keySet()){
			if(!module_classpath.get(key).startsWith(persistkeyword)){
				try {
					Module temp =(Module)Class.forName(module_classpath.get(key)).getConstructor().newInstance();
					if(temp.isPersistant()){
						addPrebuiltModule(key, temp, cli);
					}
					else{
						temp.addToCli(cli);
						pullTaskAndActions(temp, key);
						temp = null;
					}
				} catch (IllegalArgumentException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (SecurityException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (InstantiationException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (IllegalAccessException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (InvocationTargetException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (NoSuchMethodException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				} catch (ClassNotFoundException e) {
					Logger.getRootLogger().error("Cannot create class for module "+key,e);
				}
			}
		}
	}
	
	
	public void addAction(String action, String classpath){
		Logger.getRootLogger().trace("Adding action "+ action  + " to class or object " + classpath);
		this.module_actions.put(action, classpath);
	}
	
	public void addTask(String taskName, String taskclasspath){
		Logger.getRootLogger().trace("Adding task "+ taskName  + " to class or object " + taskclasspath);
		this.module_tasks.put(taskName, taskclasspath);
	}
	
	public void addModule(String modulename, String moduleclasspath){
		Logger.getRootLogger().trace("Adding ModuleName "+ modulename  + " with path / object name " + moduleclasspath);
		this.module_classpath.put(modulename, moduleclasspath);
	}
	
	public void addPrebuiltModule(String key, Module mod, UI ui){
		Logger.getRootLogger().debug("Loading Module "+ key);
		String modname = persistkeyword+modulecount;
		modulecount++;
		mod.resetModuleName(modname);/*<- Important to set, else any downstream 
									actions added will be set to default not persisting
									*/
		if(modules == null){
			modules = new Module[5];
		}
		else if(modulecount == modules.length){
			Module[] placeholder = new Module[modules.length+5];
			for(int i =0;i < modules.length; i++)placeholder[i]=modules[i];
			modules = placeholder;
		}
		if(ui.isGUI()){
			mod.addToGui((EddieGUI)ui);
		}
		else{
			mod.addToCli((EddieCLI)ui);
		}
		modules[modulecount] = mod;
		persistedObjectIndex.put(modname, modulecount);
		module_classpath.put(key,modname);
		pullTaskAndActions(mod, key);
	}
			
	public void runAction(EddieGUI gui, String action){
		try {
			String actionclass = this.module_actions.get(action);
			Logger.getRootLogger().debug("Responding to action "+ action + " class : " + actionclass);
			if(!actionclass.startsWith(persistkeyword)){
				Module temp =(Module)Class.forName(actionclass).getConstructor().newInstance();
				temp.actOnAction(action, gui);
			}
			else{
				Integer index = -1;
				if((index = persistedObjectIndex.get(actionclass)) != null){
					modules[index].actOnAction(action, gui);
				}
				else{
					Logger.getRootLogger().error("An error occured attempting to retrieve persistant object"+ actionclass);
				}
			}			
		} catch (IllegalArgumentException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (SecurityException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (InstantiationException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (IllegalAccessException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (InvocationTargetException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (NoSuchMethodException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		} catch (ClassNotFoundException e) {
			Logger.getRootLogger().error("An error running action "+action, e);
		}
	}
	
	public void runTask(UI ui, String task){
		try {
			String taskclass = this.module_tasks.get(task);
			if(!taskclass.startsWith(persistkeyword)){
				Module temp =(Module)Class.forName(taskclass).getConstructor().newInstance();
				temp.actOnTask(task, ui);
			}
			else{
				Integer index = -1;
				if((index = persistedObjectIndex.get(taskclass)) != null){
					modules[index].actOnTask(task, ui);
				}
				else{
					Logger.getRootLogger().error("An error occured attempting to retrieve persistant object"+ taskclass);
				}
			}			
		} catch (IllegalArgumentException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (SecurityException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (InstantiationException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (IllegalAccessException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (InvocationTargetException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (NoSuchMethodException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		} catch (ClassNotFoundException e) {
			Logger.getRootLogger().error("An error running task "+task, e);
		}		
	}
	
	public void printAllTasks(){
		for(String key : module_classpath.keySet()){
			try {
				if(!module_classpath.get(key).startsWith(persistkeyword)){
					Module temp =(Module)Class.forName(module_classpath.get(key)).getConstructor().newInstance();
					temp.printTasks();
				}
				else{
					modules[this.persistedObjectIndex.get(module_classpath.get(key))].printTasks();
				}
			} catch (IllegalArgumentException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (SecurityException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (InstantiationException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (IllegalAccessException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (InvocationTargetException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (NoSuchMethodException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			} catch (ClassNotFoundException e) {
				Logger.getRootLogger().error("Error printing tasks for key "+key, e);
			}
		}
	}
	
	 /******************************************
     * 
     * 
     * Module Specific Methods
     * 
     * 
     ******************************************/

	public void actOnAction(String s, EddieGUI biodesktopgui) {
		// TODO Auto-generated method stub
		
	}

	public void actOnTask(String s, UI ui) {
		// TODO Auto-generated method stub
		
	}

	public void printTasks() {
		// TODO Auto-generated method stub
	}

	public void addToGui(EddieGUI gui) {
		// TODO Auto-generated method stub
		
	}

	public void addToCli(EddieCLI cli) {
		// TODO Auto-generated method stub
		
	}

	public boolean isPersistant() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getModuleName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetModuleName(String name) {
		this.modulename = name;
	}
}
