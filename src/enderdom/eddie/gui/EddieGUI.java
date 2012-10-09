package enderdom.eddie.gui;

import enderdom.eddie.gui.utilities.PropertyFrame;
import enderdom.eddie.gui.viewers.file.FileViewer;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.ui.ModuleManager;
import enderdom.eddie.ui.PropertyLoader;


public class EddieGUI extends GenericGUI{

    private static final long serialVersionUID = 1L;
    JRadioButtonMenuItem appears[];
    ModuleManager modmanager;
    public static boolean testmode = true;
    public double version;
    
    private FileViewer view;
    public DatabaseManager dbmanager;
    public static String iconpath = "eddie.png";
    public static String quizicon = "eddie_question.png";
    private PropertyFrame propsframe;
    private static String program = "Eddie";
    
	public EddieGUI(PropertyLoader loader){
		super(loader, program);
	}
	
	public void init(){
		//View Size
		this.setIconImage(new ImageIcon(getClass().getResource(iconpath)).getImage());
		/*
		 * This needs to be started before modmanager,
		 * as modules will want to register classes with it 
		 */
		view = new FileViewer();
		
		//Module Build
		modmanager = new ModuleManager(load.getValue("MODFOLDER"));
		
		Logger.getRootLogger().info("EddieGUI Constructed");
		
	
		modmanager.init();
		modmanager.setupGUI(this);
		
		modmanager.addPrebuiltModule("FILEVIEWER", view, this);
		
		//modmanager.addPrebuiltModule("PROPERTYLOADER", load, this);
		modmanager.addPrebuiltModule("MYSELF", modmanager, this);
		
		Logger.getRootLogger().debug("Set EddieGUI to Visible");
		//Finish
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) { 	
	    if ("quit".equals(e.getActionCommand())) { //quit
	    	exit();
	    }
	    else if(modmanager.isAction(e.getActionCommand())){
	    	modmanager.runAction(this, e.getActionCommand());
		}
	    else{
	    	Logger.getRootLogger().warn("Unattached Action Performed" + e.getActionCommand());
	    }
	 }

	public void addAction(String action, String classpath){
		this.modmanager.addAction(action, classpath);
	}

	
	public void sendFiles(String[] files){
		Logger.getRootLogger().warn("This is method is called when fileadderer is not parented by a module");
	}

	
	public DatabaseManager getDatabaseManager(){
		if(this.dbmanager == null){
			this.dbmanager = new DatabaseManager(this);
		}
		return this.dbmanager;
	}
	
	//Returns the default DatabaseManager
	public DatabaseManager getDatabaseManager(String password){
		if(this.dbmanager == null){
			this.dbmanager = new DatabaseManager(this, password);
		}
		return this.dbmanager;
	}
	
	public void setDatabaseManager(DatabaseManager dbmanager){
		if(this.dbmanager != null){
			logger.warn("This UI already has a manager, you know jsut warning you");
		}
		this.dbmanager = dbmanager;
	}
	
	public void setPropertyFrame(PropertyFrame frame){
		if(this.propsframe != null){
			if(!this.propsframe.isClosed()){
				this.propsframe.dispose();
			}
		}
		this.propsframe = frame;
		this.add2Desktop(this.propsframe);
	}
	
	public PropertyFrame getPropertyFrame(){
		return this.propsframe;
	}

	public void refreshDesktop(){
		this.desktop.resetBackground();
	}
}
