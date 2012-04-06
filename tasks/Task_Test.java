package tasks;


import java.io.File;

import bio.assembly.ACEParser;
import bio.assembly.DepthMap;
import gui.EddieGUI;

public class Task_Test extends Task{

	public Task_Test(){
		complete = -1;
		this.testmode = true;
	}
	
	public void runTest(){
		/*
		 * Testing long print
		 */
		try{
			DepthMap map = new DepthMap();
			ACEParser parser = new ACEParser(map);
			parser.parseAce(new File("/home/dominic/PhD_Data/FERA_DATA/contigs.ace"));
			map.dumpData(new File("/home/dominic/Downloads/temp.txt"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
}

