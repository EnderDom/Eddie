/**
 * 
 */
package main;

import cli.EddieCLI;
import cli.EddieLite;
import tools.Tools_Fun;
import ui.PropertyLoader;
import gui.EddieGUI;

/**
 * @author Dominic Matthew Wood
 *
 */
public class Eddie {

	/**
	 * 
	 * @param args input arguments
	 */
	public static void main(String[] args) {
			int retvalue = 4;
			PropertyLoader loader = new PropertyLoader();
			retvalue = loader.loadBasicArguments(args);
			if(retvalue == 2){
				@SuppressWarnings("unused")
				EddieCLI cli = new EddieCLI(loader, false);
			}
			else if(retvalue == 3){//Persisting CLI mode <-- currently not supported
				@SuppressWarnings("unused")
				EddieCLI cli = new EddieCLI(loader, true);
			}	
			else if(retvalue == 4){
				 @SuppressWarnings("unused")
				EddieGUI desk = new EddieGUI(loader);
			}
			else if(retvalue == 5){
				@SuppressWarnings("unused")
				EddieLite lite = new EddieLite(loader);
			}
			else if(retvalue == 6){
				Tools_Fun.printAbout();
			}
			else{
				loader.printHelp();
			}
	}
	
	

}
