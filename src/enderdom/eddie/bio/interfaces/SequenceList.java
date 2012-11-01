package enderdom.eddie.bio.interfaces;

import java.io.File;
import java.util.Iterator;

public interface SequenceList extends Iterator<SequenceObject>{

	public static int FASTA = 0;
	public static int QUAL = 1;
	public static int FAST_QUAL = 2; //Both Fasta and Qual can be loaded into 1 object
	public static int FASTQ = 3;
	public static int CLUSTAL = 4;
	public static int OTHER =5;
	
	/**
	 * 
	 * @return return total n50 for all sequences
	 */
	public int getN50();
	
	/**
	 * 
	 * @return an array of length equal to length of list
	 * containing the lengths of each internal sequence
	 */
	public int[] getListOfLens();
	
	/**
	 * 
	 * @return an array of length equal to length of list
	 * containing the lengths (actual) of each internal sequence
	 */
	public int[] getListOfActualLens();
	
	/**
	 * 
	 * @return Total number of base pairs or amino acids 
	 * (Sum of all sequences lengths (actual))
	 */
	public int getNoOfMolecules();
	
	/**
	 * 
	 * @return number of sequences in list
	 */
	public int getNoOfSequences();
	
	/**
	 * 
	 * @param i index
	 * @return Sequence object at index i
	 */
	public SequenceObject getSequence(int i);
	
	/**
	 * 
	 * @param s sequence name
	 * @return First sequence with name denoted by s
	 */
	public SequenceObject getSequence(String s);
	
	/**
	 * Saves file as stated filetype, if filetype == -1
	 * will save as filetype which SequenceList was loaded
	 * from if loaded from file, else will throw exception
	 * 
	 * @param file file to save to
	 * @param filetype type of file as listed above
	 * @throws Exception
	 * 
	 * @return true if save successful
	 */
	public boolean saveFile(File file, int filetype) throws Exception, UnsupportedTypeException;
	
	/**
	 * 
	 * @param file File which contains SequenceList object 
	 * @param filetype type of file, -1 here means the method
	 * will try and work it out
	 * @throws Exception 
	 * @throws UnsupportedTypeException
	 * 
	 * @return true if load successful
	 */
	public void loadFile(File file, int filetype) throws Exception, UnsupportedTypeException;
	
}