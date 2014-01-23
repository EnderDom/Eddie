package enderdom.eddie.bio.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.tools.bio.Tools_Fasta;

/**
 * 
 * An attempt to do a nicer version of fasta pull parser
 * 
 * Holds file open, so should be aware of that when using, 
 * don't initialise class until actual use
 * 
 * @author dominic
 *
 */

public class FastaParser2 implements Iterator<SequenceObject>{

	private SequenceObject last;
	private SequenceObject current;
	private BufferedReader reader;
	private Logger logger = Logger.getRootLogger();
	private boolean parseQual;
	private StringBuilder builder;
	private String name;
	private boolean shorttitles;
	private boolean noQualasPhred;
	private boolean fastq;
	
	/**
	 * 
	 * @param shorttitles1 set whether parsing full titles or parse the title up to the first space in the name 
	 * @param noPhred set whether parser should return SequenceObjects with quality files stored as phred ie 1212HHDS+ rather than 60 42 1 12
	 * @param isQual set this to parsing quality rather than sequence (Will return sequenceobject with no sequence rather than no quality)
	 * @param Filepath of fasta/fastq/qual as String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public FastaParser2(String s, boolean shorttitles1, boolean noPhred, boolean isQual) throws FileNotFoundException, IOException{
		this.init(new FileInputStream(new File(s)), shorttitles1, noPhred, isQual);
	}
	
	/**
	 * @param shorttitles1 set whether parsing full titles or parse the title up to the first space in the name 
	 * @param noPhred set whether parser should return SequenceObjects with quality files stored as phred ie 1212HHDS+ rather than 60 42 1 12
	 * @param File of fasta/fastq/qual
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public FastaParser2(File f, boolean shorttitles1, boolean noPhred, boolean isQual) throws FileNotFoundException, IOException{
		this.init(new FileInputStream(f), shorttitles1, noPhred, isQual);
	}
	
	/**
	 * @param shorttitles1 set whether parsing full titles or parse the title up to the first space in the name 
	 * @param noPhred set whether parser should return SequenceObjects with quality files stored as phred ie 1212HHDS+ rather than 60 42 1 12
	 * @param InputStream containing fasta/qual/fastq stream
	 * @throws IOException
	 */
	public FastaParser2(InputStream stream, boolean shorttitles1, boolean noPhred, boolean isQual) throws IOException{
		init(stream, shorttitles1, noPhred, isQual);
	}
	
	//Default Settings
	public FastaParser2(File f) throws IOException {init(new FileInputStream(f), true, false, false);}
	
	public FastaParser2(){}
	
	
	/**
	 * Initialising Method 
	 * 
	 * @param shorttitles1 set whether parsing full titles or parse the title up to the first space in the name 
	 * @param noPhred set whether parser should return SequenceObjects with quality files stored as phred ie 1212HHDS+ rather than 60 42 1 12
	 * @param stream
	 * @throws IOException
	 */
	private void init(InputStream stream, boolean shorttitles1, boolean noPhred, boolean isQual) throws IOException{
		this.shorttitles = shorttitles1;
		this.noQualasPhred = noPhred;
		this.parseQual = isQual;
		builder = new StringBuilder();
		name = new String();
		reader = new BufferedReader(new InputStreamReader(stream));
		parseNext();
	}
	
	/**
	 * @return true if has next SequenceObject else false
	 */
	public boolean hasNext() {
		return current != null;
	}
	
	/**
	 * @return next parsed SequenceObject
	 */
	public SequenceObject next() {
		try {
			parseNext();
		} catch (IOException e) {
			logger.error("Failed to remove sequence as future sequences cannot be parsed", e);
		}
		return last;
	}

	/**
	 * This doesn't 'remove' so to speak, so 
	 * much as simple skips the sequence which
	 * would have been return had next() been called
	 * 
	 */
	public void remove() {
		try {
			parseNext();
		} catch (IOException e) {
			logger.error("Failed to remove sequence as future sequences cannot be parsed", e);
		}
		
	}

	/**
	 * Complications arise due to fastq quality string
	 * potentially containing both + and @ so we have to
	 * wait for length of quality to be equal to the sequence
	 * 
	 * The flip of currentlength to negative is so the quality string
	 * some will increase to count back to 0, as such creating
	 * a simple way only switching from sequence to quality string
	 * parsing when quality is the right length. This will obviously
	 * break if the quality string is a different length than the 
	 * main string
	 * 
	 * Will warn if the quality string is too long compared with the 
	 * sequence. In the event of quality string being to short, this will
	 * inevitably lead to the quality string 'eating' the next sequence anyway
	 * and becoming too long, so both should be covered.
	 * 
	 * @throws IOException
	 */
	private void parseNext() throws IOException{
		String line = null;
		last = current;
		current = null;
		boolean qual = false;
		int currentlength =0;
		if(reader == null)return;
		while ((line = reader.readLine()) != null){
			if(!fastq)if(line.startsWith("@"))fastq=true;
			if(line.startsWith(">") || line.startsWith("@") && currentlength==0){
				if(builder.length() != 0){
					setCurrent(qual);
					setTitle(line);
					qual=false;
					builder.setLength(0);
					break;
				}
				else{
					setTitle(line);
				}
			}
			else if(line.startsWith("+") && fastq && !qual){
				setCurrent(qual);
				qual=true;
				currentlength*=-1;
			}
			else if(qual && currentlength > 0){
				throw new IOException(name + " quality string within fastq has overrun," +
						" the sequence and quality strings are not the same length");
			}
			else if(line.length()!=0){
				builder.append(line);
				if(fastq)currentlength+=line.length();
			}
		}
		if(builder.length() > 0){
			setCurrent(fastq);
			reader.close();
			builder.setLength(0);
			reader = null;
		}
	}
	
	/**
	 * Sub method for parseNext
	 * 
	 * @param fastqQual
	 */
	private void setCurrent(boolean fastqQual){
		if(parseQual && !noQualasPhred && !fastq){
			current = new GenericSequence(name, null, 
					Tools_Fasta.Qual2Fastq(builder.toString()));
		}
		else if(parseQual && noQualasPhred && !fastq){
			current = new GenericSequence(name, null, 
					builder.toString());
		}
		else if(fastqQual){
			current.setQuality(builder.toString());
		}
		else{
			current = new GenericSequence(name, builder.toString(), 
					null);
		}
	}

	/**
	 * Submethod for parseNext
	 * @param line
	 */
	private void setTitle(String line){
		if(shorttitles){
			int i =-1;
			if((i=line.indexOf(" ")) == -1)i=line.length();
			name = line.substring(1,i);
		}
		else {
			name = line.substring(1, line.length());
		}
	}

	/**
	 * 
	 * @return whether parser considers the parsed data to be a fasta or fastq
	 */
	public boolean isFastq() {
		return fastq;
	}
	
}
 