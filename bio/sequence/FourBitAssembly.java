package bio.sequence;

import java.util.Arrays;

import bio.assembly.Assembly;

public class FourBitAssembly implements Assembly{

	/*
	 * Experimental
	 */
	
	int[] contignames;
	FourBitSequence[] contigs;
	byte[][] qualities;
	int[][] readnames;
	FourBitSequence[][] reads; //<--- Note reads only contain non-matching regions, use CIGAR string to id
	byte[][] cigars;
	private static int DEFAULT_SIZE = 1024;
	
	public FourBitAssembly(){
		
	}
	
	public void addContigName(int i, String name){
		if(contignames.length < i)extendContigs();
		contignames[i] = Arrays.hashCode(name.toCharArray());
	}
	
	public void addContig(int i, String contig){
		if(contigs.length < i)extendContigs();
		contigs[i] = new FourBitSequence(contig);
	}
	
	public void addContigQuality(int i, String values){
		if(qualities.length < i){
			extendQualities();
		}
		String[] val = values.split(" ");
		qualities[i] = new byte[val.length];
		int c =0;
		for(String str : val){
			if(str.length() > 0){
				qualities[i][c] = Byte.parseByte(str);
				c++;
			}
		}
	}
	
	public String addQName(int i,int j, String qname){
		if(readnames.length < i)extendReads();
		if(readnames[i].length < j)extendSubReads(i);
		readnames[i][j] = Arrays.hashCode(qname.toCharArray());
		return qname;
	}
	
	public void addSeq(String read){
		//TODO complicated shit
	}
	
	public int getContigsSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getContigIndex(String contigname) {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getDepthofContigAtPos(String contig, int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getAverageCoverageDepth(String contig) {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getGlobalAverageCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getGlobalMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getReadsSize(String contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getAvgReadLength(String contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getTotalNoOfbpContig(String contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void extendQualities(){
		byte[][] arr_temp = new byte[this.qualities.length+DEFAULT_SIZE][];
		for(int i =0; i < this.qualities.length; i++){
			arr_temp[i] = this.qualities[i]; 
		}
		this.qualities = arr_temp;
	}
	
	private void extendContigs(){
		FourBitSequence[] seqs = new FourBitSequence[this.contigs.length+DEFAULT_SIZE];
		for(int i =0; i < this.contigs.length; i++){
			seqs[i] = this.contigs[i];
		}
		this.contigs=seqs;
		int[] names = new int[this.contignames.length+DEFAULT_SIZE];
		for(int i =0; i < this.contigs.length; i++){
			names[i] = this.contignames[i];
		}
		this.contignames = names;
	}
	
	private void extendReads(){
		int[][] readsa = new int[this.readnames.length+DEFAULT_SIZE][];
		for(int i =0; i < this.readnames.length; i++){
			readsa[i] = this.readnames[i];
		}
		this.readnames = readsa;
	}
	
	private void extendSubReads(int j){
		int[] arr = new int[this.readnames[j].length+10];
		for(int i =0; i < this.readnames[j].length; i++){
			arr[i] = this.readnames[j][i];
		}
		this.readnames[j] = arr;
	}
	
}
