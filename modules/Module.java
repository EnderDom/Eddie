package modules;

import cli.EddieCLI;
import gui.EddieGUI;

public interface Module
{

    public abstract boolean ownsThisAction(String s);

    public abstract void actOnAction(String s, EddieGUI biodesktopgui);
    
    public abstract boolean ownsThisTask(String s);
    
    public abstract void actOnTask(String s);
    
    public abstract void addToGui(EddieGUI biodesktopgui);
    
    public abstract void addToCli(EddieCLI cli);
    
    public abstract boolean uninstall(EddieGUI gui);
    
    public abstract String getModuleName();
    
    
    
    //TODO 
    /*
     * CLI intergration
     */
    
    
}