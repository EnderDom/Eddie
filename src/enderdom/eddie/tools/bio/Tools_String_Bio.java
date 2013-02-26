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
	
	public static String translate(String stri, int frame, boolean spaced){
		StringBuffer amino = new StringBuffer();
		for(int i = frame; i < stri.length()-2;i = i+3){
			String sub1 = stri.substring(i,i+3);
			String sub2 = stri.substring(i,i+2);
			if(sub2.equalsIgnoreCase("GC")){
				amino = amino.append("A");
			}
			else if(sub2.equalsIgnoreCase("CG") || sub1.equalsIgnoreCase("AGA") ||sub1.equalsIgnoreCase("AGG") || sub1.equalsIgnoreCase("AGR")){
				amino = amino.append("R");
			}
			else if(sub1.equalsIgnoreCase("AAT")|| sub1.equalsIgnoreCase("AAC") || sub1.equalsIgnoreCase("AAY")){
				amino = amino.append("N");
			}
			else if(sub1.equalsIgnoreCase("GAT") || sub1.equalsIgnoreCase("GAC") || sub1.equalsIgnoreCase("GAY") ){
				amino = amino.append("D");
			}
			else if(sub1.equalsIgnoreCase("TGT") || sub1.equalsIgnoreCase("TGC")  || sub1.equalsIgnoreCase("TGY")){
				amino = amino.append("C");
			}	
			else if(sub1.equalsIgnoreCase("CAA") || sub1.equalsIgnoreCase("CAG") || sub1.equalsIgnoreCase("CAR") ){
				amino = amino.append("Q");
			}
			else if(sub1.equalsIgnoreCase("TTA") || sub1.equalsIgnoreCase("TTG") || sub1.equalsIgnoreCase("TTR") || sub2.equalsIgnoreCase("CT")){
				amino = amino.append("L");
			}
			else if(sub1.equalsIgnoreCase("AAA") || sub1.equalsIgnoreCase("AAG") || sub1.equalsIgnoreCase("AAR")){
				amino = amino.append("K");
			}
			else if(sub1.equalsIgnoreCase("ATG") ){
				amino = amino.append("M");
			}
			else if(sub1.equalsIgnoreCase("TTT") || sub1.equalsIgnoreCase("TTC") || sub1.equalsIgnoreCase("TTY")  ){
				amino = amino.append("F");
			}
			else if(sub2.equalsIgnoreCase("CC")){
				amino = amino.append("P");
			}	
			else if(sub2.equalsIgnoreCase("TC") || sub1.equalsIgnoreCase("AGT")|| sub1.equalsIgnoreCase("AGC") || sub1.equalsIgnoreCase("AGY") ){
				amino = amino.append("S");
			}
	 		else if(sub1.equalsIgnoreCase("GAA") || sub1.equalsIgnoreCase("GAG") ||  sub1.equalsIgnoreCase("GAR") ){
				amino = amino.append("E");
			}
			else if(sub2.equalsIgnoreCase("AC")){
				amino = amino.append("T");
			}
			else if(sub2.equalsIgnoreCase("GG")){
				amino = amino.append("G");
			}
			else if(sub1.equalsIgnoreCase("TGG")){
				amino = amino.append("W");
			}
			else if(sub1.equalsIgnoreCase("CAT") || sub1.equalsIgnoreCase("CAC") || sub1.equalsIgnoreCase("CAY") ){
				amino = amino.append("H");
			}
			else if(sub1.equalsIgnoreCase("TAT") || sub1.equalsIgnoreCase("TAC") || sub1.equalsIgnoreCase("TAY") ){
				amino = amino.append("Y");
			}
			else if(sub2.equalsIgnoreCase("AT") && !sub1.equalsIgnoreCase("ATG")){
				amino = amino.append("I");
			}
			else if(sub2.equalsIgnoreCase("GT")){
				amino = amino.append("V");
			}
			else if(sub1.equalsIgnoreCase("TAA") || sub1.equalsIgnoreCase("TGA")|| sub1.equalsIgnoreCase("TAG") || sub1.equalsIgnoreCase("TAR")){
				amino = amino.append(".");
			}
			else{
				amino = amino.append("X");
			}
		}
		if(spaced){
			amino = spaceProt(amino, frame);
		}
		return amino.toString();
	}
	
	public static StringBuffer spaceProt(StringBuffer stri, int frame){
		StringBuffer spaced = new StringBuffer();
		for(int i = 0; i < frame; i++){
			spaced = spaced.append(" ");
		}	
		for(int i = 0; i < stri.length(); i++){
			spaced = spaced.append(stri.substring(i,i+1) + " ");
		}
		return spaced;
	}
}
