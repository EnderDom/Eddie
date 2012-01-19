/**
 * 
 */
package main;

import ui.PropertyLoader;
import gui.BioDesktopGUI;

/**
 * @author dominic
 *
 */
public class BioDesktop {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			int retvalue = 3;
			
			PropertyLoader loader = new PropertyLoader();
			retvalue = loader.loadArguments(args);
		
			if(retvalue == 2){
				//TODO CLI
			}		
			else if(retvalue == 3){
				 @SuppressWarnings("unused")
				BioDesktopGUI desk = new BioDesktopGUI(loader);
			}
			else{
				loader.printHelp();
			}
	}

}
