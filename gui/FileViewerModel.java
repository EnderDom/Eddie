package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.Tools_XML;


public class FileViewerModel extends AbstractTableModel{

	/**
	 * 
	 * Hold data in Document....
	 * Not actually sure if this is a good idea or
	 * not, I am struggling to find information on
	 * the difference between holding the data as 
	 * objects and holding it inside Document
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static String[] tableheadings = new String[]{"Name", "File___Location", "Information", "File___Type","Date___Added"};
	private static String whitespace = "___";
	Document data;
	private String workspace;
	public static String filename = "file_list.xml";
	EddieGUI gui;
	int colcount=0;
	int rowcount=0;
	String[][] cols;
	
	public FileViewerModel(EddieGUI gui){
		this.gui = gui;
		this.workspace = this.gui.load.getWorkspace();
		if(this.workspace == null)Logger.getRootLogger().error("Error, null workspace");
		load();
	}
	
	private void load(){
		File file = new File(this.workspace + System.getProperty("file.separator") + filename);
		if(file.exists()){
			loadFile(file);
		}
		else{
			createDefaultFile(file);
		}
	}
	
	public void addFileData(String[] filedata){
		if(filedata.length == tableheadings.length){
			Node root = getRoot();
			NodeList list = data.getElementsByTagName("FILES");
			if(list.getLength() == 0){
				Element files = data.createElementNS(null,"FILES");
				root.appendChild(files);
				Element e = data.createElementNS(null, "FILE");
				e = buildElement(filedata,e);
				files.appendChild(e);
			}
			else{
				Node files = list.item(0);
				Element e = data.createElementNS(null, "FILE");
				e = buildElement(filedata,e);
				files.appendChild(e);
			}
		}
		else{
			Logger.getRootLogger().error("File Data is not the same size as the number of headers!");
		}
	}
	
	private Element buildElement(String[] filedata, Element e){
		Element e2;
		for(int i =0; i < filedata.length; i++){
			e2 = data.createElementNS(null, tableheadings[i]);
			e2.setTextContent(filedata[i]);
			e.appendChild(e2);
		}
		e2 = data.createElementNS(null, "metadata");
		e2.setTextContent("");
		e.appendChild(e2);
		return e;
	}
	
	private void createDefaultFile(File file){
		try {
			Logger.getRootLogger().debug("Building Default File XML");
			DocumentBuilder build = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			data = build.newDocument();
			Element root = data.createElement("xml");
			data.appendChild(root);
			Element header = data.createElementNS(null,"HEADERS");
			Element e;
			for(int i =0 ; i < tableheadings.length ; i++){
			// Child i.
				  e = data.createElementNS(null, "HEADING");
				  e.setAttributeNS(null, "SHOWN", "TRUE");
				  e.setTextContent(tableheadings[i]);
				  header.appendChild(e);
			}
			root.appendChild(header);		
			Logger.getRootLogger().debug("Built XML");
			saveFile(file);
		} 
		catch (ParserConfigurationException e) {
			Logger.getRootLogger().error("Failed to Build Default File List XML",e);
		}
	}
	
	private void loadFile(File file){
		boolean load = false;
		try {
			this.data = Tools_XML.inputStreamToDocument(new FileInputStream(file));
			Logger.getRootLogger().debug("Loaded XML file list");
			load = true;
		} catch (FileNotFoundException e) {
			Logger.getRootLogger().error("File.. why you no exist??",e);
		} catch (ParserConfigurationException e) {
			Logger.getRootLogger().error("File.. why you no parse??",e);
		} catch (SAXException e) {
			Logger.getRootLogger().error("File.. why you no XML??",e);
		} catch (IOException e) {
			Logger.getRootLogger().error("File.. why you no input or output something??",e);
		}
		if(!load){
			gui.sendAlert("Corrupt file list XML. Building default...");
			createDefaultFile(file);
		}
	}
	
	public Node getRoot(){
		NodeList list = this.data.getChildNodes();
		if(list.getLength() == 0){ 
			Logger.getRootLogger().error("XML incorrectly formatted, missing root node"); 
			return null;
		}
		else return list.item(0);
	}
	
	public void saveFile(File file){
		Logger.getRootLogger().debug("Saving Default File XML");
		Tools_XML.Xml2File(this.data, file);
	}
	
	public void saveFile(String workspace){
		this.workspace = workspace;
	}
	
	public int getColumnCount() {
		return colcount;
	}

	public int getRowCount() {
		return rowcount;
	}

	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return new String("Test Arg");
	}

	public String getColumnName(int column){
		return tableheadings[column];
	}
	 public boolean isCellEditable(int row, int col){ 
		 return true; 
	 }
	 
	 
}
