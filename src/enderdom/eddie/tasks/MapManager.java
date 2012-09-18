package enderdom.eddie.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_XML;
import enderdom.eddie.ui.PropertyLoader;

/**
 * 
 * Class builds the map folder
 * 
 * Creates maps/ directory in workspace if not exist
 * Creates index_map.xml which holds file map details
 * All maps have the .map file ending, but are plain txt
 * 
 * @author Dominic Wood
 *
 */

public class MapManager{
	
	public static String mapfoldername = "maps";
	private static String indexFileName= "index_maps.xml";
	private static String separator =":##:"; //Whatever this is, it should be something that wouldn't exist in filepath	
	private File mapfoldere;
	private static String mapfileext = ".eddmap";
	Logger logger = Logger.getRootLogger();
	Document mapindex;
	
	private String[] cachedMapFile;
	
	
	public MapManager(PropertyLoader load){
		mapfoldere = new File(load.getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+mapfoldername+Tools_System.getFilepathSeparator());
		if(!mapfoldere.isDirectory()){
			if(!mapfoldere.isFile()){
				logger.debug("Creating Map Directory");
				mapfoldere.mkdir();
				logger.debug("Building Index");
				buildIndexFile();
			}
			else{
				logger.error("Should be a folder called maps in workspace, but instead it is a file!");
				boolean del = mapfoldere.delete();
				if(del)del=mapfoldere.mkdir();
				if(!del)logger.error("And map folder could not be overwritten!");
				else{
					buildIndexFile();
				}
			}
		}
		if(!new File(getMapFolderPath()+indexFileName).exists()){
			logger.debug("Maps Folder exists, but index doesnt??? Creating Index XML");
			buildIndexFile();
		}
	}
	
	public boolean hasMap(String filepath1, String filepath2){
		loadIndex();
		String one = filepath1+separator+filepath2;
		String two = filepath2+separator+filepath1;
		NodeList list = this.mapindex.getElementsByTagName("Map");
		boolean ret = false;
		LinkedList<Element> remove = null;
		for(int i =0; i < list.getLength(); i++){
			Element n = (Element)list.item(i);
			if(n.getTextContent().equals(one)||n.getTextContent().equals(two)){
				logger.debug("Previous Map build found");
				this.cachedMapFile = new String[3];
				String[] s = n.getTextContent().split(separator);
				if(s.length != 2)logger.error("Balls");
				else{
					this.cachedMapFile[0] = s[0];
					this.cachedMapFile[1] = s[1];
				}			
				this.cachedMapFile[2] = getMapFolderPath()+n.getAttributeNode("FILE").getNodeValue();
				ret = true;
				
				logger.debug("Updating Last_VIEWED Data");
				//Change View
				n.setAttributeNS(null, "LAST_VIEWED", Tools_System.getDateNow());
				saveIndex();
				break;
			}
			else{
				String src = n.getAttributeNode("LAST_VIEWED").getNodeValue();
				int daysold = Tools_System.getDeltaInDaysToNow(src);
				if(daysold > 30){
					//TODO Delete Old Unused Maps
					logger.debug("Map is old, adding to remove list");
					if(remove == null){
						 remove = new LinkedList<Element>();
					}
					remove.add(n);
				}
			}
		}
		try{
			if(remove != null){
				while(remove.size() != 0){
					logger.debug("Killing Child");
					this.mapindex.removeChild(remove.getLast());
				}
				saveIndex();
			}
		}
		catch(Exception e){
			logger.error("Failed to kill all children");
			logger.trace("Ring Mary Bell.");
		}
		
		return ret;
	}
	
	public HashMap<String, String> getMap(String filepath1, String filepath2) throws IOException{
		if(cachedMapFile == null){
			if(hasMap(filepath1, filepath2)){
				return loadMapFile(cachedMapFile[2]);
			}
			else{
				logger.error("Map does not exist: " + filepath1 + " & " + filepath2);
				return null;
			}
		}
		else if((cachedMapFile[0].equals(filepath1) && cachedMapFile[1].equals(filepath2)) 
				|| (cachedMapFile[0].equals(filepath2) && cachedMapFile[1].equals(filepath1))){
			return loadMapFile(cachedMapFile[2]);
		}
		else {
			if(hasMap(filepath1, filepath2)){
				return loadMapFile(cachedMapFile[2]);
			}
			else{
				logger.error("Map does not exist: " + filepath1 + " & " + filepath2);
				return null;
			}
		}
	}
	
	private HashMap<String, String> loadMapFile(String filepath) throws IOException{
		File file = new File(filepath);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		String line = "";
		String[] i;
		HashMap<String, String> set = null;
		line=reader.readLine();
		if(line.length() >0){
			Integer l = Tools_String.parseString2Int(line);
			if(l != null){
				set = new HashMap<String, String>(l);
			}
			else{
				throw new IOException("Map first line should be size of set");
			}
		}
		else{
			throw new IOException("Map first line should be size of set");
		}
		while((line = reader.readLine()) != null){
			i = line.split(separator);
			if(i.length!=2) throw new IOException();
			else{
				set.put(i[0], i[1]);
			}
		}
		return set;
	}
	
	public boolean addMap(String filepath1, String filepath2, HashMap<String, String> maps){
		try{
			logger.debug("Attempting to add map " + filepath1 +"  " + filepath2);
			//Build Map name
			String mapfilename = "";
			File file = new File(filepath1);
			if(file.exists()){
				String[] f = file.getName().split("\\.");
				if(f.length>0){
					mapfilename = mapfilename+f[0]+"_";
				}
				else{
					mapfilename = mapfilename+file.getName()+"_";
				}
			}
			file = new File(filepath2);
			if(file.exists()){
				String[] f = file.getName().split("\\.");
				if(f.length>0){
					mapfilename = mapfilename+f[0]+"_";
				}
				else{
					mapfilename = mapfilename+file.getName()+"_";
				}
			}
			//Check name doesn't already, exist then create filewriter
			logger.debug("Map file name generated " + mapfilename);
			File n = checkName(mapfilename);
			FileWriter fstream = new FileWriter(n);
			BufferedWriter out = new BufferedWriter(fstream);
			//Save the hashset to file
			String nl = Tools_System.getNewline();
			out.write(maps.size()+nl);
			out.flush();
			for(String s : maps.keySet()){
				out.write(s+separator+maps.get(s)+nl);
				out.flush();
			}
			out.close();
			//Add the file, plus filepaths to the map_index xml
			
			if(!loadIndex()){
				throw new IOException("Index Load Fail, passed on the Map Add Fail.");
			}
			else{
				addMapFile(n.getName(), filepath1, filepath2, (Element)getRootNode());
			}
			return true;
		}
		catch(IOException e){
			logger.error("Could not add map", e);
			return false;
		}
	}
	
	private boolean loadIndex(){
		try{
			logger.debug("Loading Map Index File");
			File index = new File(getMapFolderPath()+indexFileName);
			this.mapindex = Tools_XML.inputStreamToDocument(new FileInputStream(index));
			return true;
		}
		catch(IOException io){
			logger.error("Failed to load Index FIle into XML document due to IO", io);
			return false;
		} 
		catch (ParserConfigurationException e) {
			logger.error("Failed to load Index FIle into XML document due to Config", e);
			return false;
		} 
		catch (SAXException e) {
			logger.error("Failed to load Index FIle into XML document due to SAX", e);
			return false;
		}

	}
	
	private File checkName(String mapfilename){
		File file = new File(getMapFolderPath()+mapfilename+mapfileext);
		int c=0;
		if(file.exists()){
			while(file.exists()){
				file = new File(getMapFolderPath()+mapfilename+"_"+c+mapfileext);
				c++;
			}
		}
		return file;
	}
	
	private boolean buildIndexFile(){
		try {
			logger.debug("Building XML for Map Index");
			DocumentBuilder build = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			mapindex = build.newDocument();
			Element root = mapindex.createElementNS(null,"MAPLIST");
			mapindex.appendChild(root);
			logger.debug("Added test Map");
			return saveIndex();
		}
		catch (ParserConfigurationException e) {
			logger.error("Failed to build MapIndexFile", e);
			return false;
		}
	}
	
	private boolean saveIndex(){
		logger.debug("Saving IndexMapFile XML");
		boolean ret = false;
		try{
			Tools_XML.Xml2File(this.mapindex, new File(getMapFolderPath()+indexFileName));
			return ret;
		}
		catch(Exception e){
			logger.error("Failed to save index",e);
			return false;
		}
	}
	
	private Element buildMapEle(String mapfile, String filepath1, String filepath2){
		Element e = this.mapindex.createElement("Map");
		e.setAttribute("FILE", mapfile);
		e.setAttribute("MAPTYPE", "HASH");//Leaves an opportunity for other Maps in the future?
		e.setAttribute("DATEADDED", Tools_System.getDateNow());
		e.setAttribute("LAST_VIEWED", Tools_System.getDateNow()); 
		e.setTextContent(filepath1+separator+filepath2);
		return e;
	}
	
	private boolean addMapFile(String mapfile, String filepath1, String filepath2, Element root){
		Element child = buildMapEle(mapfile, filepath1, filepath2);
		root.appendChild(child);
		return saveIndex();
	}
	
	private String getMapFolderPath(){
		return this.mapfoldere.getPath()+Tools_System.getFilepathSeparator();
	}
	
	private Node getRootNode(){
		NodeList list = this.mapindex.getChildNodes();
		if(list.getLength() == 0){
			Logger.getRootLogger().error("XML incorrectly formatted, missing root node"); 
			return null;
		}
		else return list.item(0);
	}
	
}

