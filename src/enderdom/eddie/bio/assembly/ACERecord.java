package enderdom.eddie.bio.assembly;

import java.util.ArrayList;

import enderdom.eddie.bio.sequence.BasicRegion;
import enderdom.eddie.bio.sequence.GenericSequenceXT;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.tools.bio.Tools_Fasta;


/**
 * 
 * @author Dominic Matthew Wood
 * This class basically holds the data for 1 contig
 * parsed from an .ace. It has grown a little akward, but
 * hopefully should become a bit of a black box.
 * 
 * 
 * NOTE::: ALL NUCLEOTIDE INDEXES HELD AS 0-BASED
 * ALL NUCLEOTIDE INDEXES RETURNED AS 0-BASED!!!!!!!!!!!!!
 * 
 * 
 * This currently stores sequence data as GenericSequence
 * But this should be relatively trivial to change. You could store
 * as Strings and anywhere which requires return GenericSequence
 * have a new Construcion there.
 * 
 */
public class ACERecord extends BasicContig{

	private StringBuilder sequencebuffer;
	private StringBuilder contigbuffer;
	private StringBuilder qualitybuffer;
	private boolean finalised;
	private String currentread;
	
	/**
	 * Constructor
	 */
	public ACERecord(){
		super();
		sequencebuffer = new StringBuilder();
		qualitybuffer = new StringBuilder();
		contigbuffer = new StringBuilder();
		regions = new ArrayList<BasicRegion>();
	}
	
	/** 
	 * @see java.lang.Object#clone()
	 * @return ACERecord
	 */
	public ACERecord clone() throws CloneNotSupportedException{
		if(!isFinalised())finalise();
		ACERecord newRecord = (ACERecord)super.clone();
		return newRecord;
	}
	
	/**
	 * 
	 * Adds line to the current read
	 * sequence. This will be pushed to the read
	 * seqs array when a new read is being parsed
	 * or finalised is called. 
	 * @param line
	 * 
	 */
	public void addCurrentSequence(String line){
		sequencebuffer.append(line.replaceAll("-", "\\*"));
	}
	
	public void addContigSequence(String line){
		contigbuffer.append(line.replaceAll("-", "\\*"));
	}
	
	/**
	 * See addCurrentSequence
	 * @param line
	 */
	public void addQuality(String line){
		qualitybuffer.append(line.replaceAll(" +", " "));//Added space, as the line break usually replaces space in quality strings
	}
	
	
	/**
	 * Adds a read name to the contig, 
	 * this triggers the previous read to be finalised 
	 * @param readname
	 */
	public void setReadName(String readname){
		if(currentread!=null){
			if(sequences.containsKey(currentread)){
				sequences.get(currentread).setSequence(sequencebuffer.toString());
			}
			else{
				sequences.put(currentread, new GenericSequenceXT(currentread, sequencebuffer.toString(), position));
				position++;
			}
		}
		sequencebuffer.setLength(0);
		currentread=readname;
	}

	
	/**
	 * 
	 * @return returns boolean if finalised
	 */
	public boolean isFinalised(){
		return this.finalised;
	}
	
	private void setFinalised(boolean c){
		this.finalised = c;
	}
	
	/**
	 * Finalise the contig, any buffers will
	 * be put in order, last read will be put into array
	 * etc.
	 */
	public void finalise(){
		String quality = isNoQual2fastq()? Tools_Fasta.Qual2Fastq(this.qualitybuffer.toString()) : this.qualitybuffer.toString();
		if(sequences.get(contigname)==null){
			sequences.put(contigname, new GenericSequenceXT(this.contigname, this.contigbuffer.toString(), 
					quality,0));
		}
		else{
			sequences.get(contigname).setSequence(this.contigbuffer.toString());
			sequences.get(contigname).setQuality(quality);
		}
		setReadName(currentread);
		this.sequencebuffer.setLength(0);
		this.contigbuffer.setLength(0);
		this.qualitybuffer.setLength(0);
		setFinalised(true);
	}

	/**
	 * 
	 * @param i
	 * @return read at i as GenericSequence
	 */
	public SequenceObject getRead(int i){
		for(String key : sequences.keySet()){
			if(sequences.get(key).getPositionInList()-1==i)return sequences.get(key); 
		}
		return null;
	}
	
	/**
	 * 
	 * @param name
	 * @return sequence for read named 'name',
	 * else returns null if the read doesn't exist
	 */
	public SequenceObject getRead(String name){
		return sequences.get(name);
	}
	
	/**
	 * 
	 * @param name
	 * @return the position in the contig that read
	 * is at, can use this index for method returning string
	 * using index
	 */
	public int getReadIndex(String name){
		return sequences.get(name).getPositionInList()-1;
	}

	
}

