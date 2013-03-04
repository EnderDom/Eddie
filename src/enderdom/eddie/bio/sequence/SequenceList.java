package enderdom.eddie.bio.sequence;

import java.io.File;
import java.util.Iterator;
import java.util.Set;


public interface SequenceList extends Iterator<SequenceObject>{
	
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
	public int getNoOfMonomers();
	
	/**
	 * 
	 * @return Total number of base pairs or amino acids 
	 * (Sum of all sequences lengths (not actual) includes gaps '-')
	 */
	public int getQuickMonomers();
	
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
	
	public boolean canAddSequenceObjects();
	
	/**
	 * Standard is to overwrite sequence object if already present
	 * @param object
	 */
	public void addSequenceObject(SequenceObject object);

	public boolean canRemoveSequenceObjects();
	
	/**
	 * @param name string name of the sequenceObject 
	 */
	public void removeSequenceObject(String name);
	
	/**
	 * Saves file as stated filetype, if filetype == -1
	 * will save as filetype which SequenceList was loaded
	 * from if loaded from file, else will throw exception
	 * 
	 * @param file file to save to
	 * @param filetype type of file as listed above
	 * @throws Exception
	 * 
	 * @return returns the paths of all files saved
	 */
	public String[] saveFile(File file, BioFileType filetype) throws Exception, UnsupportedTypeException;
	
	/**
	 * 
	 * @param file File which contains SequenceList object 
	 * @param filetype type of file, -1 here means the method
	 * will try and work it out
	 * @throws Exception 
	 * @throws UnsupportedTypeException
	 * 
	 * @return number of sequences loaded
	 */
	public int loadFile(File file, BioFileType filetype) throws Exception, UnsupportedTypeException;
	
	public BioFileType getFileType();

	/**
	 * 
	 * @return file name if available, else
	 * returns null
	 */
	public String getFileName();
	
	/**
	 * 
	 * @return file path if available, else
	 * returns null
	 */
	public String getFilePath();
	
	
	public Set<String> keySet();
	
}
