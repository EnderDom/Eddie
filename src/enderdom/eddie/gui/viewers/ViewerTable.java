package enderdom.eddie.gui.viewers;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;

import org.apache.log4j.Logger;


public abstract class ViewerTable  extends JTable implements MouseListener{
	/**
	 * 
	 */
	Viewer parent;
	boolean fillsViewportHeight;
	
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public ViewerTable(Viewer view){
		super();
		this.parent = view;
		addMouseListener(this);
	}
	
	 public void setFillsViewportHeight(boolean fillsViewportHeight) {
		 boolean old = this.fillsViewportHeight;
		 this.fillsViewportHeight = fillsViewportHeight;
		 resizeAndRepaint();
		 firePropertyChange("fillsViewportHeight", old, fillsViewportHeight);
	 }
	
	 
	//Just forwards to parent 
	public void mouseClicked(MouseEvent mouseevent) {

	}
	
	public void mouseEntered(MouseEvent mouseevent) {

	}
	
	public void mouseExited(MouseEvent mouseevent) {

	}
	
	public void mousePressed(MouseEvent mouseevent) {
		parent.mousePressed(mouseevent);
	}
	
	public void mouseReleased(MouseEvent mouseevent) {

	}

}
