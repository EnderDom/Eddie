package enderdom.eddie.gui.viewers;

import enderdom.eddie.gui.EddieGUI;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


import enderdom.eddie.modules.Module;

import org.apache.log4j.Logger;

import enderdom.eddie.ui.UI;
import enderdom.eddie.cli.EddieCLI;

//contains a list of all files available

public abstract class Viewer extends JInternalFrame implements TableModelListener, Module, MouseListener{

	protected static final long serialVersionUID = 1L;
	protected ViewerTable table;
	protected JScrollPane spane;
	protected ViewerModel model;
	public String[] actions;
	protected String modulename;
	Logger logger = Logger.getLogger(this.getClass().getName());
	MouseListener listener;
	
	public Viewer(String title ,boolean a, boolean b, boolean c, boolean d){
		super(title,a,b,c,d);
		modulename = this.getClass().getName();
	}
	
	public void init(){
		this.pack();
	}

	public void tableChanged(TableModelEvent e) {
		Logger.getRootLogger().debug("Have not yet implemented");
		System.out.println(e.toString());
	}
	
	/*******************************************************************
	 * 
	 * 
	 * Module Related Methods
	 * 
	 * 
	 *******************************************************************/

	public void actOnAction(String s, EddieGUI gui) {

	}

	public void actOnTask(String s, UI ui) {
		//Not needed
		
	}

	public void printTasks() {
		//Not needed
		
	}

	public void addToGui(EddieGUI gui) {
		//Build GUI
		
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
	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {
		logger.trace("Button clicked"+arg0.getButton());
		if(arg0.isPopupTrigger() || arg0.getButton() == MouseEvent.BUTTON2){
			Point p = arg0.getPoint();
			int rowz = table.rowAtPoint(p);
			logger.trace("Viewer registered a popup activating click @ row "+rowz);
			if(rowz!=-1){
				
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}

	public ViewerModel getViewerModel(){
		return this.model;
	}
	
	public ViewerTable getViewerTable(){
		return this.table;
	}
	
}

