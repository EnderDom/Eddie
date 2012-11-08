package enderdom.eddie.bio.fasta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.SequenceList;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.bio.interfaces.UnsupportedTypeException;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.bio.Tools_Fasta;
import enderdom.eddie.tools.bio.Tools_Sequences;


/**
 * @author dominic
 * This ought to be replace with something from 
 * BioJava,  but I don't have time
 */

//JUST REALISE THIS BREAKS ON AMINO ACIDS :((((
public class Fasta implements FastaHandler, SequenceList{

	private LinkedHashMap<String, GenericSequence> sequences;
	private LinkedHashMap<String, String> qualities;
	private boolean fastq;
	Logger logger = Logger.getRootLogger();
	int iteration =0;
	
	public Fasta(){
		sequences = new LinkedHashMap<String, GenericSequence>();
	}
	
	public Fasta(int fastasize){
		sequences = new LinkedHashMap<String, GenericSequence>(fastasize);
	}
	
	public void addSequence(String title, String sequence) {
		sequences.put(title, new GenericSequence(title, sequence));
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
		LinkedHashMap<String, String> newstr = new LinkedHashMap<String, String>();
		for(String key : this.sequences.keySet())newstr.put(key, this.sequences.get(key).getSequence());
		return newstr;
	}

	public void setSequences(LinkedHashMap<String, String> seqs) {
		for(String key : seqs.keySet())this.sequences.put(key, new GenericSequence(key, seqs.get(key)));
	}

	public LinkedHashMap<String, String> getQualities() {
		return qualities;
	}

	public void setQualities(LinkedHashMap<String, String> qualities) {
		this.qualities = qualities;
	}
	
	public String[] getsubFasta(String title){
		return new String[]{sequences.get(title).getSequence(), qualities.get(title)};
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
				ret = new String[]{sequences.get(title).getSequence(), qualities.get(title)};
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
			if(Tools_Fasta.checkFastq(str, sequences.get(str).getSequence(), qualities.get(str))){
				Tools_Fasta.saveFastq(str, sequences.get(str).getSequence(), qualities.get(str), out);
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
			Tools_Fasta.saveFasta(str, sequences.get(str).getSequence(), out);
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
			if(Tools_Fasta.checkFastq(str, sequences.get(str).getSequence(), qualities.get(str))){
				Tools_Fasta.saveFasta(str, sequences.get(str).getSequence(), out);
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
	
	public SequenceObject getSequence(int i){
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
	
	public int trimSequences(int tr){
		int trimcount = 0;
		LinkedList<String> toremove = new LinkedList<String>();
		for(String s : sequences.keySet()){
			GenericSequence seq = sequences.get(s);
			if(seq.getLength() < tr){
				logger.info("Remove Sequence " + s +" of length " + seq.getLength());
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
		LinkedHashMap<String, GenericSequence> seqs2 = new LinkedHashMap<String, GenericSequence>();
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
		LinkedHashMap<String, GenericSequence> seqs2 = new LinkedHashMap<String, GenericSequence>();
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

	public String[] getTruncatedNames() {
		String[] s = new String[this.size()];
		int i =0;
		int index =0;
		for(String name : this.sequences.keySet()){
			if((index=name.indexOf(" ")) != -1){
				s[i] = name.substring(0, index);
			}
			else{
				s[i] = name;
			}
			i++;
		}
		return s;
	}

	public String getSequenceName(int i){
		for(String s : sequences.keySet()){
			if(i==0){
				return s;
			}
			i--;
		}
		return null;
	}
	
	//TODO TODO TODO
	public void mergeFasta(File file, int filetype){
		logger.error("This method has not been added yet");
	}

	public boolean hasNext() {
		return iteration < this.getNoOfSequences();
	}

	public SequenceObject next() {
		iteration++;
		return this.getSequence(iteration-1);
	}

	public void remove() {
		this.remove(this.getSequenceName(iteration));
	}

	public int[] getListOfActualLens() {
		int[] li = new int[this.getNoOfSequences()];
		for(int i =0;i < li.length; i++){
			li[i] = this.getSequence(i).getActualLength();
		}
		return li;
	}
	

	public int[] getListOfLens() {
		int[] li = new int[this.getNoOfSequences()];
		for(int i =0;i < li.length; i++){
			li[i] = this.getSequence(i).getActualLength();
		}
		return li;
	}

	public int getNoOfMolecules() {
		return Tools_Math.sum(this.getListOfActualLens());
	}

	//TODO improve
	public boolean saveFile(File file, int filetype) throws UnsupportedTypeException, IOException {
		if(filetype == SequenceList.FAST_QUAL){
			return this.save2FastaAndQual(new File(file.getPath()+".fasta"), new File(file.getPath()+".qual"));
		}
		else if(filetype == SequenceList.FASTA){
			return this.save2Fasta(file);
		}
		else if(filetype == SequenceList.FASTQ){
			return this.save2Fastq(file);
		}
		else{
			throw new UnsupportedTypeException("Fasta(q) cannot be saved as this filetype");
		}
	}

	public int loadFile(File file, int filetype) throws UnsupportedTypeException, IOException {
		FastaParser parser = new FastaParser(this);
		if(filetype == SequenceList.FAST_QUAL){
			return parser.parseQual(file);
		}
		else if(filetype == SequenceList.FASTA){
			return parser.parseFasta(file);
		}
		else if(filetype == SequenceList.FASTQ){
			return parser.parseFastq(file);
		}
		else{
			throw new UnsupportedTypeException("Cannot load this filetype into fasta object");
		}
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
			String sequence = sequences.get(s).getSequence();
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
			String sequence = sequences.get(s).getSequence();
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
	
}
