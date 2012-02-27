package bio.assembly;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;


public class ACEObject implements ACEHandler, Assembly {

	
	//ALL values are converted to 0 index/base
	
	LinkedHashMap<Integer, String> contignumb;
	LinkedHashMap<String, String> contigs;
	LinkedHashMap<String, String> qualities;
	
	LinkedHashMap<String, String> reads;
	LinkedHashMap<String, String> read2contig;
	
	LinkedHashMap<String, Integer> rangeleft;
	LinkedHashMap<String, Integer> rangeright;
	LinkedHashMap<String, Integer> rangeleftpad;
	LinkedHashMap<String, Integer> rangerightpad;
	
	int currentcontig = -1;
	/*
	 * Sets the methods for calculating average coverage 
	 */
	public int averagecoveragecalc = 1;
	
	public ACEObject(){
		contigs = new LinkedHashMap<String, String>();
		qualities = new LinkedHashMap<String, String>();
		read2contig = new LinkedHashMap<String, String>();
		reads = new LinkedHashMap<String, String>();
		contignumb = new LinkedHashMap<Integer, String>();
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
		// TODO Auto-generated method stub
		
	}

	public void addQNAME(String name) {
		reads.put(name, "");
		read2contig.put(name, this.getRefName(currentcontig));
	}

	public void addSEQ(String sequence, String qname) {
		reads.put(qname, sequence);
	}

	public void addPOS(int start, String qname) {

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

	/*
	 * (non-Javadoc)
	 * @see bio.assembly.Assembly#getDepthofContigAtPos(java.lang.String, int)
	 * This position should be 0-based
	 */
	public int getDepthofContigAtPos(String contigname, int position) {
		int depth = 0;
		for(String readname : read2contig.keySet()){
			if(read2contig.get(readname).contentEquals(contigname)){
				if(rangerightpad.get(readname) > position && rangeleftpad.get(readname) < position){
					if(this.reads.get(readname).charAt((position)-rangeleft.get(readname)) != '*' && this.reads.get(readname).charAt((position)-rangeleft.get(readname)) != '-'){
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

	public int getGlobalNoOfbp() {
		int len =0;
		for(String readname : reads.keySet()){
			String read = reads.get(readname);
			read = read.replaceAll("\\*", "");
			read = read.replaceAll("-", "");
			len +=read.length();
		}
		return len;
	}

	public double[] getRangeOfCoverages(String contigindex, int range) {
		if(this.contigs.get(contigindex) != null){
			Logger.getRootLogger().trace("Contig Size " + this.contigs.get(contigindex).length());
			int arraysize = this.contigs.get(contigindex).length()/range;
			if(this.contigs.get(contigindex).length()%range != 0)arraysize++;
			Logger.getRootLogger().trace("Array Size of "+ arraysize + " Calculated");
			double[] data = new double[arraysize];
			for(int i = 0 ; i < arraysize;i++){
				System.out.print("\rCalculating..."+(i+1) +" of " + arraysize);
				for(int j =0; j < range; j++){
					data[i] += getDepthofContigAtPos(contigindex,(i*100)+j);
					if(j == range-1)data[i]/=j;
				}
			}
			System.out.print("\n");
			return data;
		}
		else{
			Logger.getRootLogger().fatal("That Contig Name " + contigindex + " does not exist");
			return null;
		}
	}

	

}
