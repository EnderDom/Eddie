package gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


import modules.Module;

import org.apache.log4j.Logger;

import tools.Tools_Modules;
import ui.UI;
import cli.EddieCLI;

//contains a list of all files available

public class FileViewer extends JInternalFrame implements TableModelListener, Module, FileReciever, MouseListener{

	/**
	 */
	private static final long serialVersionUID = 1L;
	private FileViewerTable table;
	private JScrollPane spane;
	private FileViewerModel model;
	public String[] actions;
	protected String modulename;
	Logger logger = Logger.getLogger(this.getClass().getName());
	FileOptions ops;
	MouseListener listener;
	
	public FileViewer(){
		super("File List",true,false,true,true);
		modulename = this.getClass().getName();
		ops = new FileOptions(this);
	}
	
	public void init(){
		this.pack();
	}

	public void tableChanged(TableModelEvent e) {
		Logger.getRootLogger().debug("Have not yet implemented");
		System.out.println(e.toString());
	}
	
	public void saveFile(String workspace){
		this.model.saveFile(workspace);
	}
	
	/*******************************************************************
	 * 
	 * 
	 * Module Related Methods
	 * 
	 * 
	 *******************************************************************/

	public void actOnAction(String s, EddieGUI gui) {
		if(s.contentEquals(actions[0])){
			FileAdderer fileinput = new FileAdderer(gui);
			fileinput.setFileReciever(this);
			gui.add2Desktop(fileinput);
		}
	}

	public void actOnTask(String s, UI ui) {
		//Not needed
		
	}

	public void printTasks() {
		//Not needed
		
	}

	public void addToGui(EddieGUI gui) {
		//Build GUI
		this.setSize(300, 600);
		actions = new String[1];
		table = new FileViewerTable(this);
		model = new FileViewerModel(gui, this);
		table.setModel(this.model);
		spane = new JScrollPane(table);
		this.add(spane);
		spane.addMouseListener(this);
		
		//Set up menu items
		JMenuItem menuItem = new JMenuItem("Add File...");
		menuItem.setActionCommand(modulename+"_ADD_FILE");
		gui.addAction(modulename+"_ADD_FILE",modulename);
	    menuItem.addActionListener(gui);
		Tools_Modules.add2JMenuBar(gui.getJMenuBar(), menuItem,"File", true);
		actions[0] = modulename+"_ADD_FILE";
		//This module is automatically started, so add to desktop and init
		init();
		gui.add2Desktop(this);
	}

	public void addToCli(EddieCLI cli) {
		//Not needed
	}

	public String[] getActions() {
		return actions;
	}

	public String[] getTasks() {
		//Not needed
		return null;
	}

	public boolean isPersistant() {
		return true;
	}

	public void resetModuleName(String name) {
		this.modulename = name;
	}	
	
	public void sendFiles(String[] files){
		Logger.getRootLogger().debug("Files have been sent to me: " + this.getClass().getName());
		for(int i =0 ; i < files.length ; i++){
			this.model.buildAndAddFile(files[i]);
		}
		this.model.fireTableDataChanged();
	}
	
	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {
			if(arg0.isPopupTrigger() || arg0.getButton() == MouseEvent.BUTTON2){
				Point p = arg0.getPoint();
				int rowz = table.rowAtPoint(p);
				logger.trace("FileViewer registered a popup activating click @ row "+rowz);
				if(rowz!=-1){
					ops.reset(model.getFileTypeAt(rowz));
					ops.show(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}

	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}

}

