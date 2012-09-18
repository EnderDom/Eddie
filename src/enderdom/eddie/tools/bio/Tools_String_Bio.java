package enderdom.eddie.tools.bio;

public class Tools_String_Bio {

	/**
	 * Class of static methods which 
	 * manipulate biological data stored in Strings
	 */
	
	/**
	 * 
	 * Converts a string of amino acids
	 * into a string of redundant bases.
	 * 
	 * Recognises Selenocysteine & Pyrrolysine (U & O)
	 * Recognises J as Isoleucine or leucine
	 * Recognises Z as Glutamine or Glutamic Acid
	 * Recognises B as Asparagine or Aspartic Acid
	 * 
	 * Stop codon should be '.'
	 * X is converted to NNN
	 * - is converted to an equivalent spacer in nuc, ie '---'
	 * 
	 * @param protein
	 * @return nucleotide
	 */
	public static String protein2dna(String protein){
		StringBuilder nucleotide = new StringBuilder();
		char[] aminos = new char[]{
				'A','R','N','D','C',
				'Q','E','G','H','I',
				'L','K','M','F','P',
				'S','T','W','Y','V',
				'B','Z','J','U','O',
				'.','X','-'
				};
		String[] nucs = new String[]{
				"GCN","MGN","AAY","GAY","TGY",
				"CAR","GAR","GGN","CAY","ATH",
				"YTN","AAR","ATG","TTY","CCN",
				"WSN","ACN","TGG","TAY","GTN",
				"RAY","SAR","HTN","TGA","TAG",
				"TRR","NNN","---"
				};
		
		for(int i =0; i < protein.length(); i++){
			for(int j =0; j < aminos.length; j++){
				if(protein.charAt(i) == aminos[j]){
					nucleotide.append(nucs[j]);
					break;
				}
			}
		}
		return nucleotide.toString();
	}
	
	/**
	 * Reverse compliments DNA String
	 * 
	 * Uracils not accepted as input, must be changed to T
	 *  
	 * @param sequence
	 * @return
	 */
	public static String reverseComp(String sequence){
		sequence = new StringBuffer(sequence).reverse().toString();
		char[] sense = new char[]{'A', 'C', 'Y', 'K' ,'B','D','H','V','M','R', 'G', 'T'}; //N ,I, S and W missed out as they are the same rev'comp'd
		char[] sense2 = new char[]{'a', 'c', 'y', 'k' ,'b','d','h','v','m','r', 'g', 't'}; 
		for(int i =0; i < 12; i++){
			sequence = sequence.replace(sense[i], sense2[11-i]);
		}
		return sequence.toUpperCase();
	}
	
}
