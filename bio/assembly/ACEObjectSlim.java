package bio.assembly;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import tools.Tools_Math;
import tools.bio.Tools_Sequences;
import bio.fasta.Fasta;
import bio.sequence.FourBitSequence;
import bio.sequence.Sequences;

/**
 * A slimmer version of ACEObject,
 * uses FourBitSequence, but as FourBitSequence is a little
 * experimental still, ACEObjectSlim is not the standard to use
 * 
 * @author EnderDom
 *
 */

public class ACEObjectSlim implements Sequences, Assembly, ACEHandler{


	//ALL values are converted to 0 index/base
	/*
	 * Have a problem with multiple reads with same name
	 * this has a workaround, but I need to change the way
	 * the data is stored so reads are linked to contig better
	 * ie, having reads indexed as contig_read-no rather than their
	 * read name, as this can be duplicate in ace files
	 */
	
	HashMap<Integer, String> contignumb;
	HashMap<String, FourBitSequence> contigs;
	HashMap<String, String> qualities;
	
	HashMap<String, FourBitSequence> reads;
	HashMap<String, String> read2contig;
	
	HashMap<String, Short> rangeleft;
	HashMap<String, Short> rangeright;
	HashMap<String, Short> rangeleftpad;
	HashMap<String, Short> rangerightpad;
	HashMap<String, Short> positions;
	
	int currentcontig = -1;
	/*
	 * Sets the methods for calculating average coverage 
	 */
	public int averagecoveragecalc = 1;
	
	private int[] lens;
	
	//This says whether the multiple reads with same name error has been thrown already
	private String readname = "read_";
	
	private boolean multireaderror;
	
	private int readcount;
	
	public ACEObjectSlim(){
		contigs = new LinkedHashMap<String, FourBitSequence>();
		qualities = new LinkedHashMap<String, String>();
		read2contig = new LinkedHashMap<String, String>();
		reads = new LinkedHashMap<String, FourBitSequence>();
		contignumb = new LinkedHashMap<Integer, String>();
		positions = new LinkedHashMap<String, Short>();
		rangeleft = new LinkedHashMap<String, Short>();
		rangeright = new LinkedHashMap<String, Short>();
		rangeleftpad = new LinkedHashMap<String, Short>();
		rangerightpad = new LinkedHashMap<String, Short>();
	}
	
	public void setRefName(String name) {
		currentcontig++;
		contigs.put(name, null);
		contignumb.put(currentcontig, name);
	}

	public String getRefName(int i) {
		return contignumb.get(i);
	}

	public void setRefConsensus(String buffer) {
		contigs.put(contignumb.get(currentcontig), new FourBitSequence(buffer));
	}

	public String getRefConsensus(String refname) {
		return contigs.get(refname).getAsString();
	}

	public void setRefConsensusQuality(String buffer) { 
		qualities.put(contignumb.get(currentcontig), buffer);
	}

	public String getRefConsensusQuality(String refname) {
		return qualities.get(refname);
	}

	public int getRefLength(String refname) {
		return this.contigs.get(refname).length();
	}

	public void setNoOfBases(int i) {

	}

	public void setNoOfReads(int i) {
		// TODO Auto-generated method stub
	}

	public void setBaseSegments(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setOrientation(char orient) {
		
	}

	public String addQNAME(String name) {
		if(multireaderror || reads.containsKey(name)){
			//Implements multiple reads with same name workaround
			if(!multireaderror){
				Logger.getRootLogger().warn("Multiple reads with same name " +name);

				String[] set = reads.keySet().toArray(new String[0]);
				for(int i =0 ; i < set.length; i++){
					name =  readname+i;
					//Add read with new name and remove old names
					resetAll(set[i], name);
				}
				if(readcount != set.length-1){
					Logger.getRootLogger().warn("Read size"+readcount+" does not equal count "+set.length);
					readcount = set.length-1;
				}
				multireaderror = true;
			}
			name = readname+readcount;
		}
		reads.put(name, null);
		read2contig.put(name, this.getRefName(currentcontig));
		readcount++;
		return name;
	}
	
	public void resetAll(String old, String news){
		this.reads.put(news, reads.get(old));
		this.reads.remove(old);
		this.read2contig.put(news, read2contig.get(old));
		this.read2contig.remove(old);
		this.positions.put(news, positions.get(old));
		this.positions.remove(old);
		this.rangeleft.put(news, positions.get(old));
		this.rangeleft.remove(old);
		this.rangeright.put(news, positions.get(old));
		this.rangeright.remove(old);
		this.rangeleftpad.put(news, positions.get(old));
		this.rangeleftpad.remove(old);
		this.rangerightpad.put(news, positions.get(old));
		this.rangerightpad.remove(old);
	}

	public void addSEQ(String sequence, String qname) {
		reads.put(qname, new FourBitSequence(sequence));
	}

	public void addPOS(int start, String qname) {
		this.positions.put(qname, (short)start);
	}

	public void addOrientation(char orient, String qname) {
		
	}

	public void addRange(int start, int end, String qname) {
			this.rangeleft.put(qname, (short)(start-1));
			this.rangeright.put(qname, (short)(end-1));
	}

	public void addRangePadded(int start, int end, String qname) {
			this.rangeleftpad.put(qname, (short)(start-1));
			this.rangerightpad.put(qname, (short)(end-1));
	}

	public int getContigsSize() {
		return this.contigs.size();
	}

	public int getContigIndex(String contigname) {
		int iret = -1;
		for(Integer i : contignumb.keySet()){
			if(contignumb.get(i).contentEquals(contigname)){
				iret = i; 
				break;
			}
		}
		return iret;
	}
	
	//TODO test this
	public String getPaddedString(String qname){
		String read = getReadAsString(qname);
		StringBuilder bid = new StringBuilder();
		int pos = this.positions.get(qname);
		for(int i =0; i < pos; i++){
			bid.append("-");
		}
		pos = rangeleftpad.get(qname);
		bid.append(read.substring(0, pos).toLowerCase());
		bid.append(read.substring(pos, rangerightpad.get(qname)));
		bid.append(read.substring(rangerightpad.get(qname),read.length()).toLowerCase());
		return bid.toString();
	}
	
	public String getReadAsString(String qname){
		return this.reads.get(qname).getAsString();
	}

	/*
	 * (non-Javadoc)
	 * @see bio.assembly.Assembly#getDepthofContigAtPos(java.lang.String, int)
	 * This position should be 0-based
	 */
	public int getDepthofContigAtPos(String contigname, int position) {
		int depth = 0;
		for(String readname : read2contig.keySet()){
			if(read2contig == null){
				System.out.println("Read2contig is null");
			}
			if(readname == null){
				System.out.println("Readname is null");
			}
			if(contigname == null){
				System.out.println("contigname is null");
			}
			if(read2contig.get(readname).contentEquals(contigname)){
				if(positions.get(readname)+rangerightpad.get(readname) > position && positions.get(readname)+rangeleftpad.get(readname) < position){
					if(this.reads.get(readname).charAt(position-positions.get(readname)) != '*' && this.reads.get(readname).charAt(position-positions.get(readname)) != '-'){
						depth++;
					}
				}
			}
		}
		return depth;
	}

	
	public double getAverageCoverageDepth(String contigindex) {
		/*
		 * This method takes the depth of coverage at each bp of the consensus sequence
		 * (that is not a * or -) and averages the depth
		 */
		if(this.averagecoveragecalc == 0){
			double d1 = 0;
			for(int i = 0 ; i < contigs.get(contigindex).length();i++){
				if(contigs.get(contigindex).charAt(i) != '*' && contigs.get(contigindex).charAt(i) != '-')
					d1 += getDepthofContigAtPos(contigindex,i);
			}
			d1 /= contigs.get(contigindex).getActualLength();
			return d1;
		}
		/*
		 * Calculates average coverage based on the total base pairs divided by contig consensus length
		 * This seems less accurate, as it includes base calls which are not included in the final
		 * consensus and thus could be considered erroneous. However it produces results more similar than
		 * to other programs (ie CLCbios default coverage) than the above method.
		 */
		else{
			double d2 = (double)getTotalNoOfbpContig(contigindex)/(double)contigs.get(contigindex).length();
			return d2;
		}
	}

	public double getGlobalAverageCoverageDepth() {
		double val = 0;
		int count=0;
		for(String contig : contigs.keySet()){
			val+=getAverageCoverageDepth(contig);
			count++;
		}
		return val/(double)count;
	}

	//TODO
	public int getMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGlobalMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getReadsSize(String contigindex) {
		int count =0;
		for(String readname : read2contig.keySet()){
			if(read2contig.get(readname).contentEquals(contigindex)){
				count++;
			}
		}
		return count;
	}

	public double getAvgReadLength(String contigindex) {
		int count =0;
		double len = 0;
		for(String readname : read2contig.keySet()){
			if(read2contig.get(readname).contentEquals(contigindex)){
				count++;
				len +=reads.get(readname).getActualLength();
			}
		}
		return len/(double)count;
	}

	public int getTotalNoOfbpContig(String contigindex) {
		int len = 0;
		for(String readname : read2contig.keySet()){
			if(read2contig.get(readname).contentEquals(contigindex)){
				int a = reads.get(readname).getActualLength();
				len +=a-(a-rangerightpad.get(readname))-rangeleftpad.get(readname);
			}
		}
		return len;
	}

	/*
	 * This is general compicated by the way coverage is usually calculated
	 * as the no of bp / the length. As some bp will not be present in the consensus.
	 * Here I went for the idea that the range would be equivalent to the actual
	 * 
	 */
	public double[] getRangeOfCoverages(String contigindex, int range) {

		if(this.contigs.get(contigindex) != null){
			int orig = this.contigs.get(contigindex).getLength();
			int slimmed = this.contigs.get(contigindex).getActualLength();
			int len=slimmed/range;
			if(slimmed % range != 0)len++;
			double data[] = new double[len];
			int dataindex=0;
			int index=0;
			for(int i = 0; i < orig; i++){
				System.out.print("\r"+i);
				if(this.contigs.get(contigindex).charAt(i)=='-')index++;
				if(index == range){
					index=0;
					dataindex++;
				}
				data[dataindex] += this.getDepthofContigAtPos(contigindex, i);
			}
			for(int i =0; i < len-1; i++){
				data[i] /= range;
			}
			int reman= slimmed % range;
			if(slimmed % range == 0)data[len-1] /= range;
			else data[len-1] /= reman;
			System.out.print("\n");
			return data;
		}
		else{
			Logger.getRootLogger().fatal("That Contig Name " + contigindex + " does not exist");
			return null;
		}
	}

	public Fasta getFastaFromConsensus(){
		Fasta fasta = new Fasta();
		for(String name : this.contigs.keySet()){
			if(this.contigs.get(name) != null){
				String str = this.contigs.get(name).getAsString();
				str.replaceAll("-", "");
				fasta.addSequence(name, str);
			}
			else{
				Logger.getRootLogger().error("Such Fail. Contig Consensus Sequence for " + name + " doesn't seem to exist");
			}
		}
		return fasta;
	}
	
	public HashMap<String, String> getRead2Contig(){
		return this.read2contig;
	}
	
	public HashMap<String, String> getSequences(){
		HashMap<String, String> temp = new HashMap<String, String>();
		for(String name : this.contigs.keySet()){
			String str = this.contigs.get(name).toString();
			temp.put(name, str);
		}
		return temp;
	}

	public int getN50() {
		return Tools_Sequences.n50(getListOfLens());
	}

	public int[] getListOfLens() {
		System.out.println();
		if(lens == null){
			lens = new int[contigs.size()];
			int c =0;
			for(String contigname : contigs.keySet()){
				lens[c] =contigs.get(contigname).getActualLength();
				c++;
				System.out.print("\r"+c);
			}
		}
		System.out.println();
		return lens;
	}

	public int getNoOfBps() {
		return Tools_Math.sum(getListOfLens());
	}
	
	public long getNoOfReadBps(){
		long big = 0;
		for(String contigname : contigs.keySet()){
			big+=this.getTotalNoOfbpContig(contigname);
		}
		return big;
	}

	public long[] getAllStats() {
		return Tools_Sequences.SequenceStats(getListOfLens());
	}
	
	public int getNoOfSequences(){
		return this.contigs.size();
	}
	
}
