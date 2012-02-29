package bio.fasta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import tools.bio.Tools_Fasta;

public class Fasta implements FastaHandler{

	private LinkedHashMap<String, String> sequences;
	private LinkedHashMap<String, String> qualities;
	private boolean fastq;
	
	public Fasta(){
		sequences = new LinkedHashMap<String, String>();
	}
	
	public Fasta(int fastasize){
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
		this.fastq =fastq;
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
	
	public void save2Fastq(File output) throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str), qualities.get(str))){
				Tools_Fasta.saveFastq(str, sequences.get(str), qualities.get(str), out);
			}
			else{
				throw new IOException("Fastq failed QC check");
			}
		}
		out.close();fstream.close();
	}
	
	public void save2Fasta(File output)throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str), qualities.get(str))){
				Tools_Fasta.saveFasta(str, sequences.get(str), out);
			}
			else{
				throw new IOException("Fasta failed QC check");
			}
		}
		out.close();fstream.close();
	}
	
	public void save2FastaAndQual(File output, File quality)throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		FileWriter fstream2 = new FileWriter(quality);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str), qualities.get(str))){
				Tools_Fasta.saveFasta(str, sequences.get(str), out);
				Tools_Fasta.saveFasta(str, Tools_Fasta.Fastq2Qual(qualities.get(str)), out2);
			}
			else{
				throw new IOException("Fasta failed QC check");
			}
		}
		out.close();out2.close();fstream.close();fstream2.close();
	}
}
