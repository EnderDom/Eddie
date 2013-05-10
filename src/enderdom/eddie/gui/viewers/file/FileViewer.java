package enderdom.eddie.gui.viewers.file;

import enderdom.eddie.gui.EddieGUI;
import enderdom.eddie.gui.FileAdderer;
import enderdom.eddie.gui.FileOptions;
import enderdom.eddie.gui.FileReciever;
import enderdom.eddie.gui.viewers.Viewer;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_Modules;

//contains a list of all files available

public class FileViewer extends Viewer implements FileReciever, DropTargetListener, DragSourceListener, DragGestureListener{

	/**
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane spane;
	public String[] actions;
	protected String modulename;
	Logger logger = Logger.getLogger(this.getClass().getName());
	FileOptions ops;
	MouseListener listener;
	@SuppressWarnings("unused")
	private DropTarget dropTarget = new DropTarget (this, this);
	private DragSource dragSource = DragSource.getDefaultDragSource();
	
	public FileViewer(){
		super("File List",true,false,true,true);
		table = new FileViewerTable(this);
		ops = new FileOptions(this);
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
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


	public void addToGui(EddieGUI gui) {
		//Build GUI
		this.setSize(300, 600);
		actions = new String[1];
		
		model = new FileViewerModel(gui, this);
		table.setModel(this.model);
		spane = new JScrollPane(table);
		this.add(spane);
		spane.addMouseListener(this);
		
		//Set up menu items
		JMenuItem menuItem = new JMenuItem("Add File...");
		menuItem.setActionCommand(modulename+"_ADD_FILE");
	    menuItem.addActionListener(gui);
		Tools_Modules.add2JMenuBar(gui.getJMenuBar(), menuItem,"File", true);
		actions[0] = modulename+"_ADD_FILE";
		//This module is automatically started, so add to desktop and init
		init();
		gui.add2Desktop(this);
	}

	public String[] getActions() {
		return actions;
	}


	public void resetModuleName(String name) {
		this.modulename = name;
	}	
	
	public void sendFiles(String[] files){
		Logger.getRootLogger().debug("Files have been sent to me: " + this.getClass().getName());
		for(int i =0 ; i < files.length ; i++){
			model.build(files[i]);
		}
		this.model.fireTableDataChanged();
	}
	

	public void mousePressed(MouseEvent arg0) {
		logger.trace("Button clicked"+arg0.getButton());
		if(arg0.isPopupTrigger() || SwingUtilities.isRightMouseButton(arg0)){
			
			Point p = arg0.getPoint();
			int rowz = table.rowAtPoint(p);
			logger.trace("FileViewer registered a popup activating click @ row "+rowz);
			if(rowz!=-1){
				//ops.reset(model.getFileTypeAt(rowz));
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

	public void removeFile(){
		getViewerModel().removeRows(this.getViewerTable().getSelectedRows());
		this.repaint();
	}

	public void dragDropEnd(DragSourceDropEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragEnter(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragExit(DragSourceEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragOver(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dropActionChanged(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragGestureRecognized(DragGestureEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void drop(DropTargetDropEvent arg0) {
		try{
			Transferable tr = arg0.getTransferable();
			if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)){
				arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object o =  tr.getTransferData(DataFlavor.javaFileListFlavor);
				logger.debug("DRAGNDROP Request:" + o.toString());
				String s = o.toString().replace("[","");
				s = s.replace("]","");
				String[] files = s.split(",");
				for(int i =0; i < files.length; i++){
					File file = new File(files[i].trim());
					if(file.exists()){
						this.getViewerModel().build(file.getPath());
						this.model.fireTableDataChanged();
					}
					else{
						logger.error("File dropped not an actual file " + file.getPath());
					}
				}
				this.model.saveFile();
				arg0.getDropTargetContext().dropComplete(true);
			}
			else{
				logger.error("Rejected drop event");
				arg0.rejectDrop();
		   	}
		}
		catch(Exception ex){
			logger.error("Problem occured with dnd operation " , ex);
		}
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}

