package enderdom.eddie.bio.sequence;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.bio.Tools_Sequences;

public abstract class BasicSequenceList implements SequenceList{

	protected LinkedHashMap<String, SequenceObject> sequences;
	protected int iterator = 0;
	protected Logger logger = Logger.getRootLogger();
	protected BioFileType type;
	protected String filename;
	protected String filepath;
	
	public boolean hasNext() {
		return this.iterator < this.sequences.size();
	}

	public SequenceObject next() {
		this.iterator++;
		return this.getSequence(iterator-1);
	}

	public void remove() {
		this.removeSequenceObject(this.getSequence(this.iterator).getIdentifier());
	}

	public int getN50(){
		return Tools_Sequences.n50(getListOfActualLens());
	}

	public int[] getListOfLens() {
		int[] li = new int[this.getNoOfSequences()];
		for(int i =0;i < li.length; i++){
			li[i] = this.getSequence(i).getActualLength();
		}
		return li;
	}
	
	public int[] getListOfActualLens() {
		int[] li = new int[this.getNoOfSequences()];
		for(int i =0;i < li.length; i++){
			li[i] = this.getSequence(i).getActualLength();
		}
		return li;
	}

	public int getNoOfMonomers() {
		return Tools_Math.sum(this.getListOfActualLens());
	}
	
	public int getQuickMonomers(){
		int i =0;
		for(String o : this.sequences.keySet()){
			i += this.sequences.get(o).getLength();
		}
		return i;
	}
	
	public int getNoOfSequences(){
		return this.sequences.size();
	}

	public synchronized SequenceObject getSequence(int i){
		for(String s : sequences.keySet()){
			if(i==0){
				return sequences.get(s);
			}
			i--;
		}
		return null;
	}
	
	public SequenceObject getSequence(String key){
		return this.sequences.get(key);
	}
	

	public boolean canAddSequenceObjects() {
		return true;
	}
	
	public boolean canRemoveSequenceObjects() {
		return true;
	}
	
	public void addSequenceObject(SequenceObject obj) {
		this.sequences.put(obj.getIdentifier(), obj);
	}


	public void removeSequenceObject(String name) {
		if(this.sequences.containsKey(name)){
			this.sequences.remove(name);
		}
		else{
			logger.warn("Attempting to remove sequence which does not exist " + name);
		}
	}
	public String[] saveFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		logger.fatal("This method should always be overwritten by subordinate class");
		return null;
	}

	public int loadFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		logger.fatal("This method should always be overwritten by subordinate class");
		return 0;
	}

	public BioFileType getFileType() {
		return this.type;
	}

	public String getFileName() {
		return this.filename;
	}

	public String getFilePath() {
		return this.filepath;
	}

	public Set<String> keySet() {
		return this.sequences.keySet();
	}
}
