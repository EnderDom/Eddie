/**
 * 
 */
package enderdom.eddie.main;

import enderdom.eddie.cli.EddieCLI;
import enderdom.eddie.tools.Tools_Fun;
import enderdom.eddie.ui.EddiePropertyLoader;
import enderdom.eddie.gui.EddieGUI;

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
			int retvalue = -1;
			EddiePropertyLoader loader = new EddiePropertyLoader(args);
			retvalue = loader.mode;
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
			else if(retvalue == 6){
				Tools_Fun.printAbout();
			}
			else{
				loader.printHelp();
			}

	}
	
	

}
