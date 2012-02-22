package tasks;


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
		String dna = "ATAGATGATGATGATGATGATGACCCCYYWWVVBBNN";
		FourBitSequence sequence = new FourBitSequence(dna);
		System.out.println("DNA PARSED");
		System.out.println("AFTER:" + sequence.getAsString());
		System.out.println("BEFOR:" + dna);
		System.out.println("REVCP:" + sequence.getAsStringRevComp());
		System.out.println("0 : "+sequence.get(0));
		System.out.println("0 : "+sequence.get(2));
		System.out.println("0 : "+sequence.get(36));
		System.out.println("Test has completed");
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	
	
}
