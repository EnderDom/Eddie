/**
 * 
 */
package main;

import cli.EddieCLI;
import tools.funTools;
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
			int retvalue = 4;
			PropertyLoader loader = new PropertyLoader();
			retvalue = loader.loadBasicArguments(args);
		
			if(retvalue == 2){
				@SuppressWarnings("unused")
				EddieCLI cli = new EddieCLI(loader, false);
			}
			else if(retvalue == 3){//Persisting CLI mode
				@SuppressWarnings("unused")
				EddieCLI cli = new EddieCLI(loader, true);
			}	
			else if(retvalue == 4){
				 @SuppressWarnings("unused")
				EddieGUI desk = new EddieGUI(loader);
			}
			else if(retvalue == 5){
				funTools.printAbout();
			}
			else{
				loader.printHelp();
			}
	}

}
