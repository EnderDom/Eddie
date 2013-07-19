package enderdom.eddie.bio.lists;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.bio.sequence.BasicSequenceList;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.bio.Tools_Bio_File;
import enderdom.eddie.tools.bio.Tools_Fasta;
import enderdom.eddie.tools.bio.Tools_Sequences;


/**
 * @author dominic
 * This ought to be replace with something from 
 * BioJava,  but I don't have time
 */

public class Fasta extends BasicSequenceList implements FastaHandler{

	private boolean fastq;
	int iteration =0;
	
	public Fasta(){
		sequences = new LinkedHashMap<String, SequenceObject>();
	}
	
	public Fasta(int fastasize){
		sequences = new LinkedHashMap<String, SequenceObject>(fastasize);
	}
	
	public void addSequence(String title, String sequence) {
		if(sequences.containsKey(title)){
			SequenceObject o = sequences.get(title);
			o.setSequence(sequence);
			sequences.put(title, o);
			lengthCheck(title);
		}
		else{
			sequences.put(title, new GenericSequence(title, sequence));
		}
	}


	/**
	 * Quality must be fastq
	 * @param title the name of sequence
	 * @param quality the quality string as in fastq 
	 */
	public void addQuality(String title, String quality) {
		if(sequences.containsKey(title)){
			SequenceObject o = sequences.get(title);
			o.setQuality(quality);
			sequences.put(title, o);
			lengthCheck(title);
		}
		else{
			SequenceObject o = new GenericSequence(title);
			o.setQuality(quality);
			sequences.put(title, o);
		}
	}
	
	/**
	 * Checks that sequence length and quality are the same length, 
	 * but doesn't throw an error, just logs an error
	 * 
	 * @param title
	 */
	private void lengthCheck(String title){
		if(sequences.get(title).getQuality()!=null && sequences.get(title).getSequence() != null){
			int l1 =0;int l2 =0;
			if((l1=sequences.get(title).getSequence().length()) != (l2=sequences.get(title).getQuality().length())){
				logger.error("Sequence length "+l1+" and Quality lentgh "+l2+" don't match for " + title);
			}
		}
	}

	public void addAll(String title, String sequence, String quality) {
		this.sequences.put(title, new GenericSequence(title, sequence, quality));
	}

	public void remove(String title){
		sequences.remove(title);
	}
	
	public void setFastq(boolean fastq) {
		this.fastq =fastq;
	}

	public boolean isFastq() {
		return fastq;
	}

	public LinkedHashMap<String, String> getSequences() {
		LinkedHashMap<String, String> newstr = new LinkedHashMap<String, String>();
		for(String key : this.sequences.keySet())newstr.put(key, this.sequences.get(key).getSequence());
		return newstr;
	}

	public SequenceObject getsubFasta(String title){
		return this.sequences.get(title);
	}
	
	public String save2Fastq(File output) throws IOException{
		filename= FilenameUtils.getBaseName(output.getName()) + ".fastq";
		filepath = FilenameUtils.getPath(output.getPath());
		output = new File(FilenameUtils.concat(filepath, filename));

		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		int count =0;
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str).getSequence(), sequences.get(str).getQuality())){
				Tools_Fasta.saveFastq(str, sequences.get(str).getSequence(), sequences.get(str).getQuality(), out);
			}
			else{
				throw new IOException("Fastq failed QC check");
			}
			count++;
		}
		out.close();fstream.close();
		if(count == sequences.keySet().size())return output.getPath();
		else return null;
	}
	
	public String save2Fasta(File output) throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		int count = 0;
		for(String str : sequences.keySet()){
			Tools_Fasta.saveFasta(str, sequences.get(str).getSequence(), out);
			count++;
		}
		out.close();fstream.close();
		logger.info("Fasta Saved Successfully");
		if(count == sequences.keySet().size())return output.getPath();
		else return null;
	}
	
	public String[] save2FastaAndQual(File output, File quality)throws IOException{
		FileWriter fstream = new FileWriter(output);
		BufferedWriter out = new BufferedWriter(fstream);
		FileWriter fstream2 = new FileWriter(quality);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		int count = 0;
		for(String str : sequences.keySet()){
			if(Tools_Fasta.checkFastq(str, sequences.get(str).getSequence(),  sequences.get(str).getQuality())){
				Tools_Fasta.saveFasta(str, sequences.get(str).getSequence(), out);
				Tools_Fasta.saveFasta(str, Tools_Fasta.Fastq2Qual( sequences.get(str).getQuality()), out2);
				count++;
			}
			else{
				System.out.println("The following is the sequence and quality for "+ str + " the mismatch in length has caused this error " );
				System.out.println(sequences.get(str).getSequence() );
				System.out.println(sequences.get(str).getQuality() );
				throw new IOException("Fasta failed QC check");
			}
		}
		out.close();out2.close();fstream.close();fstream2.close();
		if(count == sequences.keySet().size())return new String[]{output.getPath(), quality.getPath()};
		else return null;
	}
	
	public String[] save2FastaAndQual(String name) throws IOException{
		return save2FastaAndQual(new File(name + ".fasta"),  new File(name+".qual"));
	}
		
	public String save2Fasta(String name) throws IOException{
		return (Tools_Bio_File.detectFileType(name) == BioFileType.FASTA) ?
				save2Fasta(new File(name)) : save2Fasta(new File(name + ".fasta"));
	}
	
	public String save2Fastq(String name) throws IOException{
		return (Tools_Bio_File.detectFileType(name) == BioFileType.FASTQ) ?
				save2Fastq(new File(name)) : save2Fastq(new File(name + ".fastq"));
	}
	
	public int getNoOfBps(){
		int l = 0;
		int[] i = getListOfLens();
		for(int j : i)l=+j;
		return l;
	}
	
	/*
	 * See Tool_Sequences static method for array values
	 */
	public long[] getAllStats(){
		return Tools_Sequences.SequenceStats(getListOfActualLens());
	}

	public int size(){
		return getNoOfSequences();
	}

	public int trimSequences(int tr){
		int trimcount = 0;
		LinkedList<String> toremove = new LinkedList<String>();
		for(String s : sequences.keySet()){
			SequenceObject seq = sequences.get(s);
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
		LinkedHashMap<String, SequenceObject> seqs2 = new LinkedHashMap<String, SequenceObject>();
		int i = 0;
		for(String s : sequences.keySet()){
			if(s.contains(s2)){
				String n =s.replace(s1, s2); 
				SequenceObject o = sequences.get(s);
				o.setIdentifier(n);
				seqs2.put(n, o);
				i++;
			}
			else{
				seqs2.put(s, sequences.get(s));
			}
		}
		if(this.sequences.size() != seqs2.size()){
			logger.error("An error occured, for some reason the" +
					" new hashmap is not the same size as the old one. No changes made.");
		}
		else{
			sequences = seqs2;
		}
		return i;
	}
	
	public int trimNames(String s1){
		LinkedHashMap<String, SequenceObject> seqs2 = new LinkedHashMap<String, SequenceObject>();
		int i = 0;
		for(String s : sequences.keySet()){
			if(s.contains(s1)){
				String n =s.substring(0,s.indexOf(s1)); 
				SequenceObject o = sequences.get(s);
				o.setIdentifier(n);
				seqs2.put(n, o);
				i++;
			}
			else{
				seqs2.put(s, sequences.get(s));
			}
		}
		if(this.sequences.size() != seqs2.size()){
			logger.error("An error occured, for some reason the" +
					" new hashmap is not the same size as the old one. No changes made.");
		}
		else{
			sequences = seqs2;
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
		LinkedHashMap<String, SequenceObject> seqs2 = new LinkedHashMap<String, SequenceObject>();
		int i = 0;
		for(String s : sequences.keySet()){
			seqs2.put(s1+(start+i), sequences.get(s));
			i++;
		}
		if(this.sequences.size() != seqs2.size()){
			logger.error("An error occured, for some reason the" +
					" new hashmap is not the same size as the old one. No changes made.");
		}
		else{
			sequences = seqs2;
		}
		return i;
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


	//TODO improve remove redundancy of saveFasta and saveFile
	public String[] saveFile(File file, BioFileType filetype) throws UnsupportedTypeException, IOException {
		switch(filetype){
			case FAST_QUAL:
				return this.save2FastaAndQual(new File(file.getPath()+".fasta"), new File(file.getPath()+".qual"));
			case FASTA:
				return new String[]{this.save2Fasta(file)};
			case FASTQ:
				return new String[]{this.save2Fastq(file)};
			default:
				throw new UnsupportedTypeException("Fasta(q) cannot be saved as this filetype");
		}
	}
	
	public int loadFile(File file, BioFileType filetype) throws UnsupportedTypeException, IOException {
		if(file.isFile()){
			filename = file.getName();
			filepath = file.getPath();
			return loadFile(new FileInputStream(file), filetype);
		}
		else{
			throw new FileNotFoundException("File is not a file");
		}
	}
	
	public int loadFile(InputStream file, BioFileType filetype) throws UnsupportedTypeException, IOException {
			FastaParser parser = new FastaParser(this);
			switch(filetype){
				case QUAL:
					return parser.parseQual(file);
				case FASTA:
					return parser.parseFasta(file, false);
				case FASTQ:
					return parser.parseFastq(file);
				default:
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

	public BioFileType getFileType() {
		if(type!=null) return type;
		else{
			type = BioFileType.UNKNOWN;
			if(this.fastq) return BioFileType.FASTQ;
			else if(this.sequences.size() > 0){
				for(String tag : sequences.keySet()){
					if(sequences.get(tag).getSequence() != null && sequences.get(tag).getQuality() != null){
						type= BioFileType.FAST_QUAL;
					}
					else{
						type = (sequences.get(tag).getQuality()!= null) ? BioFileType.QUAL :BioFileType.FASTA; 
					}
					return type;
				}
			}
		}
		return type;
	}

	
	public void dump(){
		logger.warn("Dumping sequences, this should only be done in the event that a save failed");
		System.out.println("---CUT----");
		System.out.println("          ");
		for(String key : sequences.keySet()){
			System.out.println(">"+key);
			System.out.println(sequences.get(key));
		}
		System.out.println("          ");
		System.out.println("---CUT----");
	}
	
}
