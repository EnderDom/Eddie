package enderdom.eddie.bio.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.SequenceList;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.bio.interfaces.UnsupportedTypeException;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.bio.Tools_Sequences;

//TODO implement
//@stub
public class ClustalAlign implements SequenceList{

	private String filename;
	private String filepath;
	SequenceObject[] sequences;
	int iterator =0;
	Logger logger = Logger.getRootLogger();
	
	public ClustalAlign(File file, BioFileType type) throws UnsupportedTypeException, Exception{
		loadFile(file, type);
	}
	
	//TODO improve with parse method
	public boolean hasNext() {
		return iterator < sequences.length;
	}

	//TODO improve with parse method
	public SequenceObject next() {
		iterator++;
		return sequences[iterator-1];
	}

	public void remove() {
		//TODO
	}

	public int getN50() {
		return Tools_Sequences.n50(this.getListOfActualLens());
	}

	public int[] getListOfLens() {
		int[] ins = new int[this.sequences.length];
		for(int i =0;i < sequences.length;i++){
			ins[i] = sequences[i].getLength();
		}
		return ins;
	}

	public int[] getListOfActualLens() {
		int[] ins = new int[this.sequences.length];
		for(int i =0;i < sequences.length;i++){
			ins[i] = sequences[i].getActualLength();
		}
		return ins;
	}

	public int getNoOfMonomers() {
		return Tools_Math.sum(getListOfActualLens());
	}

	public int getNoOfSequences() {
		return sequences.length;
	}

	public SequenceObject getSequence(int i) {
		return sequences[i];
	}

	public SequenceObject getSequence(String s) {
		for(int i=0;i < sequences.length;i++){
			if(sequences[i].getName().equals(s))return sequences[i];
		}
		return null;
	}

	public String[] saveFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		logger.error("Not yet implemented");
		return null;
	}

	/**
	 * Quick & Dirty Clustal parser
	 */
	public int loadFile(File file, BioFileType filetype) throws Exception, UnsupportedTypeException {
		int counter=0;
		
		if(file.isFile()){
			filename = file.getName();
			filepath = file.getPath();
			LinkedList<String> nams = new LinkedList<String>();
			LinkedList<String> seqs = new LinkedList<String>();
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			boolean started = false;
			while((line=reader.readLine()) != null){
				if(started){
					if(line.length() > 0){
						boolean fin = true;
						String[] lines = line.split("      ");
						if(lines.length < 2) throw new Exception("Cannot parse clustal");
						for(int i =0;i < nams.size(); i++){
							if(nams.get(i).equals(lines[0])){
								seqs.get(i).concat(line);
								fin=true;
							}
						}
						if(!fin){
							nams.add(lines[0]);
							seqs.add(lines[1]);
							counter++;
						}
					}
				}
				if(line.startsWith("CLUSTAL"))started=true;
			}
			if(!started)throw new UnsupportedTypeException("CLUSTAL does not have standard Clustal header");
			else{
				this.sequences = new GenericSequence[seqs.size()];
				for(int i =0; i < nams.size(); i++){
					this.sequences[i] = new GenericSequence(nams.get(i), seqs.get(i));
				}
			}
		}
		else{
			throw new Exception("Not a file!");
		}
		return counter;
	}

	public BioFileType getFileType() {
		return BioFileType.CLUSTAL_ALN;
	}

	public String getFileName() {
		return filename;
	}

	public String getFilePath() {
		return filepath;
	}

	public boolean canAddSequenceObjects() {
		return true;
	}

	public void addSequenceObject(SequenceObject object) {
		SequenceObject[] temp = new GenericSequence[this.sequences.length+1];
		for(int i =0;i < sequences.length;i++){
			temp[i] = sequences[i];
		}
		temp[this.sequences.length] = object;
		this.sequences = temp;
	}

}
