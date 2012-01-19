package modules;

import gui.EddieGUI;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
	public static String menustring = "Windows"+moduleTools.menudivider+"Tools";
	public String menuItemName = "Basic";
	public String[] actions;
	public String[] tasks;
	public String[] taskinfo;
	public String taskname;
	
	public boolean ownsThisAction(String s) {
		return moduleTools.ownsThisAction(actions, s);
	}

	public void actOnAction(String s, EddieGUI biodesktopgui) {
		//Do Something Here?
	}

	
	public void addToGui(EddieGUI biodesktopgui) {
		JMenuBar menu = biodesktopgui.getJMenuBar();
		build(menu, biodesktopgui);
	}
	
	public void build(JMenuBar menubar, EddieGUI gui){
		int i =0;
		actions = new String[1];
		JMenuItem menuItem = new JMenuItem(menuItemName);
	    menuItem.setActionCommand(getModuleName()+i);
	    actions[i] = getModuleName()+i;
	    menuItem.addActionListener(gui);
	    moduleTools.add2JMenuBar(menubar, menuItem, new String(menustring));
	}

	public boolean ownsThisTask(String s) {
		return moduleTools.ownsThisAction(tasks, s);
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

	public void printTasks() {
		if(tasks != null && taskinfo != null){
			for(int i =0; i < tasks.length; i++){
				System.out.println(tasks[i] + "		"+ taskinfo[i]);
			}
		}
	}

	public void actOnTask(String s, UI ui) {
		// TODO Auto-generated method stub
		
	}

	public void addToCli(EddieCLI cli) {
		// TODO Auto-generated method stub
		
	}
}
