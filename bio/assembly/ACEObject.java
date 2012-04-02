package bio.assembly;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import tools.Tools_Math;
import tools.bio.Tools_Sequences;

import bio.fasta.Fasta;
import bio.sequence.Sequences;


public class ACEObject implements ACEHandler, Assembly, Sequences {

	
	//ALL values are converted to 0 index/base
	/*
	 * Have a problem with multiple reads with same name
	 * this has a workaround, but I need to change the way
	 * the data is stored so reads are linked to contig better
	 * ie, having reads indexed as contig_read-no rather than their
	 * read name, as this can be duplicate in ace files
	 */
	
	HashMap<Integer, String> contignumb;
	HashMap<String, String> contigs;
	HashMap<String, String> qualities;
	
	HashMap<String, String> reads;
	HashMap<String, String> read2contig;
	
	HashMap<String, Integer> rangeleft;
	HashMap<String, Integer> rangeright;
	HashMap<String, Integer> rangeleftpad;
	HashMap<String, Integer> rangerightpad;
	HashMap<String, Integer> positions;
	
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
	
	
	public ACEObject(){
		contigs = new LinkedHashMap<String, String>();
		qualities = new LinkedHashMap<String, String>();
		read2contig = new LinkedHashMap<String, String>();
		reads = new LinkedHashMap<String, String>();
		contignumb = new LinkedHashMap<Integer, String>();
		positions = new LinkedHashMap<String, Integer>();
		rangeleft = new LinkedHashMap<String, Integer>();
		rangeright = new LinkedHashMap<String, Integer>();
		rangeleftpad = new LinkedHashMap<String, Integer>();
		rangerightpad = new LinkedHashMap<String, Integer>();
	}
	
	public void setRefName(String name) {
		currentcontig++;
		contigs.put(name, "");
		contignumb.put(currentcontig, name);
	}

	public String getRefName(int i) {
		return contignumb.get(i);
	}

	public void setRefConsensus(String buffer) {
		contigs.put(contignumb.get(currentcontig), buffer);
	}

	public String getRefConsensus(String refname) {
		return contigs.get(refname);
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
		reads.put(name, "");
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
		reads.put(qname, sequence);
	}

	public void addPOS(int start, String qname) {
		this.positions.put(qname, start);
	}

	public void addOrientation(char orient, String qname) {
		
	}

	public void addRange(int start, int end, String qname) {
			this.rangeleft.put(qname, start-1);
			this.rangeright.put(qname, end-1);
	}

	public void addRangePadded(int start, int end, String qname) {
			this.rangeleftpad.put(qname, start-1);
			this.rangerightpad.put(qname, end-1);
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
		return this.reads.get(qname);
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
			d1 /= contigs.get(contigindex).replaceAll("-", "").replaceAll("\\*", "").length();
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
				String read = reads.get(readname);
				read = read.replaceAll("\\*", "");
				read = read.replaceAll("-", "");
				len +=read.length();
			}
		}
		return len/(double)count;
	}

	public int getTotalNoOfbpContig(String contigindex) {
		int len = 0;
		for(String readname : read2contig.keySet()){
			if(read2contig.get(readname).contentEquals(contigindex)){
				String read = reads.get(readname);
				read = read.replaceAll("\\*", "");
				read = read.replaceAll("-", "");
				len +=read.length()-(read.length()-rangerightpad.get(readname))-rangeleftpad.get(readname);
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
			String original = this.contigs.get(contigindex);
			String slimmed = this.contigs.get(contigindex).replaceAll("\\*", "").replaceAll("-", "");
			int len=slimmed.length()/range;
			if(slimmed.length() % range != 0)len++;
			double data[] = new double[len];
			int dataindex=0;
			int index=0;
			for(int i = 0; i < original.length(); i++){
				System.out.print("\r"+i);
				if(original.charAt(i) != '*' && original.charAt(i) != '-')index++;
				if(index == range){
					index=0;
					dataindex++;
				}
				data[dataindex] += this.getDepthofContigAtPos(contigindex, i);
			}
			for(int i =0; i < len-1; i++){
				data[i] /= range;
			}
			int reman= slimmed.length() % range;
			if(slimmed.length() % range == 0)data[len-1] /= range;
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
			String str = this.contigs.get(name).replaceAll("\\*", "");
			fasta.addSequence(name, str);
		}
		return fasta;
	}
	
	public HashMap<String, String> getRead2Contig(){
		return this.read2contig;
	}
	
	public HashMap<String, String> getSequences(){
		return this.contigs;
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
				String contig = contigs.get(contigname).replaceAll("\\*", "");
				contig = contig.replaceAll("-", "");
				for(int i =0; i < contig.length(); i++){
					if(contig.charAt(i) != '-' && contig.charAt(i) != '*'){
						lens[c]++;						
					}
				}
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
