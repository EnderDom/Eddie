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

import tools.Tools_System;
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
	String[] tableheadings;
	String[] actualheadings;
	private boolean[] shown;
	private static String whitespace = "___";
	Document data;
	private String workspace;
	public static String filename = "file_list.xml";
	EddieGUI gui;
	int colcount=0;
	int rowcount=0;
	String[][] cols;
	
	public FileViewerModel(EddieGUI gui, FileViewer view){
		tableheadings = new String[]{"Name", "File"+whitespace+"Location", "Information", "File"+whitespace+"Type","Date"+whitespace+"Added"};
		actualheadings = tableheadings;
		this.gui = gui;
		this.workspace = this.gui.load.getWorkspace();
		if(this.workspace == null)Logger.getRootLogger().error("Error, null workspace");
		load();
		view.repaint();
	}
	
	private void load(){
		File file = getFile();
		if(file.exists()){
			loadFile(file);
		}
		else{
			createDefaultFile(file);
			loadFile(file);
		}
	}
	
	private File getFile(){
		return new File(this.workspace + System.getProperty("file.separator") + filename);
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
				saveFile(getFile());
				addRow(filedata);
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
			Element files = data.createElementNS(null,"FILES");
			root.appendChild(files);
			//DUMMY DATA FOR TESTING REMOVE
			Logger.getRootLogger().info("This is here for testing. TO BE REMOVED");
			addFileData(this.tableheadings);
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
			Logger.getRootLogger().debug("Loading XML file into object...");
			this.data = Tools_XML.inputStreamToDocument(new FileInputStream(file));
			parseHeaderData();
			parseFileData();
			Logger.getRootLogger().debug("Completed loading XML file list");
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
	
	private void parseHeaderData(){
		NodeList list2 = this.data.getElementsByTagName("HEADING");
		if(list2.getLength() != 0){
			this.actualheadings = new String[list2.getLength()];
			this.shown = new boolean[list2.getLength()];
			int tablecount =0;
			for(int i =0; i < list2.getLength(); i++){
				if(list2.item(i).hasAttributes()){
					if(list2.item(i).getAttributes().item(0).getTextContent().equals("TRUE")){
						shown[i] = true;
						this.actualheadings[tablecount] = list2.item(i).getTextContent();
						tablecount++;
					}
					else{
						Logger.getRootLogger().debug("Table Heading " + list2.item(i).getTextContent() + " found but hidden");
					}
				}
				else{
					Logger.getRootLogger().warn("Incorrect XML File List, should include SHOWN='' value in tag space");
				}
			}
			String[] temp = new String[tablecount];
			for(int i =0; i < tablecount ;i ++ )temp[i] = actualheadings[i].replaceAll("___", " ");
			actualheadings = temp;
		}
		else{
			Logger.getRootLogger().error("Missing XML Header Names");
		}
	}
	
	private void parseFileData(){
		NodeList list2 = this.data.getElementsByTagName("FILE");
		cols = new String[actualheadings.length][list2.getLength()];
		for(int i =0; i < list2.getLength(); i++){
			String[] array = pullArray(list2.item(i));
			for(int j=0; j < array.length; j++)cols[j][i]=array[j];
		}
	}
	
	private String[] pullArray(Node node){
		String[] array = new String[actualheadings.length];
		int arraycount=0;
		NodeList list = node.getChildNodes();
		for(int i = 0; i < list.getLength(); i++){
			for(int j =0; j < this.tableheadings.length; j++){
				if(this.shown[j]){
					if(list.item(i).getNodeName().equals(this.tableheadings[j]) ){
						if(arraycount != array.length){
							array[arraycount] = list.item(i).getTextContent().replaceAll(whitespace, " ");
							arraycount++;
						}
						else{
							Logger.getRootLogger().error("This error means array enumeration has failed, this is due to a coding error, check src.");
						}
						break;
					}
				}
			}
			
		}
		return array;
	}
	
	public void saveFile(File file){
		Logger.getRootLogger().debug("Saving Default File XML");
		Tools_XML.Xml2File(this.data, file);
	}
	
	public void saveFile(String workspace){
		this.workspace = workspace;
	}
	
	public int getColumnCount() {
		if(cols == null){
			return 0;
		}
		else{
			return cols.length;
		}
	}

	public int getRowCount() {
		if(cols == null){
			return 0;
		}
		else{
			return cols[0].length;
		}
	}
	
	public void addRow(String[] data){
		String[][] data2 = new String[cols.length][cols[0].length+1];
		for(int i =0; i < cols.length; i++){
			for(int j = 0 ; j < cols[0].length; j++){
				data2[i][j] = cols[i][j];
			}
		}
		for(int i =0; i < cols.length; i++){
			data2[i][cols[0].length] = data[i];
		}
		this.cols = data2;
	}

	public Object getValueAt(int row, int col) {
		if(cols == null){
			return "PLACE HOLDER";
		}
		else{
			return cols[col][row];
		}
	}
	

	public String getColumnName(int column){
		return actualheadings[column];
	}
	
	public boolean isCellEditable(int row, int col){ 
		 return false; 
	}

	public void buildAndAddFile(String string) {
		File file = new File(string);
		Logger.getRootLogger().debug("Building file  "+ string);
		if(file.exists()){
			String[] filedata = new String[this.tableheadings.length];
			if(file.isFile()){
				String f1 = file.getName();
				int f2 = f1.lastIndexOf(".");
				/*
				 * ISSUE may arise if File already has "___" in file name or path
				 * Temporary workaround until I figure out how to get whitespace into XML
				 */
				if(f2 != -1 && f2 != f1.length()-1){
					filedata[0] = f1.substring(0,f2).replaceAll(" ", "___");
					filedata[3] = f1.substring(f2+1, f1.length()).replaceAll(" ", "___");
				}
				else{
					filedata[0] = f1.replaceAll(" ", "___");
					filedata[3] = "Unknown";
				}
				filedata[1] = file.getPath().replaceAll(" ", "___");
				filedata[2] = "N_A_";
				filedata[4] = Tools_System.getDateNow();
				addFileData(filedata);
			}
			else if(file.isDirectory()){
				Logger.getRootLogger().warn("Directories not yet supported");
			}
			else{
				Logger.getRootLogger().error("Filesystem supports more than just file or folder???");
			}
			
		}
		else{
			Logger.getRootLogger().error("File added is not a file!");
		}
		
		
	}
	 
}
