package enderdom.eddie.bio.assembly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.lists.ClustalAlign;
import enderdom.eddie.bio.sequence.BasicRegion;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.Contig;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.GenericSequenceXT;
import enderdom.eddie.bio.sequence.SequenceObjectXT;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.ui.BasicPropertyLoader;

/**
 * Be warned, contigname is kind of set
 * in 3 or 4 separate locations contigname, the key for the 
 * consensus and the sequenceObject's identifier in this object. But
 * also potentially the key for this object in a parent contiglist
 *
 * This is a semi-WTF, but its kind of needed for when 
 * the consensus acts as its own entity and when the
 * contig acts as its own entity.Check setContigName
 * for what needs to be changed 
 * 
 * @author dominic
 *
 */
public class BasicContig implements Contig{

	protected HashMap<String, SequenceObjectXT> sequences;
	protected HashMap<Integer, String> readpos;
	protected SequenceObjectXT consensus;
	protected Logger logger = Logger.getRootLogger();
	protected int iteratorcount = 0;
	protected ArrayList<BasicRegion> regions;
	private boolean noQual2fastq;
	protected BioFileType type;
	
	public BasicContig(){
		this.sequences = new LinkedHashMap<String, SequenceObjectXT>();
		this.readpos = new HashMap<Integer, String>();
		consensus = new GenericSequenceXT("Unnamed_"+BasicPropertyLoader.numbercache++);
	}
	
	public BasicContig(String contig){
		this.sequences = new LinkedHashMap<String, SequenceObjectXT>();
		this.readpos = new HashMap<Integer, String>();
		consensus = new GenericSequenceXT(contig);
	}
	
	//Not all the relevant for ACErecord but
	public int getN50() {
		logger.error("Not implemented");
		return 0;
	}

	public int[] getListOfLens() {
		int[] lens = new int[this.getNoOfSequences()];
		int i=0;
		for(String s : sequences.keySet()){
			lens[i] = sequences.get(s).getLength();
			i++;
		}
		return lens;
	}
	
	public int[] getListOfActualLens() {
		int[] lens = new int[this.getNoOfSequences()];
		int i=0;
		for(String s : sequences.keySet()){
			lens[i] = sequences.get(s).getActualLength();
			i++;

		}
		return lens;
	}

	public int getNoOfMonomers() {
		return Tools_Math.sum(getListOfActualLens());
	}

	public int getQuickMonomers() {
		return Tools_Math.sum(getListOfLens());
	}
	
	/**
	 * @return number of reads in this contig
	 */
	public int getNoOfSequences() {
		return this.sequences.size();
	}

	public SequenceObjectXT getSequence(int i) {
		if(this.readpos.containsKey(i)){
			return this.sequences.get(readpos.get(i));
		}
		else return null;
	}
	
	public String getContigName() {
		return this.consensus.getIdentifier();
	}


	public void setContigName(String s) {
		this.consensus.setIdentifier(s);
	}

	public boolean hasNext() {
		return iteratorcount+1 < this.getNoOfSequences();
	}

	public SequenceObjectXT next() {
		iteratorcount++;
		return this.getSequence(iteratorcount);
	}

	public void remove() {
		logger.error("Not implemented");
	}
	
	public int getCoverageAtBp(int position, int base) {
		int coverage = 0;
		for(String read : this.getReadNames()){
			char c = this.getCharAtRelative2Contig(read, position, base);
			if(c == '-' || c=='*');
			else coverage++;
		}
		return coverage;
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> keySet() {
		return this.sequences.keySet();
	}

	/**
	 * 
	 * @return Consensus sequence as a SequenceObject
	 */
	public SequenceObjectXT getConsensus(){
		return this.consensus;
	}
		
	public void setConsensus(SequenceObjectXT s) {
		this.consensus = s;
	}

	public boolean canAddSequenceObjects() {
		return true;
	}

	public void addSequenceObject(SequenceObjectXT object) {
		this.readpos.put(this.sequences.size(), object.getIdentifier());
		this.sequences.put(object.getIdentifier(), object);
	}

	public boolean canRemoveSequenceObjects() {
		return true;
	}

	public void removeSequenceObject(String name) {
		if(this.sequences.containsKey(name)){
			int p =-1;
			while(!readpos.get(p++).equals(name));
			while(p++ < readpos.size())readpos.put(p--, readpos.get(p));
			readpos.remove(p--);
			this.sequences.remove(name);
		}
	}
	
	public BioFileType getFileType() {
		return type == null ? BioFileType.CONTIG_BASIC : type;
	}

	public SequenceObjectXT getSequence(String s) {
		return sequences.get(s);
	}
		
	public String[] saveFile(File file, BioFileType filetype) throws IOException, UnsupportedTypeException {
		if(filetype == BioFileType.CLUSTAL_ALN){
			ClustalAlign align = SequenceListFactory.Contig2Clustal(this);
			return align.saveFile(file, filetype);
		}
		else{
			logger.error("Not implemented");
			return null;
		}
	}
	
	public void getClustalAlign(){
		
	}

	public int loadFile(File file, BioFileType filetype) throws UnsupportedTypeException {
		logger.error("Not implemented");
		return -1;
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

	
	public void setRange(String s, int start, int stop, int base){
		checkSequence(s);
		sequences.get(s).setRange(start, stop, base);
	}
	
	public void setPaddedRange(String s, int start, int stop, int base){
		checkSequence(s);
		sequences.get(s).setPaddedRange(start, stop, base);
	}

	private void checkSequence(String s) {
		if(!sequences.containsKey(s)){
			this.addSequenceObject(new GenericSequenceXT(s));
		}
	}

	public String[] getReadNames() {
		return this.sequences.keySet().toArray(new String[0]);
	}

	public int getOffset(String s, int base) {
		return this.sequences.get(s).getOffset(base);
	}

	public void setOffset(String s, int offset, int base){
		checkSequence(s);
		this.sequences.get(s).setOffset(offset, base);
	}
	
	public int[] getRange(String s, int base) {
		return this.sequences.get(s).getRange(base);
	}

	public int[] getPaddedRange(String s, int base) {
		return this.sequences.get(s).getPaddedRange(base);
	}

	public void setCompliment(String s, char c) {
		this.sequences.get(s).setCompliment(c);
	}

	public char getCompliment(String s) {
		return this.sequences.get(s).getCompliments();
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
	 * Add region for read 'readname'
	 * @param i1
	 * @param i2
	 * @param readname
	 */
	public void addRegion(int i1, int i2, String readname, int base){		
		if(regions == null){
			regions = new ArrayList<BasicRegion>();
		}
		regions.add(new BasicRegion(i1-base, i2-base, 0, readname));
	}

	public ArrayList<BasicRegion> getRegions() {
		return this.regions;
	}

	public char getConsensusCompliment() {
		return this.getConsensus().getCompliments();
	}

	public void setConsensusCompliment(char c) {
		this.getConsensus().setCompliment(c);
	}

//	//Don't add consensus, use setConsensus
//	public void addSequenceObject(SequenceObject object) {
//		this.addSequenceObject(object.getAsSeqObjXT());
//	}
//	
	public char getCharAtRelative2Contig(String s, int position, int base) {
		return this.sequences.get(s).getSequence().charAt((position-base)+this.sequences.get(s).getOffset(0));
	}

	public boolean isNoQual2fastq() {
		return noQual2fastq;
	}

	public void setNoQual2fastq(boolean noQual2fastq) {
		this.noQual2fastq = noQual2fastq;
	}


	public void setFileType(BioFileType t){
		this.type = t;
	}

	public void addSequenceObject(SequenceObject object) {
		this.addSequenceObject(object.getAsSeqObjXT());
	}
	
}
