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
	
	public String getName();
	
	public SequenceObject getConsensus(); 
	
	public int trimLeftAllContig();
	
	public int trimRightAllContig();
	
	public Contig[] removeSectionAllContig(int opts);
	
}
