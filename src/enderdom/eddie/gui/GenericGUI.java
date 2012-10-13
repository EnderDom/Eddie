package enderdom.eddie.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import enderdom.eddie.gui.utilities.ErrorFrame;
import enderdom.eddie.tasks.Task;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_UI;
import enderdom.eddie.ui.EddiePropertyLoader;
import enderdom.eddie.ui.PropertyLoader;
import enderdom.eddie.ui.TaskManager;
import enderdom.eddie.ui.UI;
import enderdom.eddie.ui.UIEvent;

public abstract class GenericGUI extends JFrame implements ActionListener, WindowListener, UI, FileReciever{

	protected static final long serialVersionUID = 1L;
	protected JMenuBar menu;
	protected LinkedList<GUIObject> dock = new LinkedList<GUIObject>();
	protected PropertyLoader load;
	protected double version;
	protected DesktopPane desktop;
	private String programname;
	protected TaskManager manager;
	
	public GenericGUI(PropertyLoader loader, String programname){
		super(programname+" v"+loader.getValue("GUIVERSION") + " " + loader.getValue("EDITION") + " Edition");
		this.load = loader;
		this.programname = programname;
		Double dx = Tools_String.parseString2Double(loader.getValueOrSet("GUIVERSION", ""+EddiePropertyLoader.guiversion));
		if(dx != null)this.version=dx;
		System.out.println(programname+" v" + version + " by (S.C.Corp.)");
		Dimension d =Toolkit.getDefaultToolkit().getScreenSize();
		//-20 as screen size includes menu bars etc..
		d.setSize(d.getWidth(), d.getHeight()-20);
		this.setBounds(new Rectangle(d));
		this.setPreferredSize(this.getBounds().getSize());
		
		//Centres frame
		this.setLocationRelativeTo(null);
		
		//load.loadPropertiesGUI(this);
		Tools_UI.changeLnF(load.getValue("PREFLNF"), this);
		
		//Options
		addWindowListener(this);
		setJMenuBar((this.menu = createMenuBar()));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create Desktop
		desktop = new DesktopPane();
		desktop.setSize(this.getBounds().getSize());
		setContentPane(desktop);
		init();
	}
	
	public void init(){
		//View Size
		Logger.getRootLogger().debug("Set GUI to Visible");
		//Finish
		pack();
		setVisible(true);
	}
	
	public void exit(){
		int i = JOptionPane.showConfirmDialog(this, "Exit "+programname+" v"+load.getValue("GUIVERSION")+"?", "Exit?", JOptionPane.YES_NO_OPTION);
		if(i !=1){
			load.savePropertyFile((new StringBuilder(String.valueOf(load.getValue("ROOTFOLDER")))).append(load.getValue("PROPERTYFILENAME")).toString(), load.getPropertyObject());
	        Logger.getRootLogger().info((new StringBuilder("Closing "+programname+" @ ")).append(Tools_System.getDateNow()).toString());
	        LogManager.shutdown();
	        setVisible(false);
	        dispose();
		}
	}
	
	public void actionPerformed(ActionEvent e) { 	
        if ("quit".equals(e.getActionCommand())) { //quit
            exit();
        }
        else{
        	 Logger.getRootLogger().warn("Unattached Action Performed" + e.getActionCommand());
        }
	 }
	
	protected JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		menuBar = buildFileMenu(menuBar);
		menuBar = buildEditMenu(menuBar);
		return menuBar;
	}
	

	protected JMenuBar buildEditMenu(JMenuBar menuBar){
		JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
       
        return menuBar;
	}
	
	protected JMenuBar buildFileMenu(JMenuBar menuBar){
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
	
	public PropertyLoader getPropertyLoader(){
		return this.load;
	}

	public boolean isGUI() {
		return true;
	}
	
	public void update(Task task) {
		//
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
	
	
	public void sendAlert(String str){
		JOptionPane.showMessageDialog(this, str);
	}
	
	public void alert(String str){
		this.sendAlert(str);
	}
}
