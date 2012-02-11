package modules;

import gui.EddieGUI;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import tasks.Task;
import tools.Tools_Modules;
import ui.UI;

import cli.EddieCLI;

/*
 * Basic Module Extension to use as a base for modules
 * 
 * It is paramount that modulename be equal to
 * a modulename in ModuleLoader elsewise it won't be loaded at all.
 */

public abstract class Module_Basic implements Module{
	public String menustring = "Window"+Tools_Modules.menudivider+"Tools";
	public String menuItemName = "Basic";
	protected String[] actions;
	protected String[] tasks;
	protected String[] taskinfo;
	protected String[] classes;
	public String taskname;
	protected boolean persistance;
	
	public void actOnTask(String s, UI cli) {
		Logger.getRootLogger().debug("Task "+ s + " sent");
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
	    setActions(actions1);
	    menuItem.addActionListener(gui);
	    Tools_Modules.add2JMenuBar(menubar, menuItem, new String(getMenuString()));
	}

	public void actOnTask(String s) {
		//--> Act here
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
	
	public String getModuleName(){
		return this.getClass().getName();
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
	
	public String[] getActions(){
		return this.actions;
	}
	
	public boolean isPersistant(){
		return persistance;
	}
}
