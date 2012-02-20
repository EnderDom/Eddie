package tasks;

import bio.sequence.FourBitSequence;
import gui.EddieGUI;

public class Task_Test extends Task{

	
	
	public Task_Test(){
		complete = -1;
		this.testmode = true;
	}
	
	public void runTest(){
		String dna = "ATAGATGATGATGATGATGATGACCCCYYWWVVBBNN";
		System.out.println("BEFORE:" + dna);
		FourBitSequence sequence = new FourBitSequence(dna);
		System.out.println("DNA PARSED");
		System.out.println("AFTER:" + sequence.getAsString());
		System.out.println("Test has completed");
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	
	
}
