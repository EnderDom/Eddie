package enderdom.eddie.gui;

import enderdom.eddie.gui.viewers.file.FileViewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

public class FileOptions implements ActionListener{

	public String[] menunames;
	public String[] filetypes;
	public String[] classnames;
	int filetype =-1;
	Logger logger = Logger.getRootLogger();
	JPopupMenu menu;
	FileViewer viewer;
	
	public FileOptions(FileViewer viewer){
		this.viewer = viewer;
		menu = new JPopupMenu("File Menu Options");
		menu.addMouseListener(viewer);
		buildDefaultMenuItems();
	}
	
	public void reset(String filetypea){
		menu.removeAll();
		
		if(filetypes != null){
			for(int i =0; i < filetypes.length; i++){
				if(filetypes[i].equals(filetypea)){
					filetype =i;
					logger.debug("Filetype "+filetypea+" is recognised");
					break;
				}
			}
		}
		buildDefaultMenuItems();
		buildFileTypeMenuItems(filetypea);
		menu.pack();
	}
	
	private void buildDefaultMenuItems(){
		logger.trace("Default Menu Built");
		JMenuItem item1 = new JMenuItem("Open");
		//JMenuItem item2 = new JMenuItem("Convert to...");
		JMenuItem item3 = new JMenuItem("Remove");
		item1.addActionListener(this);
		//item2.addActionListener(this);
		item3.addActionListener(this);
		menu.add(item1);
		//menu.add(item2);
		menu.add(item3);
		menu.addSeparator();
	}
	
	private void buildFileTypeMenuItems(String filetypea){
		
	}
	
	public boolean isValid(){
		if(filetype ==-1)return false;
		else return true;
	}

	public void itemStateChanged(ItemEvent itemevent) {
		logger.debug("Item event:" + itemevent.paramString());
	}
	
	public void setVisible(boolean value){
		this.menu.setVisible(value);
	}
	
	public void show(Component c, int x, int y){
		this.menu.show(c, x, y);
	}
	
	public JPopupMenu getMenu(){
		return this.menu;
	}

	public void actionPerformed(ActionEvent actionevent) {
		logger.trace("Action: " + actionevent.getActionCommand());
		if(actionevent.getActionCommand().equals("Remove")){
			viewer.removeFile();
		}
		else{
			logger.warn("Action not recognised");
		}
	}
	
}
