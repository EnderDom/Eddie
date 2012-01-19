package ui;
import gui.EddieGUI;
import gui.utilities.SpringUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

import cli.EddieCLI;


import tools.uiTools;
import tools.arrayTools;

import modules.Module;
import modules.moduleTools;

public class ModuleLoader implements Module{

    Properties props;
    Module modules[];
    String modulesnames[];
    public static String modulefilename = new String("modules.properties");
    private String modulename = "MOD_ui.ModuleLoader";
    public String modulesfolder;
    public String[] actions;
    JInternalFrame propsframe;
   	JRadioButton[] buttons;
   	boolean[] selected;
    
    public ModuleLoader(String modulesfolder){
    	this.modulesfolder = modulesfolder;
    	props = new Properties();
        File file = new File(modulesfolder);
        if(!file.isDirectory()){
            file.mkdir();
            PropertyLoader.save(modulesfolder+modulefilename, getDefaultProperties());
        }
        else{
        	File modfile = new File(modulesfolder+modulefilename);
        	if(!modfile.isFile()){
        		PropertyLoader.save(modulesfolder+modulefilename, getDefaultProperties());
        	}
        }
        try
        {
            props.load(new FileInputStream(new File(modulesfolder+modulefilename)));
        } 
        catch(IOException e) {
        	Logger.getRootLogger().error("File "+modulesfolder+modulefilename+" could not be loaded");
        }
    }

    public Module[] addModules(Module[] oldmods){
    	LinkedList<Module> mods = new LinkedList<Module>();
    	if(this.modules == null){
    		for(Object ob : props.keySet()){
    			if(ob.toString().startsWith("MOD_") && props.get(ob).toString().contentEquals("yes")){
    				Module module = null;
					try {
						module = (Module)Class.forName(ob.toString().replace("MOD_", "")).getConstructor().newInstance();
						mods.add(module);
					} 
					catch (Exception e) {
						for(StackTraceElement trace : e.getStackTrace())Logger.getRootLogger().trace(trace.toString());
					} 
					if(module == null){
						Logger.getRootLogger().error("Failed To Load Module: " + ob.toString());
					}
					else{
						Logger.getRootLogger().debug("Loaded Module: " + ob.toString());
					}
    			}
    		}
    	}
    	this.modules = mods.toArray(new Module[0]);
    	Module[] newMods = new Module[oldmods.length+mods.size()];
        for(int i =0; i < oldmods.length; i++)newMods[i]=oldmods[i];
        for(int i =0; i < mods.size(); i++)newMods[i+oldmods.length]=this.modules[i];
        return newMods;
    }

    public Properties getDefaultProperties(){
        Properties defaults = new Properties();
        defaults.setProperty("MOD_modules.lnf.DefaultLNF", "yes");
        defaults.setProperty("NAME_modules.lnf.DefaultLNF", "Look&Feel Changer");
        defaults.setProperty("MOD_modules.lnf.JTatoo", "no");
        defaults.setProperty("NAME_modules.lnf.JTatoo", "JTattoo Look&Feel Extension");
        defaults.setProperty("MOD_modules.bio.fastaTools", "yes");
        defaults.setProperty("NAME_modules.bio.fastaTools", "Add Fasta Tools to Functionality");
        
        return defaults;
    }
    
    public String[][] getModuleNames(){    	
    	LinkedList<String> mods = new LinkedList<String>();
    	for(Object ob : this.props.keySet()){
    		if(ob.toString().startsWith("NAME_")){
    			mods.add(ob.toString().replace("NAME_", ""));
    		}
    	}
    	String[][] ret = new String[3][mods.size()];
    	for(int i = 0 ; i < mods.size(); i++){
    		ret[0][i] = props.getProperty("NAME_"+mods.get(i));
    		ret[1][i] = props.getProperty("MOD_"+mods.get(i));
    		ret[2][i] = mods.get(i);
    	}
    	return ret;
    }

	public boolean ownsThisAction(String s) {
		return moduleTools.ownsThisAction(actions, s);
	}

	public void actOnAction(String s, EddieGUI gui) {
		Logger.getRootLogger().debug("ModuleLoader acting upon command "+s);
		if(s.contentEquals(this.modulename)){
			Logger.getRootLogger().debug("Building General Properties Frame");
			propsframe = uiTools.getGenericPropertiesMenu();
			propsframe.setTitle("Modules");
			int  num = 0;
			JPanel p = new JPanel(new SpringLayout());
			String[][] labels = getModuleNames();
			buttons = new JRadioButton[labels[0].length];
			selected = new boolean[labels[0].length];
			for(int i= 0 ; i< labels[0].length; i++){
				JLabel l = new JLabel(labels[0][i], JLabel.TRAILING);
	            p.add(l);
	            JRadioButton button1 = new JRadioButton();
	            l.setLabelFor(button1);
	            if(labels[1][i].contentEquals("yes")){
	            	button1.setSelected(true);
	            	selected[i] = true;
	            }
	            p.add(button1);
	            buttons[i] = button1;
	            Logger.getRootLogger().trace("Added Properties " + labels[0][i] + " to Panel as No. " + num);
	            num++;
			}
			JButton button1 = new JButton("Save");
			JButton button2 = new JButton("Cancel");
			button1.setActionCommand(modulename+"_PROPS_SAVE");
			button2.setActionCommand(modulename+"_PROPS_CLOSE");
			actions = arrayTools.mergeStrings(actions, new String[]{modulename+"_PROPS_SAVE",modulename+"_PROPS_CLOSE" });
			button1.addActionListener(gui);
			button2.addActionListener(gui);
			p.add(button1);
			p.add(button2);
			num++;
			//Lay out the panel.
			
			SpringUtilities.makeCompactGrid(p,
					num, 2, //rows, cols
					6, 6,        //initX, initY
		            6, 6);       //xPad, yPad
			p.setOpaque(true);
			propsframe.setContentPane(p);
			propsframe.pack();
			propsframe.setVisible(true);
			gui.add2Desktop(propsframe);
		}
		else if(s.contentEquals(modulename+"_PROPS_CLOSE")){
			Logger.getRootLogger().debug("Closing Modules Without Saving");
			this.propsframe.dispose();
			buttons = null;
			actions = new String[]{actions[0]};
		}
		else if(s.contentEquals(modulename+"_PROPS_SAVE")){
			Logger.getRootLogger().debug("Closing Modules and Saving");
			String[][] labels = getModuleNames();
			for(int i =0 ; i < selected.length; i++){
				//TODO check to see if module can be altered
				if(selected[i] && !buttons[i].isSelected()){
					props.setProperty("MOD_" + labels[2][i], "no");
					//Uzninstall
				}
				else if(!selected[i] && buttons[i].isSelected()){
					props.setProperty("MOD_" + labels[2][i], "yes");
					//TODO Install
				}
			}
			save();
			JOptionPane.showMessageDialog(gui, "Module Changes will not take affect until program restarts");
			this.propsframe.dispose();
			buttons= null;
			actions = new String[]{actions[0]};
		}	
	}

	public void addToGui(EddieGUI biodesktopgui) {
		actions = new String[1]; 
		JMenuItem menuItem = new JMenuItem("Modules");
		menuItem.setMnemonic(KeyEvent.VK_M);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
	    menuItem.setActionCommand(this.modulename);
	    actions[0] = this.modulename;
	    menuItem.addActionListener(biodesktopgui);
	    moduleTools.add2JMenuBar(biodesktopgui.getMenu(), menuItem, "Properties");
	}

	public boolean uninstallWithoutRestart() {
		return false;
	}

	public boolean uninstall(EddieGUI gui) {
		return false;
	}
    
    public void save(){
    	PropertyLoader.save(modulesfolder+modulefilename, this.props);
    }

	public String getModuleName() {
		return this.modulename;
	}

	public boolean ownsThisTask(String s) {
		// TODO Auto-generated method stub
		return false;
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
    

}
