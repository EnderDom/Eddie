/**
 * 
 */
package enderdom.eddie.main;

import enderdom.eddie.cli.EddieCLI;
import enderdom.eddie.tools.Tools_Fun;
import enderdom.eddie.ui.EddiePropertyLoader;

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
				EddieCLI cli = new EddieCLI(loader);
				cli.exit();
			}
			else if(retvalue == 3){//Persisting CLI mode <-- currently not supported
				
			}	
			else if(retvalue == 4){
				System.out.println(EddiePropertyLoader.edition+ " Edition does not include GUI modules ");
			}
			else if(retvalue == 6){
				Tools_Fun.printAbout();
			}
			else{
				loader.printHelp();
			}

	}
	
	

}
