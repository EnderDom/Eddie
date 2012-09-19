package enderdom.eddie.gui;

import enderdom.eddie.gui.utilities.ErrorFrame;
import enderdom.eddie.gui.utilities.PropertyFrame;
import enderdom.eddie.gui.viewers.file.FileViewer;

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
import javax.swing.JPasswordField;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.*;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_UI;
import enderdom.eddie.ui.ModuleManager;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.TaskManager;
import enderdom.eddie.ui.UI;
import enderdom.eddie.ui.UIEvent;

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
    private FileViewer view;
    public DatabaseManager dbmanager;
    public static String iconpath = "eddie.png";
    public static String quizicon = "eddie_question.png";
    private PropertyFrame propsframe;
    
    
	public EddieGUI(PropertyLoader loader){
		super("Eddie v"+loader.getValue("GUIVERSION") + " " + loader.getValue("EDITION") + " Edition");
		Double dx = Tools_String.parseString2Double(loader.getValue("GUIVERSION"));
		if(dx != null)this.version=dx; 
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
		//load.loadPropertiesGUI(this);
		Tools_UI.changeLnF(load.getValue("PREFLNF"), this);
		this.setIconImage(new ImageIcon(getClass().getResource(iconpath)).getImage());
		
		/*
		 * This needs to be started before modmanager,
		 * as modules will want to register classes with it 
		 */
		view = new FileViewer();
		
		
		//Module Build
		modmanager = new ModuleManager(load.getValue("MODFOLDER"));
     
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
		
		modmanager.addPrebuiltModule("FILEVIEWER", view, this);
		
		//modmanager.addPrebuiltModule("PROPERTYLOADER", load, this);
		modmanager.addPrebuiltModule("MYSELF", modmanager, this);
		
		Logger.getRootLogger().debug("Set EddieGUI to Visible");
		//Finish
		pack();
		setVisible(true);
	}

	public void exit(){
		int i = JOptionPane.showConfirmDialog(this, "Exit Eddie v"+(load.getValue("ENGINEVERSION")+load.getValue("GUIVERSION"))+"?", "Exit?", JOptionPane.YES_NO_OPTION);
		if(i !=1){
			
			load.savePropertyFile((new StringBuilder(String.valueOf(load.getValue("ROOTFOLDER")))).append(load.getValue("PROPERTYFILENAME")).toString(), load.getPropertyObject());
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
		task.parseOpts(this.load.getPropertyObject());
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
		Integer core = Tools_String.parseString2Int(load.getValueOrSet("CORETHREAD", "1"));
		Integer auxil = Tools_String.parseString2Int(load.getValueOrSet("AUXILTHREAD","5"));
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
	
	public PropertyLoader getPropertyLoaderXT(){
		return this.load;
	}

	public boolean isGUI() {
		return true;
	}
	
	public void sendAlert(String str){
		JOptionPane.showMessageDialog(this, str);
	}
	
	public void alert(String str){
		this.sendAlert(str);
	}
	
	public void sendFiles(String[] files){
		Logger.getRootLogger().warn("This is method is called when fileadderer is not parented by a module");
	}

	public void fireUIEvent(UIEvent evt) {
		logger.debug("UI event caught, nothing yet actually added");
	}

	public String requiresUserInput(String message, String title) {
		return JOptionPane.showInternalInputDialog(this.desktop, message, title, JOptionPane.QUESTION_MESSAGE);
	}
	
	public String requiresUserPassword(String message, String title) {
		JPasswordField pwd = new JPasswordField(50);
		if(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC){
			//Workaround for bug 6801620
			pwd.enableInputMethods(true);
		}
		return (JOptionPane.showConfirmDialog(this, pwd, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) ?
		new String(pwd.getPassword()) : null;
	}
	
	public int requiresUserYNI(String message, String title) {
		return JOptionPane.showInternalConfirmDialog(this.desktop, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
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
	
	public void throwError(String message, Throwable t){
		logger.error(message,t);
		ErrorFrame frame = new ErrorFrame(message, t);
		this.add2Desktop(frame);
		logger.debug("Frame Opened");
	}
	
	public void throwError(String message, String[] details){
		logger.error(message);
		ErrorFrame frame = new ErrorFrame(message, details);
		this.add2Desktop(frame);
		logger.debug("Frame Opened");
	}
	
	public void error(String message, Throwable t){
		throwError(message, t);
	}
	
	public void error(String message){
		logger.error(message);
		ErrorFrame frame = new ErrorFrame(message);
		this.add2Desktop(frame);
		logger.debug("Frame Opened");
	}
	
	public void refreshDesktop(){
		this.desktop.resetBackground();
	}
}