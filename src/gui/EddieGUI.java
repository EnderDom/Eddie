package gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

import tasks.*;
import tools.Tools_UI;
import tools.Tools_String;
import tools.Tools_System;
import ui.ModuleManager;
import ui.PropertyLoader;
import ui.TaskManager;
import ui.UI;
import ui.UIEvent;
import ui.UIEventListener;

public class EddieGUI extends JFrame implements ActionListener, WindowListener, UI, FileReciever{

    private static final long serialVersionUID = 1L;
    public PropertyLoader load;
    private JMenuBar menu;
    JRadioButtonMenuItem appears[];
    TaskManager manager;
    ModuleManager modmanager;
    public static boolean testmode = true;
    public double version;
    private DesktopPane desktop;
    FileViewer view;
    public DatabaseManager dbmanager;
    private static String iconpath = "eddie.png";
    
    
	public EddieGUI(PropertyLoader loader){
		super("Eddie v"+PropertyLoader.guiversion + " " + PropertyLoader.edition + " Edition");
		this.version= PropertyLoader.guiversion;
		System.out.println("Eddie v" + version + " by (S.C.Corp.)");
		
		//View Size
		Dimension d =Toolkit.getDefaultToolkit().getScreenSize();
		//-20 as screen size includes menu bars etc..
		d.setSize(d.getWidth(), d.getHeight()-20);
		this.setBounds(new Rectangle(d));
		this.setPreferredSize(this.getBounds().getSize());
		
		//Centres frame
		this.setLocationRelativeTo(null);
		
		//Load Properties
		load = loader;
		load.loadPropertiesGUI(this);
		Tools_UI.changeLnF(load.getLastLNF(), this);
		this.setIconImage(new ImageIcon(getClass().getResource(iconpath)).getImage());
		
		//Module Build
		modmanager = new ModuleManager(load.getModuleFolder());
     
		//Options
		addWindowListener(this);
		setJMenuBar((this.menu = createMenuBar()));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		Logger.getRootLogger().info("EddieGUI Constructed");
		//Create Desktop
		desktop = new DesktopPane();
		desktop.setSize(this.getBounds().getSize());
		setContentPane(desktop);
	
		modmanager.init();
		modmanager.setupGUI(this);	
		modmanager.addPrebuiltModule("PROPERTYLOADER", load, this);
		modmanager.addPrebuiltModule("MYSELF", modmanager, this);
		
		view = new FileViewer();
		modmanager.addPrebuiltModule("FILEVIEWER", view, this);

		Logger.getRootLogger().debug("Set EddieGUI to Visible");
		//Finish
		pack();
		setVisible(true);
	}

	public void exit(){
		int i = JOptionPane.showConfirmDialog(this, "Exit EddieGUI v"+version+"?", "Exit?", JOptionPane.YES_NO_OPTION);
		if(i !=1){
			
			PropertyLoader.save((new StringBuilder(String.valueOf(load.rootfolder))).append(PropertyLoader.propertyfilename).toString(), load.getProps());
			view.saveFile(load.getWorkspace());
	        Logger.getRootLogger().info((new StringBuilder("Closing Eddie @ ")).append(Tools_System.getDateNow()).toString());
	        LogManager.shutdown();
	        setVisible(false);
	        dispose();
		}
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
	
	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		menuBar = buildFileMenu(menuBar);
		menuBar = buildEditMenu(menuBar);
		return menuBar;
	}
	
	private JMenuBar buildEditMenu(JMenuBar menuBar){
		JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
       
        return menuBar;
	}
	
	private JMenuBar buildFileMenu(JMenuBar menuBar){
		JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);       
		return menuBar;
	}
	
	public void addTask(Task task){
		if(this.manager == null){
			buildTaskManager();
		}
		//TODO
		if(task.wantsUI())task.addUI(this);
		task.parseOpts(this.load.getProps());
		this.manager.addTask(task);
		if(!this.manager.isStarted()){
			this.manager.run();
		}
	}
	
	public void addAction(String action, String classpath){
		this.modmanager.addAction(action, classpath);
	}

	public void windowOpened(WindowEvent paramWindowEvent) {
	}

	public void windowClosed(WindowEvent paramWindowEvent) {
	}

	public void windowIconified(WindowEvent paramWindowEvent) {
	}

	public void windowDeiconified(WindowEvent paramWindowEvent) {
	}

	public void windowActivated(WindowEvent paramWindowEvent) {
	}

	public void windowDeactivated(WindowEvent paramWindowEvent) {
	}

	public void windowClosing(WindowEvent paramWindowEvent) {
		exit();
	}
	
	public void update(Task task) {
		//
	}

	public void buildTaskManager() {
		Integer core = Tools_String.parseString2Int(load.getCore());
		Integer auxil = Tools_String.parseString2Int(load.getAuxil());
		if(core == null){
			core = 1;
			logger.error("Something has gone horribly wrong");
		}
		if(auxil == null){
			auxil =5;
			logger.error("Something has gone horribly wrong");
		}
	}

	public JMenuBar getMenu() {
		return menu;
	}

	public void setMenu(JMenuBar menu) {
		this.menu = menu;
	}
	
	public void add2Desktop(JInternalFrame frame){
		this.desktop.add(frame);
		try {
	        frame.setSelected(true);
	    }
		catch (java.beans.PropertyVetoException e) {
			Logger.getRootLogger().warn("Frame Selection Warning",e);
		}
		frame.setVisible(true);
	}
	
	public PropertyLoader getPropertyLoader(){
		return this.load;
	}

	public boolean isGUI() {
		return true;
	}
	
	public void sendAlert(String str){
		JOptionPane.showMessageDialog(this, str);
	}
	
	public void sendFiles(String[] files){
		Logger.getRootLogger().warn("This is method is called when fileadderer is not parented by a module");
	}
	
	public void addUIEventListener(UIEventListener listener) {
		listenerList.add(UIEventListener.class, listener);
	}

	public void removeUIEventListener(UIEventListener listener) {
		listenerList.remove(UIEventListener.class, listener);
	}

	public void fireUIEvent(UIEvent evt) {
		Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==UIEventListener.class) {
                ((UIEventListener)listeners[i+1]).UIEventOccurred(evt);
            }
        }
	}

	public String requiresUserInput(String message, String title) {
		return JOptionPane.showInternalInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
	}
	
	//TODO make password box
	public String requiresUserPassword(String message, String title) {
		return JOptionPane.showInternalInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
	}
	
	public int requiresUserYNI(String message, String title) {
		return JOptionPane.showInternalConfirmDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}
	
	//Returns the default DatabaseManager
	public DatabaseManager getDatabaseManager(){
		if(this.dbmanager == null){
			this.dbmanager = new DatabaseManager(this);
		}
		return this.dbmanager;
	}
	
	public void setDatabaseManager(DatabaseManager dbmanager){
		if(this.dbmanager != null){
			logger.warn("This UI already has a manager, you know jsut warning you");
		}
		this.dbmanager = dbmanager;
	}

}
