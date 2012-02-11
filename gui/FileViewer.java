package gui;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

//contains a list of all files available

public class FileViewer extends JInternalFrame implements TableModelListener{

	/**
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane spane;
	private FileViewerModel model;
	
	
	public FileViewer(){
		table = new JTable();
		model = new FileViewerModel();
		table.setModel(this.model);
		spane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		this.add(spane);
		this.pack();
		this.setVisible(true);
	}

	public void tableChanged(TableModelEvent e) {
		Logger.getRootLogger().debug("Have not yet implemented");
		System.out.println(e.toString());
	}
	
}
