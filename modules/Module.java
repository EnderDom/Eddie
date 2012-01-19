package modules;

import gui.BioDesktopGUI;

public interface Module
{

    public abstract boolean ownsThisAction(String s);

    public abstract void actOnAction(String s, BioDesktopGUI biodesktopgui);
    
    public abstract void addToGui(BioDesktopGUI biodesktopgui);
    
    public abstract boolean uninstall(BioDesktopGUI gui);
    
    public abstract String getModuleName();
    
}