package enderdom.eddie.bio.lists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import enderdom.eddie.bio.sequence.BasicSequenceList;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

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
	protected File file;
	public static int namespacemax=10;
	
	public ClustalAlign(File file, BioFileType type) throws UnsupportedTypeException, IOException{
		this.file=file;		
		loadFile(file, type);
	}
	
	public ClustalAlign(InputStream in, BioFileType type) throws UnsupportedTypeException, IOException{
		loadFile(in, type);
	}
	
	public ClustalAlign(){
		sequences = new LinkedHashMap<String, SequenceObject>();
	}
	
	public String[] saveFile(File file, BioFileType filetype) throws IOException,
			UnsupportedTypeException {
		if(filetype == BioFileType.CLUSTAL_ALN){
			this.save(file, 60);
			return new String[]{file.getPath()};
		}
		else{
			throw new IOException("Can't save Clustal as " + filetype.toString());
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
	
	
	public int loadFile(InputStream fis, BioFileType filetype) throws UnsupportedTypeException, IOException {
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
		
		logger.info("Parsed " + (this.sequences.size()-1) + " sequences from clustal file");
		return counter;
	}
	
	/**
	 * Quick & Dirty Clustal parser
	 */
	public int loadFile(File file, BioFileType filetype) throws UnsupportedTypeException, IOException {
		filename = file.getName();
		filepath = file.getPath();
		this.type = filetype;
		FileInputStream fis = new FileInputStream(file);
		return loadFile(fis, filetype);
	}

	public BioFileType getFileType() {
		return BioFileType.CLUSTAL_ALN;
	}

	private void save(File file, int linelen) throws IOException{
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		save2writer(out, linelen, false);
		out.close();
		fstream.close();
	}
	
	public void save2writer(BufferedWriter out, int linelen, boolean html) throws IOException{
		String neline = (html) ?  "<br/>" : Tools_System.getNewline() ;
		if(!html){
			out.write("CLUSTAL 2.1 multiple sequence alignment"+neline+neline+neline);
		}
		else out.write("<p style=\"font-family:'Monospaced','Courier New', 'Courier';font-size=12px;\">");
		int count = 0;
		int maxlength = Tools_Math.getMaxValue(this.getListOfLens());
		
		
		
		for(String name : sequences.keySet()){
			if(sequences.get(name).getLength() < maxlength){
				sequences.get(name).extendRight(maxlength-sequences.get(name).getLength());
			}
			if(name.length() > namespacemax)namespacemax=name.length();
			
		}
		String gap = "      ";
		String towrite = null;
		while(count+linelen < maxlength){
			for(String name : sequences.keySet()){
				if(sequences.get(name).getLength() < maxlength){
					sequences.get(name).extendRight(maxlength-sequences.get(name).getLength());
				}
				//TODO sort out this mess, kinda hacked it as i was writing my thesis :(
				towrite = Tools_String.getStringofLenX(name, namespacemax) +
						gap+sequences.get(name).getSequence().substring(count, count+linelen) + neline;
				if(html)towrite=towrite.replaceAll(" ", "&nbsp;");
				
				out.write(towrite);
			}
            towrite = Tools_String.getStringofLenX("", namespacemax)+gap+getClustAnnnot(sequences, count, count+linelen)+neline;
            if(html)towrite=towrite.replaceAll(" ", "&nbsp;");
            out.write(towrite);
			out.write(neline+neline);
			count = count + linelen;
		}
		for(String name : sequences.keySet()){
			towrite = Tools_String.getStringofLenX(name, namespacemax) + gap+sequences.get(name).getSequence().substring(count, maxlength) + neline;
			if(html)towrite=towrite.replaceAll(" ", "&nbsp;");
			out.write(towrite);
		}
	}

	//TODO complete for . and : symbols
	private String getClustAnnnot(
			LinkedHashMap<String, SequenceObject> sequences, int count, int i) {
		StringBuffer b = new StringBuffer();
		while(count!=i){
			char a = '~';
			for(String s : sequences.keySet()){
				if(a=='~')a=sequences.get(s).getSequence().charAt(count);
				else if(sequences.get(s).getSequence().charAt(count) != a){
					a='#';
					break;
				}
			}
			if(a=='#')b.append(" ");
			else b.append("*");
			count++;
		}
		return b.toString();
	}
	
}
