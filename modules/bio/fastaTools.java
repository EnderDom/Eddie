package modules.bio;

import org.apache.log4j.Logger;

import tasks.bio.fasta.fastaConverter;
import ui.UI;
import gui.EddieGUI;
import cli.EddieCLI;

import modules.ModuleBasic;
import modules.moduleTools;

public class fastaTools extends ModuleBasic{
	
	String modulename = "MOD_modules.bio.fastaTools";
	public static String menustring = "Windows"+moduleTools.menudivider+"Tools";
	public String menuItemName = "Fasta Tools";
	
	public fastaTools(){
		
	}
	
	public void actOnAction(String s, EddieGUI biodesktopgui) {
		//TODO implement the GUI side of this
	}
	
	public void addToCli(EddieCLI cli) {
		tasks = new String[]{"convert"};
		taskinfo = new String[]{"converts fasta & qual to fastq"};
	}
	
	public void actOnTask(String s, UI cli) {
		Logger.getRootLogger().debug("Action "+ s + " sent to fastaTools");
		if(s.contentEquals(tasks[0])){
			cli.addTask(new fastaConverter());
		}
		else{
			
		}
	}

}
