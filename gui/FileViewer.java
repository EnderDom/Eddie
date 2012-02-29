package gui;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import modules.Module;

import org.apache.log4j.Logger;

import tools.Tools_Modules;
import ui.UI;
import cli.EddieCLI;

//contains a list of all files available

public class FileViewer extends JInternalFrame implements TableModelListener, Module{

	/**
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane spane;
	private FileViewerModel model;
	public String[] actions;
	protected String modulename;
	
	public FileViewer(){
		super("File List",true,false,true,true);
		modulename = this.getClass().getName();
	}
	
	public void init(){
		this.pack();
		this.setVisible(true);
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

	public void actOnAction(String s, EddieGUI biodesktopgui) {
		// TODO Auto-generated method stub
		//TODO set action
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
		table = new JTable();
		model = new FileViewerModel(gui);
		table.setModel(this.model);
		spane = new JScrollPane(table);
		this.add(spane);
		
		//Set up menu items
		JMenuItem menuItem = new JMenuItem("Add File...");
		menuItem.setActionCommand(modulename+"_ADD_FILE");
		gui.addAction(modulename+"_ADD_FILE",modulename);
	    menuItem.addActionListener(gui);
		Tools_Modules.add2JMenuBar(gui.getJMenuBar(), menuItem,"File", true);
		actions[0] = modulename+"_ADD_FILE";
		//This module is automatically started, so add to desktop and init
		gui.desktop.add(this);
		init();
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
}
