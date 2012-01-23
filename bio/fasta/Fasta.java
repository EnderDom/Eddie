package bio.fasta;

import java.util.HashMap;
import java.util.LinkedHashMap;

import tools.bio.Tools_Fasta;

public class Fasta implements FastaHandler{

	private LinkedHashMap<String, String> sequences;
	private LinkedHashMap<String, String> qualities;
	private boolean fastq;
	
	public void MultiFasta(){
		sequences = new LinkedHashMap<String, String>();
	}
	
	public void MultiFasta(int fastasize){
		sequences = new LinkedHashMap<String, String>(fastasize);
	}
	
	public void addSequence(String title, String sequence) {
		sequences.put(title, sequence);
	}

	public void addQuality(String title, String quality) {
		if(qualities == null)qualities = new LinkedHashMap<String, String>();
		if(fastq)qualities.put(title, quality);
		else qualities.put(title, Tools_Fasta.Qual2Fastq(quality));
	}

	public void addAll(String title, String sequence, String quality) {
		if(qualities == null)qualities = new LinkedHashMap<String, String>();
		addSequence(title, sequence);
		addQuality(title, quality);
	}

	public void setFastq(boolean fastq) {
		this.fastq =true;
		if(qualities == null)qualities = new LinkedHashMap<String, String>();
	}

	public boolean isFastq() {
		return fastq;
	}

	public HashMap<String, String> getSequences() {
		return sequences;
	}

	public void setSequences(LinkedHashMap<String, String> sequences) {
		this.sequences = sequences;
	}

	public HashMap<String, String> getQualities() {
		return qualities;
	}

	public void setQualities(LinkedHashMap<String, String> qualities) {
		this.qualities = qualities;
	}
	
	public String[] getsubFasta(String title){
		return new String[]{sequences.get(title), qualities.get(title)};
	}
	
	/*
	 * Will get a specific index of sequence,
	 * though may not be particularly quick???
	 */
	public String[] getsubFasta(int  i){
		int j=0;
		String[] ret = null;
		for(String title : sequences.keySet()){
			if(i == j){
				ret = new String[]{sequences.get(title), qualities.get(title)};
				break;
			}
			j++;
		}
		return ret;
	}
	
}
