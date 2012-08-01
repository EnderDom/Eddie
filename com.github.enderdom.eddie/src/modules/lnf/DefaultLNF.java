package modules.lnf;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import modules.Module;

import org.apache.log4j.Logger;

import cli.EddieCLI;

import tools.Tools_Modules;
import tools.Tools_UI;
import tools.Tools_String;
import ui.UI;

import gui.EddieGUI;

/**
 * 
 * @author EnderDom
 *
 * I'm not even sure why I spent so much time on this...
 *
 */
public class DefaultLNF implements Module{
	
	public static String menuname = "Window";
	public static String menuname1 = "Look&Feel";
	public String uis[];
	JRadioButtonMenuItem appears[];
	public String[] actions;
	public static String namereplace = "LookAndFeel";
	protected String modulename;
	public int startinglnf; 

	public boolean ownsThisAction(String s) {
		return Tools_Modules.ownsThisAction(actions, s);
	}

	public void actOnAction(String s, EddieGUI biodesktopgui) {
		Integer appear = Tools_String.parseString2Int(s.substring(s.indexOf(getModuleName()) + new String(getModuleName()).length(), s.length()));
		Logger.getRootLogger().trace("Appear value setting to " + appear);
		if(appear != null)changeAppear(appear, biodesktopgui);
		else Logger.getRootLogger().debug("Value returned NaN");
	}

	
	public void addToGui(EddieGUI biodesktopgui) {
		JMenuBar menu = biodesktopgui.getJMenuBar();
		build(menu, biodesktopgui);
	}
	
	public void build(JMenuBar menubar, EddieGUI gui){
		uis = getLooknFeels();
		appears = new JRadioButtonMenuItem[uis.length];
		actions = new String[uis.length];
		for(int i =0; i < uis.length; i++){
			String name = uis[i];
			int numb = uis[i].lastIndexOf(".")+1;
			if(numb > 0){
				name = uis[i].substring(numb, uis[i].length());
				name = name.replaceAll(namereplace, "");
			}
			
			boolean selected = uis[i].equalsIgnoreCase(Tools_UI.getLookAndFeel());
			JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(name,selected);
	        menuItem.setActionCommand(getModuleName()+i);
	        actions[i] = getModuleName()+i;
	        menuItem.addActionListener(gui);
	        appears[i] = menuItem;
	        Tools_Modules.add2JMenuBar(menubar, menuItem, new String(menuname+Tools_Modules.menudivider+menuname1), false);
		}
	}
	
	protected void changeAppear(int numb, EddieGUI gui){
		if(numb > -1 && numb < uis.length){
    		boolean returna = Tools_UI.changeLnF(uis[numb], gui);
    		if(returna){
    			changeAppearanceRadio(gui, numb);
    			Logger.getRootLogger().debug("Changed look and feel to " + uis[numb]);
    		}
    		else{
    			Logger.getRootLogger().debug("Failed to change look and feel to " + uis[numb]);
    		}
    		gui.load.changeDefaultLNF(uis[numb]);
    	}
	}
	
	protected void changeAppearanceRadio(EddieGUI biodesktopgui, int ip){
		JMenuBar menu = biodesktopgui.getJMenuBar();	
		for(int i =0; i < menu.getMenuCount(); i++){
			if(menu.getMenu(i).getText().contentEquals(menuname)){
				JMenu menu1 = menu.getMenu(i);
				for(int j = 0; j < menu1.getItemCount(); j++){
					if(menu1.getItem(j).getText().contentEquals(menuname1)){
						JMenu menu2 = (JMenu)menu1.getItem(j);
						if(menu2.getItemCount() > 0){
							for(int l = 0; l < menu2.getItemCount(); l++){
								JRadioButtonMenuItem jradio = (JRadioButtonMenuItem)menu2.getItem(l);
								jradio.setSelected(false);
							}
						}
						else{
							Logger.getRootLogger().warn("Missing LNF Menu Items");
						}
					}
				}
			}
		}
		for(int i =0; i < appears.length; i++){
			if(i == ip){
				appears[i].setSelected(true);
			}
			else{
				appears[i].setSelected(false);
			}
		}
	}
	
	public String getModuleName(){
		return this.getClass().getName();
	}
	
	public String[] getLooknFeels(){
		return Tools_UI.getInstalledLnFs();
	}

	public void printTasks() {
		// TODO Auto-generated method stub
		
	}

	public void actOnTask(String s, UI ui) {
		// TODO Auto-generated method stub
	}
	
	public void addToCli(EddieCLI cli) {
		// TODO Auto-generated method stub
	}

	public boolean isPersistant() {
		return true;
	}

	public String[] getActions() {
		return actions;
	}

	public String[] getTasks() {
		return null;
	}

	public void resetModuleName(String name) {
		this.modulename = name;
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
