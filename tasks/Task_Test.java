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
		dna = sequence.getAsStringRevComp();
		sequence.toReverseComp();
		for(int i =0 ; i < dna.length(); i++){
			char c =sequence.get(i);
			if(c != dna.charAt(i)){
				System.out.println(i +") "+ c+ " || " + dna.charAt(i));
			}
		}	
		System.out.println("Test has completed");
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	
	
}
