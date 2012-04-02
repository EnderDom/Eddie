package bio.assembly;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import tools.Tools_Math;

import bio.sequence.Sequences;

/*
 * Class for holder statistics surrounding an ACE file
 * without actually holding the sequence data. 
 * Sequence Data is actually read on method call, rather 
 * than being held in the object
 * 
 */


//TODO <-- Incomplete
public class ACEIOHolder implements ACEHandler, Sequences, Assembly{

	private HashMap<String, Integer> contigs;
	private Vector<Vector<String>> reads;
	
	boolean readnotunique;
	private Vector<int[]> depth;
	private int[] length;
	private int[] actlength;
	private double avgReadLength;
	private int contigcount;
	private String readtemp;
	
	File file;
	
	public ACEIOHolder(File file){
		this.file = file;
	}
	
	public int getContigsSize() {
		return this.contigs.size();
	}

	public int getContigIndex(String contigname) {
		return contigs.get(contigname);
	}

	public int getDepthofContigAtPos(String contig, int position) {
		return depth.get(contigs.get(contig))[position];
	}

	public double getAverageCoverageDepth(String contig) {
		return Tools_Math.average(depth.get(contigs.get(contig)));
	}

	public double getGlobalAverageCoverageDepth() {
		double length =0;
		double bp = 0;
		for(String c : contigs.keySet()){
			int[] dep = depth.get(contigs.get(c));
			length +=dep.length;
			bp+= Tools_Math.sum(dep);
		}
		return bp/length;
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
		return reads.get(contigs.get(contigindex)).size();
	}

	public double getAvgReadLength(String contigindex) {
		return avgReadLength;
	}

	public int getTotalNoOfbpContig(String contigindex) {
		return length[contigs.get(contigindex)];
	}

	public void setRefName(String name) {
		contigs.put(name, contigcount);
		contigcount++;
	}

	public String getRefName(int i) {
		String r = null;
		for(String c : contigs.keySet()){
			if(contigs.get(c) == i){
				r =c;
				break;
			}
		}
		return r;
	}

	public void setRefConsensus(String buffer) {
		for(int i =0; i < buffer.length() ; i ++){
			if(buffer.charAt(i) != '*' && buffer.charAt(i) != '-'){
				length[contigcount]++;
			}
		}
		actlength[contigcount] = buffer.length();
		this.depth.add(new int[buffer.length()]);
	}

	public String getRefConsensus(String refname) {
		//TODO
		return null;
	}

	public void setRefConsensusQuality(String buffer) {
		// TODO Auto-generated method stub
		
	}

	public String getRefConsensusQuality(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRefLength(String refname) {
		return this.length[contigs.get(refname)];
	}

	public void setNoOfBases(int i) {
		
	}

	public void setNoOfReads(int i) {

	}

	public void setBaseSegments(int i) {
		
	}

	public void setOrientation(char orient) {
		
	}

	public String addQNAME(String name) {
		this.reads.get(contigcount).add(name);
		return name;
	}

	public void addSEQ(String sequence, String qname) {
		this.readtemp = sequence;
	}

	public void addPOS(int start, String qname) {
		//TODO
	}

	public void addOrientation(char orient, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addRange(int start, int end, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addRangePadded(int start, int end, String qname) {
		// TODO Auto-generated method stub
		
	}

	public int getN50() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int[] getListOfLens() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNoOfBps() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long[] getAllStats() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNoOfSequences() {
		// TODO Auto-generated method stub
		return 0;
	}

}
