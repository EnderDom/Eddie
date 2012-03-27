package gui;

import javax.swing.JTable;

public class FileViewerTable extends JTable {

	/**
	 * 
	 */
	FileViewer parent;
	boolean fillsViewportHeight;
	
	private static final long serialVersionUID = 1L;

	public FileViewerTable(FileViewer view){
		super();
		this.parent = view;
	}

	 public void setFillsViewportHeight(boolean fillsViewportHeight) {
	        boolean old = this.fillsViewportHeight;
	        this.fillsViewportHeight = fillsViewportHeight;
	        resizeAndRepaint();
	        firePropertyChange("fillsViewportHeight", old, fillsViewportHeight);
	 }
	
}
