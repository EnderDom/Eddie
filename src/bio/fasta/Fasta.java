package bio.fasta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import bio.sequence.Sequences;

import tools.bio.Tools_Fasta;
import tools.bio.Tools_Sequences;


/**
 * @author dominic
 * This ought to be replace with something from 
 * BioJava,  but I don't have time
 */

public class Fasta implements FastaHandler, Sequences{

	private LinkedHashMap<String, String> sequences;
	private LinkedHashMap<String, String> qualities;
	private boolean fastq;
	private int[] list_of_lens; 
	Logger logger = Logger.getRootLogger();
	
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

	public void remove(String title){
		sequences.remove(title);
		if(qualities.containsValue(title)) qualities.remove(title);
	}
	
	public void setFastq(boolean fastq) {
		this.fastq =fastq;
		if(qualities == null)qualities = new LinkedHashMap<String, String>();
	}

	public boolean isFastq() {
		return fastq;
	}

	public LinkedHashMap<String, String> getSequences() {
		return sequences;
	}

	public void setSequences(LinkedHashMap<String, String> sequences) {
		this.sequences = sequences;
	}

	public LinkedHashMap<String, String> getQualities() {
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
	
	public boolean save2Fastq(File output) throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		int count =0;
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str), qualities.get(str))){
				Tools_Fasta.saveFastq(str, sequences.get(str), qualities.get(str), out);
			}
			else{
				throw new IOException("Fastq failed QC check");
			}
			count++;
		}
		out.close();fstream.close();
		if(count == sequences.keySet().size())return true;
		else return false;
	}
	
	public boolean save2Fasta(File output) throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		int count = 0;
		for(String str : sequences.keySet()){
			Tools_Fasta.saveFasta(str, sequences.get(str), out);
			count++;
		}
		out.close();fstream.close();
		logger.info("Fasta Saved Successfully");
		if(count == sequences.keySet().size())return true;
		else return false;
	}
	
	public boolean save2FastaAndQual(File output, File quality)throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		FileWriter fstream2 = new FileWriter(quality);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		int count = 0;
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str), qualities.get(str))){
				Tools_Fasta.saveFasta(str, sequences.get(str), out);
				Tools_Fasta.saveFasta(str, Tools_Fasta.Fastq2Qual(qualities.get(str)), out2);
				count++;
			}
			else{
				throw new IOException("Fasta failed QC check");
			}
		}
		out.close();out2.close();fstream.close();fstream2.close();
		if(count == sequences.keySet().size())return true;
		else return false;
	}
	

	
	public int getNoOfBps(){
		int l = 0;
		int[] i = getListOfLens();
		for(int j : i)l=+j;
		return l;
	}
	
	public int[] getListOfLens(){
		if(this.list_of_lens == null){
			this.list_of_lens = new int[sequences.size()];
			int c=0;
			for(String s : sequences.keySet()){
				String seq = sequences.get(s);
				list_of_lens[c] = seq.length();
				c++;
			}
		}
		return this.list_of_lens;
	}
	
	public int getN50(){
		return Tools_Sequences.n50(getListOfLens());
	}
	
	/*
	 * See Tool_Sequences static method for array values
	 */
	public long[] getAllStats(){
		return Tools_Sequences.SequenceStats(getListOfLens());
	}
	
	public int getNoOfSequences(){
		return this.sequences.size();
	}
	
	public int size(){
		return getNoOfSequences();
	}
	
	public String getSequence(int i){
		for(String s : sequences.keySet()){
			if(i==0){
				return sequences.get(s);
			}
			i--;
		}
		return null;
	}
	
	public String getSequence(String key){
		return this.sequences.get(key);
	}
	
	
	public int trimSequences(int tr){
		int trimcount = 0;
		LinkedList<String> toremove = new LinkedList<String>();
		for(String s : sequences.keySet()){
			String seq = sequences.get(s);
			if(seq.length() < tr){
				logger.info("Remove Sequence " + s +" of length " + seq.length());
				toremove.add(s);
				trimcount++;
			}
		}
		for(String s : toremove)remove(s);
		return trimcount;
	}

	public boolean hasSequence(String name){
		return sequences.containsKey(name);
	}
	
	public int removeSequencesWithNs(int Ns){
		int i=0;
		StringBuilder build = new StringBuilder();
		for(int j =0; j < Ns; j++){
			build.append("N");
		}
		Pattern p = Pattern.compile(build.toString());
		LinkedList<String> toremove = new LinkedList<String>();
		for(String s : sequences.keySet()){
			String sequence = sequences.get(s);
			Matcher m = p.matcher(sequence);
			if(m.find()){
				logger.info("Remove Sequence "+s +" due to having to many Ns");
				toremove.add(s);
				i++;
			}
		}
		for(String s : toremove)remove(s);
		return i;
	}
	
	public int removeSequencesWithPercNs(int Perc){
		int i=0;
		int r = 1;
		double perc =((double)Perc)/100.0;
		LinkedList<String> toremove = new LinkedList<String>();
		for(String s : sequences.keySet()){
			String sequence = sequences.get(s).toUpperCase();
			r = (int)((double)sequence.length()*perc);
			
			int index =0;
			while(r-- > -1 && (index = sequence.indexOf("N", index+1))!=-1);
			if(r <1){
				logger.info("Removing "+ s + " due to infringing <"+Perc+"% Ns");
				toremove.add(s);
				i++;
			}
		}
		for(String s : toremove)remove(s);
		return i;
	}
	
	/**
	 * This replaces string1 with string2 within the names of fastas
	 * ie .
	 * s1 = "Contig"
	 * s2 = "ConsensusContig"
	 * 
	 * If the names were "Contig_x [organism=Deroceras reticulatum]"
	 * they would become:
	 * "ConsensusContig_x [organism=Deroceras reticulatum]"
	 * 
	 * 
	 * @param s1 Find String 
	 * @param s2 Replace string
	 * @return the number of names which have been modified (Note:
	 * not the number of modifications (Multiple modifications per string
	 * will happen if s1 occurs more than once and this is not counted)
	 */
	public int replaceNames(String s1, String s2){
		LinkedHashMap<String, String> seqs2 = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> quals2 = new LinkedHashMap<String, String>();
		int i = 0;
		for(String s : sequences.keySet()){
			if(s.indexOf(s1) != -1)i++;
			String two = s.replaceAll(s1, s2);
			seqs2.put(two, sequences.get(s));
			if(qualities.containsValue(s)) quals2.put(two, qualities.get(s));
		}
		if(this.sequences.size() != seqs2.size()){
			logger.error("An error occured, for some reason the" +
					" new hashmap is not the same size as the old one. No changes made.");
		}
		else{
			this.sequences = seqs2;
			this.qualities = quals2;
		}
		return i;
	}
	
	/**
	 * Renames the sequence to s1 + (start + count).
	 * This admittedly assumes the strings are in a
	 * suitable order. As LinkedHashMap was used they
	 * should be in the order they were read in as.
	 * 
	 * 
	 * @param s1
	 * @param start
	 * @return Number of sequences renamed, 
	 * this should be all of them
	 */
	public int renameSeqs(String s1, int start){
		LinkedHashMap<String, String> seqs2 = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> quals2 = new LinkedHashMap<String, String>();
		int i = start;
		for(String s : sequences.keySet()){
			seqs2.put(s1+start, sequences.get(s));
			if(qualities.containsValue(s)) quals2.put(s1+start, qualities.get(s));
			i++;
		}
		if(this.sequences.size() != seqs2.size()){
			logger.error("An error occured, for some reason the" +
					" new hashmap is not the same size as the old one. No changes made.");
		}
		else{
			this.sequences = seqs2;
			this.qualities = quals2;
		}
		return i-start;
	}
	
}
