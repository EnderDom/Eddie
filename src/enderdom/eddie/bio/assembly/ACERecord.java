package enderdom.eddie.bio.assembly;

import java.io.File;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.Contig;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.bio.interfaces.UnsupportedTypeException;
import enderdom.eddie.bio.sequence.FourBitNuclear;
import enderdom.eddie.bio.sequence.FourBitSequence;

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
 * This currently stores sequence data as FourBitSequence
 * But this should be relatively trivial to change. You could store
 * as Strings and anywhere which requires return FourBitSequence
 * have a new Construcion there.
 * 
 */
public class ACERecord implements Contig{

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
	private int iteratorcount = 0;
	
	/**
	 * Constructor
	 */
	public ACERecord(){
		current = new StringBuilder();
		cons = new StringBuilder();
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
		current.append(line);
	}
	
	/**
	 * See addCurrentSequence
	 * @param line
	 */
	public void addQuality(String line){
		cons.append(line+" ");//Added space, as the line break usually replaces space in quality strings
	}
	
	/**
	 * 
	 * @return the reference name of this contig
	 */
	public String getContigName(){
		return this.contigname;
	}
	
	/**
	 * Sets the reference name of this contig
	 * @param name
	 */
	public void setContigName(String name){
		this.contigname= name;
	}
	
	/**
	 * Set the number of reads to be added
	 * this should be done as this is were the 
	 * read data objects are initialised
	 * @param i
	 */
	public void setNumberOfReads(int i){
		logger.trace("Number of reads set to " + i);
		this.seqs = new FourBitSequence[i+1]; //+1 for the consensus
		this.offset = new int[5][i];
		this.compliments = new char[i];
		this.readnames = new String[i];
	}
	
	/**
	 * Sets the number of regions (BS)
	 * @param i
	 */
	public void setNumberOfRegions(int i){
		this.regions = new int[3][i];//0 stores start, 1 stores end, 2 stores read index for readnames/seqs
	}
	
	/**
	 * Adds a read name to the contig, 
	 * this triggers the previous read to be finalised 
	 * @param readname
	 */
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
		this.consensusqual=cons.toString();
		cons = null;
		seqs[readcount] = new FourBitSequence(current.toString());
		current = null;
		setFinalised(true);
	}
	
	/**
	 * Sets the expected length of the read to be put
	 * into this contig object. If this is different
	 * a warning will be alerted (but not an error)
	 * @param l
	 */
	public void setExpectedLength(int l){
		this.expectedlength = l;
	}
	
	/**
	 * Add offset 'off' to read 'name' with complimentation 'c'
	 * @param name
	 * @param off
	 * @param c expected to be 'C' or 'U'
	 */
	public void addOffSet(String name, int off, char c){
		logger.trace("Set readname " + name + " @" + readcount);
		readnames[readcount]=name;
		offset[0][readcount] = off-1;
		compliments[readcount] = c;
		readcount++;
		if(readcount == readnames.length){
			readcount=0;/*
			* Sets back to 0 for when read sequences are added, 
			* so we can use this variable for both rather than 2 vars
			*/
		}
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
		this.offset[1][readcount-1] = i1-1;
		this.offset[2][readcount-1] = i2-1;
		this.offset[3][readcount-1] = i3-1;
		this.offset[4][readcount-1] = i4-1;
	}
	
	/**
	 * Add region for read 'readname'
	 * @param i1
	 * @param i2
	 * @param readname
	 */
	public void addRegion(int i1, int i2, String readname){
		int l =-1;
		for(int i = 0; i < readnames.length; i++){
			if(readnames[i].equals(readname)){
				l=i;
				break;
			}
		}
		if(l == -1)logger.error("There is a region for a readname which doesn't pre-exist in the contig");
		regions[0][regioncount] = i1-1;
		regions[1][regioncount] = i2-1;
		regions[2][regioncount] = l;
		regioncount++;
	}
	
	/**
	 * 
	 * @return consensus Quality as String
	 */
	public String getConsensusQualityLine(){
		return this.consensusqual;
	}
	
	/**
	 *
	 * @param str
	 */
	public void setConsensusQuality(String str){
		this.consensusqual = str;
	}
	
	/**
	 * 
	 * @return consensus as a FourBitSequence
	 * object
	 */
	public FourBitSequence getConsensusAs4(){
		return this.seqs[0];
	}
	
	/**
	 * 
	 * @return Consensus sequence as a String
	 */
	public FourBitNuclear getConsensus(){
		FourBitNuclear n = new FourBitNuclear(this.seqs[0]);
		n.setName(this.contigname);
		n.setQuality(this.consensusqual);
		return n;
	}
	
	/**
	 * 
	 * @param i
	 * @return read at i as FourBitSequence
	 */
	public FourBitSequence getRead(int i){
		return this.seqs[i+1];
	}
	
	/**
	 * 
	 * @param name
	 * @return sequence for read named 'name',
	 * else returns null if the read doesn't exist
	 */
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
	
	/**
	 * Remember, as with all methods, 0-based
	 * @param i
	 * @return read number i as string
	 */
	public String getReadAsString(int i){
		return getRead(i).getAsString();
	}
	
	/**
	 * 
	 * @param name
	 * @return read named as a String
	 */
	public String getReadAsString(String name){
		return getRead(name).getAsString();
	}
	
	/**
	 * 
	 * @param name
	 * @return the position in the contig that read
	 * is at, can use this index for method returning string
	 * using index
	 */
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
	
	/**
	 * 
	 * @param index
	 * @return read at that index
	 */
	public String getReadName(int index){
		return this.readnames[index];
	}
	
	/**
	 * 
	 * @return self explanatory
	 */
	public int getNoOfReads(){
		return this.readnames.length;
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
		FourBitNuclear seq = this.getConsensus();
		int actuallength = 0;
		int depth=0;
		for(int i =0; i < seq.length(); i++){
			if(seq.charAt(i) != '-'){
				depth=0;
				for(int j =0; j < this.getNoOfReads(); j++){
					int l = this.getReadOffset(j);
					if(i >= l && i < this.getReadRange(j)[1]+l){
						//TODO consider BS inclusion ranges <-- at the moment this is inaccurate without them						
						if(this.getRead(j).charAt(i+l) != '-'){
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
		int[] lens = new int[this.readcount];
		for(int i =0;i < lens.length; i++){
			lens[i] = seqs[i+1].getLength();
		}
		return lens;
	}
	

	public int[] getListOfActualLens() {
		int[] lens = new int[this.readcount];
		for(int i =0;i < lens.length; i++){
			lens[i] = seqs[i+1].getActualLength();
		}
		return lens;
	}

	public int getNoOfMonomers() {
		int t = 0;
		for(int i =0; i < this.readcount; i++){
			t=seqs[i+1].getActualLength();
		}
		return t;
	}

	public int getQuickMonomers() {
		int t = 0;
		for(int i =0; i < this.readcount; i++){
			t=seqs[i+1].getLength();
		}
		return t;
	}
	
	public int getNoOfSequences() {
		return this.readcount;
	}

	public SequenceObject getSequence(int i) {
		FourBitNuclear n = new FourBitNuclear(getRead(i));
		n.setName(this.getReadName(i));
		n.setQuality(this.consensusqual);
		return n;
	}

	public SequenceObject getSequence(String s) {
		FourBitNuclear n =new FourBitNuclear(getRead(s));
		n.setName(s);
		n.setQuality(this.consensusqual);
		return n;
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
		return iteratorcount+1 < this.seqs.length;
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
