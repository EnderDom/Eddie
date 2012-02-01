package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import modules.Module;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.Tools_XML;

public class ModuleManager {

	/*
	 * Replacement for ModuleLoader
	 * as I've decided as I dislike that method
	 */
	Document xmlModuleList;
	private String modulefolder;
	private boolean loaded;
	NodeList modules;
	Module[] instantiated;
	
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
			Logger.getRootLogger().error("No Module Xml file!!");
			return false;
		}
	}
	
	//Go through xml and pulls out all the classes
	public void loadModules(){
		if(loaded)modules = xmlModuleList.getElementsByTagName("MOD");
		else Logger.getRootLogger().error("Module XML not loaded. Why is this still being called???");
	}
	
	public void checkInstalls(){
		
	}
	
	public void addActions(){
		
	}
	
}
