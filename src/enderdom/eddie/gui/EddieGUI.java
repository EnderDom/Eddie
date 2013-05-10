package enderdom.eddie.gui;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskLike;
import enderdom.eddie.ui.PropertyLoader;


public class EddieGUI extends GenericGUI{

    private static final long serialVersionUID = 1L;
    JRadioButtonMenuItem appears[];
    public static boolean testmode = true;
    public double version;
   
    public DatabaseManager dbmanager;
    public static String iconpath = "eddie.png";
    public static String quizicon = "eddie_question.png";
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
		Logger.getRootLogger().info("EddieGUI Constructed");		
		//Finish
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) { 	
	    if ("quit".equals(e.getActionCommand())) { //quit
	    	exit();
	    }
	    else{
	    	Logger.getRootLogger().warn("Unattached Action Performed" + e.getActionCommand());
	    }
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

	public void refreshDesktop(){
		this.desktop.resetBackground();
	}

	public void addTaskLike(TaskLike task) {
		Logger.getRootLogger().debug("GUI recieve task, sending to task manager...");
		if(this.manager == null){
			buildTaskManager();
		}
		if(task.wantsUI())task.addUI(this);
		this.manager.addTask(task);
	}

	
}