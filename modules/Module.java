package modules;

import gui.EddieGUI;

public interface Module
{

    public abstract boolean ownsThisAction(String s);

    public abstract void actOnAction(String s, EddieGUI biodesktopgui);
    
    public abstract void addToGui(EddieGUI biodesktopgui);
    
    public abstract boolean uninstall(EddieGUI gui);
    
    public abstract String getModuleName();
    
}