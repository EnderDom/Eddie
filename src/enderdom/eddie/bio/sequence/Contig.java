package enderdom.eddie.bio.sequence;


/**
 * 
 * @author dominic
 * 
 * Contig extends SequenceList and its
 * the getSequence() style methods refer
 * to the reads
 *
 */

public interface Contig extends SequenceList {
	
	public String getContigName();
	
	public void setContigName(String s);
	
	public SequenceObject getConsensus(); 
	
	public void setConsensus(SequenceObject s);
	
	/**
	 * Set the offset, how much to add to the leftmost base in order to align
	 * @param Name of read
	 * @param offset number to offset by (0-based of course). ie a value of 1
	 * would be equivalent to the following alignment:
	 * Raw:
	 * CONS: ATGTGTG
	 * READ: TGTGTG
	 * --->
	 * With Offset:
	 * CONS: ATGTGTG
	 * READ: -TGTGTG
	 * 
	 */
	public void setOffset(String s, int offset);

	
	/**
	 * I'm not really all the clear what range and padded range refer
	 * to. I think range is range without additions of '-' to 
	 * align the read and contig properly but idoknow :S 
	 * Seemingly assemblers don't seem to output these values half the 
	 * time so for everything I've kinda ignored them ...
	 * 
	 * @param s
	 * @param start
	 * @param end
	 */
	public void setRange(String s, int start, int end);
	
	public int trimLeftAllContig();
	
	public int trimRightAllContig();
	
	public Contig[] removeSectionAllContig(int opts);
	
	public int getCoverageAtBp(int i, int base);
	
	//Gets char based on underlying file, ie includes '*' in consensuse
	public char getCharAt(int sequencenumber, int position, int base);

	public int createPosition();
	
}
