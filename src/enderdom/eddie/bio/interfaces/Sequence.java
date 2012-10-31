package enderdom.eddie.bio.interfaces;

public interface Sequence {

	public static int DNA = 0;
	public static int RNA = 1;
	public static int PROTEIN = 2;
	public static int OTHER = 3;
	
	/**
	 * 
	 * @return name of the sequence as String
	 */
	public String getName();
	
	/**
	 * 
	 * @return Sequence as a String
	 */
	public String getSequence();
	
	
	/**
	 * 
	 * @return an integer representing type of sequence
	 */
	public int getSequenceType();
	
	/**
	 * 
	 * @return this is the actual number of molecules, not
	 * the String.length(). For example ATAT-T would be len=5
	 * Take care with amino acids as sometimes stop codons can
	 * be represented with '-' :(
	 */
	public int getActualLength();
	
	/**
	 * Trims a section from the left
	 * side of the sequence removing the number
	 * of characters remaining
	 * ie leftTrim(4)
	 * 012345678
	 * ATGATGATG
	 * ----^^^^^
	 * ->
	 *     TGATG
	 * @param i index at which to start sequence
	 * @return remaining size of sequence string size, not actual size
	 */
	public int leftTrim(int i);
	
	/**
	 * Trims a section from the left
	 * side of the sequence removing the number
	 * of characters remaining
	 * ie leftTrim(4)
	 * 012345678
	 * ATGATGATG
	 * ^^^^^----
	 * ->
	 * ATGAT
	 * @param i index at which to stop sequence
	 * @return remaining size of sequence string size, not actual size
	 */
	public int rightTrim(int i);
	
	/**
	 * removeSection(4,7);
	 * 
	 * 012345678
	 * ATGATGATG
	 * ----^^^--
	 * ->
	 * ATGA and TG are returned
	 * @param start start to section
	 * @param end of section
	 * @return the resulting two sequences produced by removing the region
	 * there names will be the original sequence name plus an additional 
	 * identifier ie Seq1 -> Seq1_A, Seq1_B 
	 */
	public Sequence[] removeSection(int start, int end);
	
	
	
}
