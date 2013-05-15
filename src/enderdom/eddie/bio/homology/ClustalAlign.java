package enderdom.eddie.bio.homology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;


import enderdom.eddie.bio.sequence.BasicSequenceList;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

/**
 * 
 * Bit of Parse stuff nicked from ClustalWAlignmentSAXParser,
 * didn't won't to use the actual parser but too 
 * lazy to write full parser myself :(
 *  
 *
 */
public class ClustalAlign extends BasicSequenceList{

	//TODO implement contig
	private String line;
	public static int clustallen = 60;
	public static int whitespace =6;
	protected File file;
	
	public ClustalAlign(File file, BioFileType type) throws UnsupportedTypeException, Exception{
		this.file=file;
		loadFile(file, type);
	}
	
	public ClustalAlign(InputStream in, BioFileType type) throws UnsupportedTypeException, Exception{
		loadFile(in, type);
	}
	
	public ClustalAlign(){
		
	}

	public String[] saveFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		if(filetype == BioFileType.CLUSTAL_ALN){
			return null;
		}
		else{
			throw new Exception("Can't save Clustal as " + filetype.toString());
		}
	}

    private boolean lineIsRelevant() {
    	if(line.startsWith(" ")) return false;
    	line=line.trim();
		if (line.equals("") ||
		    line.startsWith("CLUSTAL")) {
			return false;
		}
		return true;
    }
	
	
	public int loadFile(InputStream fis, BioFileType filetype) throws Exception, UnsupportedTypeException {
		int counter=0;
		
		LinkedList<String> nams = new LinkedList<String>();
		LinkedList<String> seqs = new LinkedList<String>();
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		boolean started = false;
		while((line=reader.readLine()) != null){
			if(lineIsRelevant()){
				line=line.trim();
				boolean fin = false;
				int ind = line.lastIndexOf(" ");
				if(ind == -1)ind = line.lastIndexOf("\t");
				if(ind != -1){
					for(int i =0;i < nams.size(); i++){
						if(nams.get(i).equals(line.substring(0, ind).trim())){
							seqs.set(i, seqs.get(i).concat(line.substring(ind+1)));
							fin=true;
						}
					}
					if(!fin){
						nams.add(line.substring(0, ind).trim());
						seqs.add(line.substring(ind+1));
						
						counter++;
					}
				}
			}
			else if(line.startsWith("CLUSTAL"))started=true;
		}
		
		if(!started)logger.warn("CLUSTAL does not have standard Clustal header");

		this.sequences = new LinkedHashMap<String, SequenceObject>();
		for(int i =0; i < nams.size(); i++){
			if(this.sequences.containsKey(nams.get(i))){
				int j = 0;
				while(this.sequences.containsKey(nams.get(i) + "_" + j))j++;
				nams.set(i, nams.get(i) + "_" + j);
			}
			this.sequences.put(nams.get(i),new GenericSequence(nams.get(i), seqs.get(i)));
		}
		
		logger.info("Parsed " + this.sequences.size() + " sequences from clustal file");
		return counter;
	}
	
	/**
	 * Quick & Dirty Clustal parser
	 */
	public int loadFile(File file, BioFileType filetype) throws Exception, UnsupportedTypeException {
		filename = file.getName();
		filepath = file.getPath();
		FileInputStream fis = new FileInputStream(file);
		
		return loadFile(fis, filetype);
	}

	public BioFileType getFileType() {
		return BioFileType.CLUSTAL_ALN;
	}


}
