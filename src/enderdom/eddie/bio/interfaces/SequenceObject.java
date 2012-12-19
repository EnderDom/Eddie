package enderdom.eddie.bio.interfaces;

public interface SequenceObject {

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
	 * @return string containing quality if available
	 */
	public String getQuality();
	
	public void setSequence(String sequence);
	
	public void setQuality(String quality);
	
	public void setName(String title);
	
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
	 * 
	 * @return the string length of the sequence including gaps 
	 * that were introduced
	 */
	public int getLength();
	
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
	 * @param i distance from right side
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
	 * end-start should equal number of characters to remove
	 * 
	 * @param start start to section
	 * @param end of section
	 * @return the resulting two sequences produced by removing the region
	 * there names will be the original sequence name plus an additional 
	 * identifier ie Seq1 -> Seq1_A, Seq1_B 
	 */
	public SequenceObject[] removeSection(int start, int end);
	
	/**
	 * Insert a sequence within the sequence
	 * 
	 * If sequenceobject has quality, that is also inserted
	 * 
	 * @param pos postion to insert from, so pos =0,
	 *  would insert before any amino acids
	 * @param s the sequence to insert, this obviously will need to
	 * be checked by the subclass for sequence compatability using 
	 * @see getSequenceType()
	 * @return 
	 */
	public void insert(int pos, SequenceObject s);
	
	/**
	 * 
	 * Add sequence to end
	 * 
	 * If sequenceobject has quality, that is also appended
	 * 
	 * @param s
	 */
	public void append(SequenceObject s);
	
	/**
	 * If sequenceobject has quality, 
	 * that is also extend with values of 0
	 * 
	 * Extend sequence left with filler symbols,
	 * ie ---- 
	 * @param i 
	 */
	public void extendLeft(int i);
	
	
	/**
	 * If sequenceobject has quality, 
	 * that is also extend with values of 0
	 * 
	 * Extend sequence right with filler symbols,
	 * ie ---- 
	 * @param i
	 */
	public void extendRight(int i);
	
	
	public boolean hasQuality();
	
	
	
	
}
