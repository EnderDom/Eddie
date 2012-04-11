package bio.assembly;

import org.apache.log4j.Logger;

import bio.sequence.FourBitSequence;

/**
 * 
 * @author dominic
 * This class basically holds the data for 1 contig
 * parsed from an .ace. It has grown a little akward, but
 * hopefully should become a bit of a black box.
 * 
 * TODO add all the data retrieval stuff
 */

public class ACERecord implements Cloneable {

	private StringBuilder current;
	private String contigname;
	private String[] readnames;
	//NOTE reads are offset by one relative to readnames due to consensus being shoved in seqs
	private FourBitSequence[] seqs;//Note, first sequence is consensus, if empty no consensus provided
	private boolean finalised;
	private int readcount;
	private int expectedlength;
	Logger logger = Logger.getLogger("ACEFileParser");
	private String consensusqual; //<-- Better way of storing quality?
	private StringBuilder cons;
	private int[][] offset;
	private int[][] regions;
	private int regioncount;
	private char[] compliments;
	//TODO sort out adding RD with quality data from a qual/fastq file
	
	public ACERecord(){
		current = new StringBuilder();
		cons = new StringBuilder();
	}
	
	public ACERecord clone()throws CloneNotSupportedException{
		if(!isFinalised())finalise();
		ACERecord newRecord = (ACERecord)super.clone();
		return newRecord;
	}
	
	public void addCurrentSequence(String line){
		current.append(line);
	}
	
	public void addQuality(String line){
		cons.append(line+" ");//Added space, as the line break usally replaces space in quality strings
	}
	
	public String getContigName(){
		return this.contigname;
	}
	
	public void setContigName(String name){
		this.contigname= name;
	}
	
	public void setNumberOfReads(int i){
		logger.trace("Number of reads set to " + i);
		this.seqs = new FourBitSequence[i+1]; //+1 for the consensus
		this.offset = new int[5][i];
		this.compliments = new char[i];
		this.readnames = new String[i];
	}
	
	public void setNumberOfRegions(int i){
		this.regions = new int[3][i];//0 stores start, 1 stores end, 2 stores read index for readnames/seqs
	}
	
	public void setReadName(String readname){
		seqs[readcount] = new FourBitSequence(current.toString());
		if(seqs[readcount].length() != expectedlength && seqs[readcount].getActualLength() != expectedlength){
			logger.warn("Expected length "+expectedlength+" of the read is not equal to its total("+seqs[readcount].getActualLength()+") or actual("+seqs[readcount].length()+") ([!*]) length ");
		}
		current = new StringBuilder();
		if(!readname.equals(readnames[readcount])){
			logger.warn("Someting has gone wrong, but I'm not sure what...:S");
		}
		readcount++;
	}
	
	public boolean isFinalised(){
		return this.finalised;
	}
	
	public void setFinalised(boolean c){
		this.finalised = c;
	}
	
	public void finalise(){
		this.consensusqual=cons.toString();
		cons = null;
		seqs[readcount] = new FourBitSequence(current.toString());
		current = null;
		setFinalised(true);
	}
	
	public void setExpectedLength(int l){
		this.expectedlength = l;
	}
	
	public void addOffSet(String name, int off, char c){
		logger.trace("Set readname " + name + " @" + readcount);
		readnames[readcount]=name;
		offset[0][readcount] = off;
		compliments[readcount] = c;
		readcount++;
		if(readcount == readnames.length){
			readcount=0;/*
			* Sets back to 0 for when read sequences are added, 
			* so we can use this variable for both rather than 2 vars
			*/
		}
	}
	//Assumes QA for read comes after read sequence thus readcount-1
	public void addQA(int i1, int i2, int i3, int i4){
		this.offset[1][readcount-1] = i1;
		this.offset[2][readcount-1] = i2;
		this.offset[3][readcount-1] = i3;
		this.offset[4][readcount-1] = i4;
	}
	
	public void addRegion(int i1, int i2, String readname){
		int l =-1;
		for(int i = 0; i < readnames.length; i++){
			if(readnames[i].equals(readname)){
				l=i;
				break;
			}
		}
		if(l == -1)logger.error("There is a region for a readname which doesn't pre-exist in the contig");
		regions[0][regioncount] = i1;
		regions[1][regioncount] = i2;
		regions[2][regioncount] = l;
		regioncount++;
	}
	
	public String getConsensusQualityLine(){
		return this.consensusqual;
	}
	
	public void setConsensusQuality(String str){
		this.consensusqual = str;
	}
	
	public FourBitSequence getConsensus(){
		return this.seqs[0];
	}
	
	public String getConsensusAsString(){
		return this.seqs[0].getAsString();
	}
	
	public FourBitSequence getRead(int i){
		return this.seqs[i-1];
	}
	
	public FourBitSequence getRead(String name){
		int l = getReadIndex(name);
		if(l == -1){
			logger.error("ACErecord does not contain the read name");
			return null;
		}
		else{
			return getRead(l);
		}
	}
	
	public String getReadAsString(int i){
		return getRead(i).getAsString();
	}
	
	public String getReadAsString(String name){
		return getRead(name).getAsString();
	}
	
	public int getReadIndex(String name){
		int l =-1;
		for(int i =0; i < readnames.length; i++){
			if(name.equalsIgnoreCase(name)){
				l=i;
				break;
			}
		}
		return l;		
	}
	
	public int getNoOfReads(){
		return this.readnames.length;
	}
	
	public int getReadOffset(int i){
		return offset[0][i];
	}
	
	public int[] getReadRange(int i){
		return new int[]{offset[1][i], offset[2][i]};
	}
	
	public int[] getReadRangePadded(int i){
		return new int[]{offset[3][i], offset[4][i]};
	}
	
}
