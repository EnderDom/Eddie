package modules.lnf;

import org.apache.log4j.Logger;

import tools.stringTools;
import ui.PropertyLoader;
import gui.BioDesktopGUI;
import modules.moduleTools;

public class JTatoo extends DefaultLNF{

	private String modulename = "MOD_modules.lnf.JTatoo";
	public static String[] looknfeels = new String[]{"com.jtattoo.plaf.hifi.HiFiLookAndFeel", "com.jtattoo.plaf.luna.LunaLookAndFeel"};
	
	public boolean ownsThisAction(String s) {
		return moduleTools.ownsThisAction(actions, s);
	}
	
	public void actOnAction(String s, BioDesktopGUI biodesktopgui) {
		changeAppear(stringTools.parseString2Int(s.substring(s.indexOf(modulename) + new String(modulename).length(), s.length())), biodesktopgui);
	}
	
	public String[] getLooknFeels(){
		return looknfeels;
	}
	
	public String getModuleName(){
		return this.modulename;
	}

	public boolean uninstallWithoutRestart() {
		return false;
	}

	public boolean uninstall(BioDesktopGUI gui) {
		Logger.getRootLogger().info("Uninstalling Module " + modulename);
		PropertyLoader loader = gui.getPropertyLoader();
		String lnf = loader.getLastLNF();
		for(int i =0; i < looknfeels.length; i++){
			if(lnf.contentEquals(looknfeels[i])){
				loader.changeDefaultLNF(PropertyLoader.defaultlnf);
				Logger.getRootLogger().debug("Changed L&F back to default as part of uninstall");
			}
		}
		return true;
	}
	
}
