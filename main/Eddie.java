/**
 * 
 */
package main;

import ui.PropertyLoader;
import gui.EddieGUI;

/**
 * @author Dominic Wood
 *
 */
public class Eddie {
	
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
				EddieGUI desk = new EddieGUI(loader);
			}
			else{
				loader.printHelp();
			}
	}

}
