package tasks;

import tools.Tools_Bit;
import bio.sequence.FourBitSequence;
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
		long o = 0x000FF000;
		System.out.println(Tools_Bit.LongAsBitString(o));
		o = 1;
		for(int i =0; i < 128; i ++){
			System.out.println(Tools_Bit.LongAsBitString(o)+ "  |"+i );
			o<<=1;
		}
		o=1;
		System.out.println(Tools_Bit.LongAsBitString(o));
		String dna = "ATAGATGATGATGATGATGATGACCCCYYWWVVBBNN";
		//System.out.println("BEFORE:" + dna);
		//FourBitSequence sequence = new FourBitSequence(dna);
		//System.out.println("DNA PARSED");
		//System.out.println("AFTER:" + sequence.getAsString());
		System.out.println("Test has completed");
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	
	
}
