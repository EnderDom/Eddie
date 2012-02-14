package bio.assembly;

public interface Assembly {

	
	/*
	 * Get Number of contigs in the assembly
	 */
	public int getContigsSize();

	/*
	 * Get the index of the contig
	 * with contigname parameter
	 */
	public int getContigIndex(String contigname);
	
	/*
	 * Returns the depth of coverage at the bp position
	 */
	public int getDepthofContigAtPos(int contigindex, int position);
	
	/*
	 * Gets the average coverage for this contig at this position
	 */
	public double getAverageCoverageDepth(int contigindex);
	
	/*
	 * Get global Average Coverage 
	 */
	public double getGlobalAverageCoverageDepth();
	
	
	public int getMedianCoverageDepth();
	
	
	public int getGlobalMedianCoverageDepth();
	
	/*
	 * Get Number of reads that makes up this contig
	 */
	public int getReadsSize(int contigindex);
	
	/*
	 * Get Average Read Length
	 */
	public int getAvgReadLength(int contigindex);
	
	
	public int getTotalNoOfbpContig(int contigindex);
	
	public int getGlobalNoOfbp();
}
