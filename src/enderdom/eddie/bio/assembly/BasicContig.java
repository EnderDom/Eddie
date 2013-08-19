package enderdom.eddie.bio.assembly;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.lists.ClustalAlign;
import enderdom.eddie.bio.sequence.BasicRegion;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.Contig;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.ui.BasicPropertyLoader;

public class BasicContig implements Contig{

	protected LinkedHashMap<String, SequenceObject> sequences;
	protected String contigname;
	protected Logger logger = Logger.getRootLogger();
	protected int iteratorcount = 0;
	protected int[][] offset;
	protected char[] compliments;
	protected int position = 1;
	private char comcomp;
	protected ArrayList<BasicRegion> regions;
	
	public BasicContig(){
		this.sequences = new LinkedHashMap<String, SequenceObject>();
		this.contigname = "Unnamed_"+BasicPropertyLoader.numbercache++;
	}
	
	public BasicContig(String contig){
		this.sequences = new LinkedHashMap<String, SequenceObject>();
		this.contigname = contig;
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
			if(!s.equals(contigname)){
				lens[i] = sequences.get(s).getLength();
				i++;
			}
		}
		return lens;
	}
	
	public int[] getListOfActualLens() {
		int[] lens = new int[this.getNoOfSequences()];
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
		return this.sequences.size()-1;
	}

	public synchronized SequenceObject getSequence(int i) {
		for(String s : sequences.keySet()){
			if(sequences.get(s).getPositionInList()-1 == i){
				return sequences.get(s);
			}
		}
		return null;
	}
	
	public String getContigName() {
		return this.contigname;
	}


	public void setContigName(String s) {
		this.contigname = s;
	}

	public boolean hasNext() {
		return iteratorcount+1 < this.getNoOfSequences();
	}

	public SequenceObject next() {
		iteratorcount++;
		return this.getSequence(iteratorcount);
	}

	public void remove() {
		logger.error("Not implemented");
	}
	
	public int getCoverageAtBp(int position, int base) {
		int coverage = 0;
		for(int i =0; i < this.getNoOfSequences(); i++){
			char c = this.getCharAt(i, position, base);
			if(c == '-' || c=='*');
			else coverage++;
		}
		return coverage;
	}

	public char getCharAt(int sequencenumber, int position, int base) {
		int offset = position-this.offset[0][sequencenumber]-base;
		if(offset > -1 && offset < this.getSequence(sequencenumber).getSequence().length()){
			return this.getSequence(sequencenumber).getSequence().charAt(position-this.offset[0][sequencenumber]-base);
		}
		else{
			return '-';
		}
	}

	public void setNumberOfReads(int i){
		this.offset = new int[5][i];
		this.compliments = new char[i];
	}
	
	public void resetNumberOfReads(int iniu){
		int[][] temp = new int[5][iniu];
		char[] temp2 = new char[iniu];
		for(int i =0; i < offset[0].length; i++){
			for(int j = 0; j < 5; j++)temp[j][i] = offset[j][i];
			temp2[i] = this.compliments[i];
		}
		offset = temp;
		
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
	public SequenceObject getConsensus(){
		return sequences.get(contigname);
	}
	

	public void setConsensus(SequenceObject s) {
		this.sequences.put(contigname, s);
	}

	public boolean canAddSequenceObjects() {
		return true;
	}

	public void addSequenceObject(SequenceObject object) {
		object.setPositionInList(position);
		position++;
		this.sequences.put(object.getIdentifier(), object);
	}

	public boolean canRemoveSequenceObjects() {
		return true;
	}

	public void removeSequenceObject(String name) {
		this.sequences.remove(name);
	}
	
	public BioFileType getFileType() {
		return BioFileType.CONTIG_BASIC;
	}

	public SequenceObject getSequence(String s) {
		return sequences.get(s);
	}
		
	public String[] saveFile(File file, BioFileType filetype) throws Exception {
		if(filetype == BioFileType.CLUSTAL_ALN){
			ClustalAlign align = new ClustalAlign();
			for(String k : this.sequences.keySet()){
				if(sequences.get(k).getPositionInList()==0){
					align.addSequenceObject(sequences.get(k));
				}
				else{
					SequenceObject o = sequences.get(k);
					int off = offset[0][o.getPositionInList()-1];
					if(off < 0){
						o.leftTrim(off*-1, 0);
					}
					else if(off > 0){
						o.extendLeft(off);
					}
					align.addSequenceObject(o);
				}
			}
			return align.saveFile(file, filetype);
		}
		else{
			logger.error("Not implemented");
			return null;
		}
	}

	public int loadFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
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

	
	public void setRange(String s, int start, int stop){
		checkSequence(s);
		offset[1][sequences.get(s).getPositionInList()-1]= start;
		offset[2][sequences.get(s).getPositionInList()-1]= stop;
	}
	
	public void setPaddedRange(String s, int start, int stop){
		checkSequence(s);
		offset[3][sequences.get(s).getPositionInList()-1]= start;
		offset[4][sequences.get(s).getPositionInList()-1]= stop;
	}

	private void checkSequence(String s) {
		if(!sequences.containsKey(s)){
			sequences.put(s, new GenericSequence(s, "", position));
			position++;
		}
	}

	private void checkOffset(int index){
		if(offset == null){
			this.setNumberOfReads(index+1);
		}
		else if (offset.length <= index)this.resetNumberOfReads(index+1);
		else return;
	}
	
	private void checkCompliment(int index){
		if(compliments == null){
			this.setNumberOfReads(index+1);
		}
		else if (compliments.length <= index)this.resetNumberOfReads(index+1);
		else return;
	}

	public int createPosition() {
		return this.position++;
	}

	public void setOffset(String s, int off) {
		checkSequence(s);
		checkOffset(position);
		this.offset[0][sequences.get(s).getPositionInList()-1] = off;
	}

	public String[] getReadNames() {
		String[] str = new String[this.getNoOfSequences()];
		for(String s : this.sequences.keySet()){
			int i = this.sequences.get(s).getPositionInList();
			if(i != 0){
				str[i] = this.sequences.get(s).getIdentifier();
			}
		}
		return str;
	}

	public int getOffset(String s) {
		return this.offset[0][this.sequences.get(s).getPositionInList()-1];
	}

	public int[] getRange(String s) {
		return new int[]{
			this.offset[1][this.sequences.get(s).getPositionInList()-1],
			this.offset[2][this.sequences.get(s).getPositionInList()-1],
		};
	}
	

	public int[] getPaddedRange(String s) {
		return new int[]{
			this.offset[3][this.sequences.get(s).getPositionInList()-1],
			this.offset[4][this.sequences.get(s).getPositionInList()-1],
		};
	}

	public void setCompliment(String s, char c) {
		this.checkSequence(s);
		this.checkCompliment(position);
		this.compliments[sequences.get(s).getPositionInList()-1]=c;
		
	}

	public char getCompliment(String s) {
		return this.compliments[this.sequences.get(s).getPositionInList()-1];
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
	public void addRegion(int i1, int i2, String readname){		
		regions.add(new BasicRegion(i1, i2, 0, readname));
	}

	public ArrayList<BasicRegion> getRegions() {
		return this.regions;
	}

	public char getConsensusCompliment() {
		return this.comcomp;
	}

	public void setConsensusCompliment(char c) {
		this.comcomp = c;
	}

	
}
