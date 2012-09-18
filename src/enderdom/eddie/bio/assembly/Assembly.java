package enderdom.eddie.bio.assembly;

/**
 * @author Dominic Matthew Wood
 * 
 * Currently everything that implements this is deprecated
 * but I quite like the idea of having an interface for assemblies 
 * for things like coverage and retrieved contig number etc.
 */

public interface Assembly {


	public int getContigsSize();

	/*
	 * Get the index of the contig
	 * with contigname parameter
	 */

	public int getContigIndex(String contigname);
	
	/*
	 * Returns the depth of coverage at the bp position
	 */
	public int getDepthofContigAtPos(String contig, int position);
	
	/*
	 * Gets the average coverage for this contig at this position
	 */
	public double getAverageCoverageDepth(String contig);
	
	/*
	 * Get global Average Coverage 
	 */
	public double getGlobalAverageCoverageDepth();
	
	
	public int getMedianCoverageDepth();
	
	
	public int getGlobalMedianCoverageDepth();
	
	/*
	 * Get Number of reads that makes up this contig
	 */
	public int getReadsSize(String contigindex);
	
	/*
	 * Get Average Read Length
	 */
	public double getAvgReadLength(String contigindex);
	
	
	public int getTotalNoOfbpContig(String contigindex);
	
}
