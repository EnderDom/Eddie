package modules;

import ui.UI;
import cli.EddieCLI;
import gui.EddieGUI;

public interface Module
{

    public abstract void actOnAction(String s, EddieGUI biodesktopgui);
      
    public abstract void actOnTask(String s, UI ui);
    
    public abstract void printTasks();
    
    public abstract void addToGui(EddieGUI gui);
    
    public abstract void addToCli(EddieCLI cli);    
    
        
	public abstract String[] getActions();

	public abstract String[] getTasks();
    
	
	
	 /*
     * Note on isPersistant():
     * if set to true, when the module is built it is
     * kept instantiated and each time an action or task 
     * is called it is sent to that same object.
     * 
     * If it is not persistant, then each time a action or task
     * is called a new object is created to deal with it
     * 
     * adding to the GUI may require persistant modules, if 
     * data about the guis state is needed to be kept.
     * 
     * However if it is simply adding a menu to start a task/
     * or other method and the module is just a kind of proxy
     * then it need not be persistant
     * 
     * Note: I don't know if this is a good or terrible idea,
     * but i like the idea, of just, plugging in a module, and keeping
     * it somewhat separate from the primary classes.
     * 
     */
    
    public abstract boolean isPersistant();
    
    /*
     * Needed if the module becomes persistant, module name changes
     * from the default a classpath, to a name 
     */

    public abstract void resetModuleName(String name);

    public abstract boolean isTest();
    
}