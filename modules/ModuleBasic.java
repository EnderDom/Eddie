package modules;

import gui.EddieGUI;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import tasks.Task;
import ui.UI;

import cli.EddieCLI;

/*
 * Basic Module Extension to use as a base for modules
 * 
 * It is paramount that modulename be equal to
 * a modulename in ModuleLoader elsewise it won't be loaded at all.
 */

public class ModuleBasic implements Module{
	public String modulename = "MOD_modules.default";
	public String menustring = "Window"+moduleTools.menudivider+"Tools";
	public String menuItemName = "Basic";
	protected String[] actions;
	protected String[] tasks;
	protected String[] taskinfo;
	protected String[] classes;
	public String taskname;
	
	public boolean ownsThisAction(String s) {
		return moduleTools.ownsThisAction(getActions(), s);
	}

	public void actOnTask(String s, UI cli) {
		Logger.getRootLogger().debug("Action "+ s + " sent");
		for(int i =0; i < getTasks().length; i++){
			if(s.contentEquals(getTasks()[i])){
				try {
					Task t = (Task) Class.forName(getClasses()[i]).getConstructor().newInstance();
					if(t != null){
						cli.addTask(t);
					}
				} 					
				catch (Exception e) {
					Logger.getRootLogger().fatal("Task class does not exist, please report this bug:", e);
				}
			}
		}
	}

	
	public void addToGui(EddieGUI biodesktopgui) {
		JMenuBar menu = biodesktopgui.getJMenuBar();
		build(menu, biodesktopgui);
	}
	
	public void build(JMenuBar menubar, EddieGUI gui){
		int i =0;
		String[] actions1 = new String[1];
		JMenuItem menuItem = new JMenuItem(getMenuItemName());
	    menuItem.setActionCommand(getModuleName()+i);
	    actions1[i] = getModuleName()+i;
	    menuItem.addActionListener(gui);
	    moduleTools.add2JMenuBar(menubar, menuItem, new String(getMenuString()));
	    setActions(actions);
	}

	public boolean ownsThisTask(String s) {
		return moduleTools.ownsThisAction(getTasks(), s);
	}

	public void actOnTask(String s) {
		//--> Act here
	}

	public boolean uninstall(EddieGUI gui) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getModuleName() {
		return this.modulename;
	}
	
	public String getMenuItemName(){
		return this.menuItemName;
	}

	public void printTasks() {
		if(getTasks() != null && getTaskinfo() != null){
			for(int i =0; i < getTasks().length; i++){
				System.out.println(getTasks()[i] + "		"+ getTaskinfo()[i]);
			}
		}
	}
	public void addToCli(EddieCLI cli) {
		// TODO Auto-generated method stub
		
	}
	
	public String getMenuString(){
		return this.menustring;
	}

	public String[] getTasks() {
		return tasks;
	}

	public void setTasks(String[] tasks) {
		this.tasks = tasks;
	}

	public String[] getTaskinfo() {
		return taskinfo;
	}

	public void setTaskinfo(String[] taskinfo) {
		this.taskinfo = taskinfo;
	}

	public String[] getActions() {
		return actions;
	}

	public void setActions(String[] actions) {
		this.actions = actions;
	}

	public String[] getClasses() {
		return classes;
	}

	public void setClasses(String[] classes) {
		this.classes = classes;
	}

	public void actOnAction(String s, EddieGUI biodesktopgui) {
		// TODO Auto-generated method stub
		
	}
	
	
}
