package enderdom.eddie.bio.assembly;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.Contig;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.bio.interfaces.UnsupportedTypeException;
import enderdom.eddie.bio.sequence.BasicRegion;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.tools.Tools_Math;


/**
 * 
 * @author Dominic Matthew Wood
 * This class basically holds the data for 1 contig
 * parsed from an .ace. It has grown a little akward, but
 * hopefully should become a bit of a black box.
 * 
 * 
 * NOTE::: ALL NUCLEOTIDE INDEXES HELD AS 0-BASED
 * ALL NUCLEOTIDE INDEXES RETURNED AS 0-BASED!!!!!!!!!!!!!
 * 
 * 
 * This currently stores sequence data as GenericSequence
 * But this should be relatively trivial to change. You could store
 * as Strings and anywhere which requires return GenericSequence
 * have a new Construcion there.
 * 
 */
public class ACERecord implements Contig{

	private StringBuilder sequencebuffer;
	private StringBuilder contigbuffer;
	private StringBuilder qualitybuffer;
	private String contigname;
	private LinkedHashMap<String, SequenceObject> sequences;
	private ArrayList<BasicRegion> regions;
	private boolean finalised;
	private int arraycount =0;
	private int position =1;
	private String currentread;
	Logger logger = Logger.getLogger("ACEFileParser");
	private int[][] offset;
	private char[] compliments;
	private int iteratorcount = 0;
	
	/**
	 * Constructor
	 */
	public ACERecord(){
		sequences = new LinkedHashMap<String, SequenceObject>();
		sequencebuffer = new StringBuilder();
		qualitybuffer = new StringBuilder();
		contigbuffer = new StringBuilder();
		regions = new ArrayList<BasicRegion>();
	}
	
	/** 
	 * @see java.lang.Object#clone()
	 * @return ACERecord
	 */
	public ACERecord clone() throws CloneNotSupportedException{
		if(!isFinalised())finalise();
		ACERecord newRecord = (ACERecord)super.clone();
		return newRecord;
	}
	
	/**
	 * 
	 * Adds line to the current read
	 * sequence. This will be pushed to the read
	 * seqs array when a new read is being parsed
	 * or finalised is called. 
	 * @param line
	 * 
	 */
	public void addCurrentSequence(String line){
		sequencebuffer.append(line);
	}
	
	public void addContigSequence(String line){
		contigbuffer.append(line);
	}
	
	/**
	 * See addCurrentSequence
	 * @param line
	 */
	public void addQuality(String line){
		qualitybuffer.append(line+" ");//Added space, as the line break usually replaces space in quality strings
	}
	
	/**
	 * 
	 * @return the reference name of this contig
	 */
	public String getContigName(){
		return contigname;
	}
	
	/**
	 * Sets the reference name of this contig
	 * @param name
	 */
	public void setContigName(String name){
		logger.trace("Contig name set as " + name);
		this.contigname = name;
	}
	
	/**
	 * Set the number of reads to be added
	 * this should be done as this is were the 
	 * read data objects are initialised
	 * @param i
	 */
	public void setNumberOfReads(int i){
		logger.trace("Number of reads set to " + i);
		this.offset = new int[5][i];
		this.compliments = new char[i];
	}
	
	/**
	 * Sets the number of regions (BS)
	 * 
	 * @param i
	 */
	public void setNumberOfRegions(int i){
		ArrayList<BasicRegion> regs = new ArrayList<BasicRegion>(i);
		if(regions.size() !=0){
			for(int j=0;j < regions.size(); j++){
				regs.add(regions.get(j));
			}
		}
		regions = regs;
	}
	
	/**
	 * Adds a read name to the contig, 
	 * this triggers the previous read to be finalised 
	 * @param readname
	 */
	public void setReadName(String readname){
		if(readname!=null){
			sequences.put(readname, new GenericSequence(readname, position));	
		}
		sequencebuffer = new StringBuilder();
		currentread=readname;
		position++;
		
	}

	
	/**
	 * 
	 * @return returns boolean if finalised
	 */
	public boolean isFinalised(){
		return this.finalised;
	}
	
	private void setFinalised(boolean c){
		this.finalised = c;
	}
	
	/**
	 * Finalise the contig, any buffers will
	 * be put in order, last read will be put into array
	 * etc.
	 */
	public void finalise(){
		sequences.put(contigname, new GenericSequence(this.contigname, this.contigbuffer.toString(), 
				this.qualitybuffer.toString(),0));
		sequences.get(currentread).setSequence(this.sequencebuffer.toString());
		this.sequencebuffer = null;
		setFinalised(true);
	}

	
	/**
	 * Add offset 'off' to read 'name' with complimentation 'c'
	 * @param name
	 * @param off
	 * @param c expected to be 'C' or 'U'
	 */
	public void addOffSet(String name, int off, char c){
		logger.trace("Set readname " + name + " @" + arraycount);
		offset[0][arraycount] = off-1;
		compliments[arraycount] = c;
		arraycount++;
	}

	/**
	 * 
	 * Assumes QA for read comes after read sequence thus readcount-1
	 * if it doesn't this will cause errors, as yet
	 * this errors will go uncaught
	 * 
	 * @param i1
	 * @param i2
	 * @param i3
	 * @param i4
	 */
	public void addQA(int i1, int i2, int i3, int i4){
		this.offset[1][arraycount-1] = i1-1;
		this.offset[2][arraycount-1] = i2-1;
		this.offset[3][arraycount-1] = i3-1;
		this.offset[4][arraycount-1] = i4-1;
	}
	
	/**
	 * Add region for read 'readname'
	 * @param i1
	 * @param i2
	 * @param readname
	 */
	public void addRegion(int i1, int i2, String readname){		
		regions.add(new BasicRegion(i1, i2, 0, readname));
	}
	

	/**
	 * 
	 * @return Consensus sequence as a SequenceObject
	 */
	public SequenceObject getConsensus(){
		return sequences.get(contigname);
	}
	
	/**
	 * 
	 * @param i
	 * @return read at i as GenericSequence
	 */
	public SequenceObject getRead(int i){
		for(String key : sequences.keySet()){
			if(sequences.get(key).getPositionInList()-1==i)return sequences.get(key); 
		}
		return null;
	}
	
	/**
	 * 
	 * @param name
	 * @return sequence for read named 'name',
	 * else returns null if the read doesn't exist
	 */
	public SequenceObject getRead(String name){
		return sequences.get(name);
	}
	
	
	/**
	 * 
	 * @param name
	 * @return the position in the contig that read
	 * is at, can use this index for method returning string
	 * using index
	 */
	public int getReadIndex(String name){
		return sequences.get(name).getPositionInList();
	}
	
	/**
	 * 
	 * @param index
	 * @return read at that index
	 */
	public String getReadName(int index){
		return this.getRead(index).getName();
	}
	
	/**
	 * 
	 * @return self explanatory
	 */
	public int getNoOfReads(){
		return this.sequences.size()-1;
	}
	
	/**
	 * Get offset for read at i in contig
	 * @param i
	 * @return int
	 */
	public int getReadOffset(int i){
		return offset[0][i];
	}
	
	/**
	 * @param i
	 * @return two values, start and end of range, as a int[].length ==2
	 */
	public int[] getReadRange(int i){
		return new int[]{offset[1][i], offset[2][i]};
	}
	
	/**
	 * @param i
	 * @return two values, start and end of range, as a int[].length ==2
	 */
	public int[] getReadRangePadded(int i){
		return new int[]{offset[3][i], offset[4][i]};
	}
	
	/**
	 * 
	 * INDEV
	 * Not sure what to do about the fact that coverage is often
	 * considered as length/no. of bps, as such bps which are not
	 * actually in the consensus are included in the coverage count
	 * but when we consider the contig, on a per-bp basis, this causes
	 * a minor issue, to we include the nucleotides, not actual aligned
	 * to the consensus contig, in this case I haven't
	 * 
	 *  @return Returns an int array of length equal to consensus, each
	 *  position has an integer value which is a count of bps in 
	 *  read at equivalent aligned position should be equivalent to coverage
	 *  
	 */
	public int[] getDepthMap(){
		int[] arr = new int[this.getConsensus().getActualLength()];
		SequenceObject seq = sequences.get(contigname);
		int actuallength = 0;
		int depth=0;
		for(int i =0; i < seq.getSequence().length(); i++){
			if(seq.getSequence().charAt(i) != '-'){
				depth=0;
				for(int j =0; j < this.getNoOfReads(); j++){
					int l = this.getReadOffset(j);
					if(i >= l && i < this.getReadRange(j)[1]+l){
						//TODO consider BS inclusion ranges <-- at the moment this is inaccurate without them						
						if(this.getRead(j).getSequence().charAt(i+l) != '-'){
							depth++;
						}
					}
				}
				arr[actuallength] = depth;
				actuallength++;
			}
		}
		return arr;
	}
	
	/**
	 * @return Total Sum of lengths of all reads minus non-bp (ie '*'/'-')
	 */
	public int getTotalBpofReads(){
		int r =0;
		for(int i =0; i < this.getNoOfReads(); i++){
			r+=this.getRead(i).getActualLength();
		}
		return r;
	}
	
	/**
	 * 
	 * @param read index
	 * @return a char, should be 'C' or 'U', but this
	 * isn't checked
	 */
	public char getReadCompliment(int read){
		return this.compliments[read];
	}

	//Not all the relevant for ACErecord but
	public int getN50() {
		logger.error("Not implemented");
		return 0;
	}

	public int[] getListOfLens() {
		int[] lens = new int[this.getNoOfReads()];
		int i=0;
		for(String s : sequences.keySet()){
			if(!s.equals(contigname)){
				lens[i] = sequences.get(s).getLength();
				i++;
			}
		}
		return lens;
	}
	

	public int[] getListOfActualLens() {
		int[] lens = new int[this.getNoOfReads()];
		int i=0;
		for(String s : sequences.keySet()){
			if(!s.equals(contigname)){
				lens[i] = sequences.get(s).getActualLength();
				i++;
			}
		}
		return lens;
	}

	public int getNoOfMonomers() {
		return Tools_Math.sum(getListOfActualLens());
	}

	public int getQuickMonomers() {
		return Tools_Math.sum(getListOfLens());
	}
	
	public int getNoOfSequences() {
		return this.getNoOfReads();
	}

	public SequenceObject getSequence(int i) {
		for(String s : sequences.keySet()){
			if(sequences.get(s).getPositionInList()-1 == i){
				return sequences.get(s);
			}
		}
		return null;
	}

	public SequenceObject getSequence(String s) {
		return sequences.get(s);
	}
		
	public String[] saveFile(File file, BioFileType filetype) throws Exception {
		logger.error("Not implemented");
		return null;
	}

	public int loadFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		logger.error("Not implemented");
		return -1;
	}

	public boolean hasNext() {
		return iteratorcount+1 < this.getNoOfReads();
	}

	public SequenceObject next() {
		iteratorcount++;
		return this.getSequence(iteratorcount);
	}

	public void remove() {
		logger.error("Not implemented");
	}

	public int trimLeftAllContig() {
		logger.error("Not implemented");
		return 0;
	}

	public int trimRightAllContig() {
		logger.error("Not implemented");
		return 0; 
	}

	public Contig[] removeSectionAllContig(int opts) {
		logger.error("Not implemented");
		return null;
	}

	public String getName(){
		return contigname;
	}

	public BioFileType getFileType() {
		return BioFileType.ACE_SECTION;
	}

	public String getFileName() {
		logger.error("Not implemented");
		return null;
	}

	public String getFilePath() {
		logger.error("Not implemented");
		return null;
	}

	//TODO
	public boolean canAddSequenceObjects() {
		return false;
	}

	public void addSequenceObject(SequenceObject object) {
		logger.error("Currently no support for add sequences to ACERecord");
	}

	public boolean canRemoveSequenceObjects() {
		return false;
	}

	public void removeSequenceObject(String name) {
		logger.error("Currently no support for add sequences to ACERecord");
	}

	
}

