package enderdom.eddie.bio.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;

/**
 * 
 * An eventual replacement for the ACEParser
 * 
 * Similar to SAMTextParser
 * 
 * 
 * Used this as a reference for format specifications http://www.cbcb.umd.edu/research/contig_representation.shtml
 * though somewhat lacking in general :(
 * 
 */

public class ACEFileParser{

	
	private int contigs;
	private int reads;
	private BufferedReader mReader;
	private String currentline;
	private ACERecord currentrecord;
	private static int AS = 0;
	private static int CO = 1;
	private static int CO_ = 2;
	private static int BQ = 3;
	private static int RD = 4;
	private static int RD_ = 5;
	private static int AF = 6;
	private static int BS = 7;
	private static int QA = 8;
	private static int CT = 9;
	private static int DS = 10;
	private static int BREAK = 11;
	private static int EOF = 12;
	private int currentsw;
	Logger logger = Logger.getLogger("ACEFileParser");
	int warn;
	File f;
	
	/**
	 * @param file Ace file input
	 * @throws IOException
	 */
	public ACEFileParser(File file) throws IOException{
		this(new FileInputStream(file));
		f = file;
	}
	
	/**
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public ACEFileParser(InputStream stream) throws IOException{
		init(stream);
	}
	
	private void init(InputStream stream) throws IOException{
		logger.debug("Initialising stream parsing");
		InputStreamReader in = new InputStreamReader(stream, "UTF-8");
		mReader = new BufferedReader(in);
		currentline=new String();
		while((currentline=mReader.readLine()) != null && !currentline.startsWith("CO ")){
        	if(currentline.startsWith("AS ")){
        		parseLine(AS, currentline);
        	}
        }
        currentsw=CO;
	}
	
	/**
	 * Implementing Iterator interface
	 * 
	 * @return true if another record is available
	 * else false
	 */
	public boolean hasNext() {
		if(currentline == null) return false;
		else return true;
	}
	
	/**
	 * set current record to null
	 */
	public void remove() {
		this.currentrecord=null;
	}

	/**
	 * Implements Iterator interface
	 * 
	 * Removes the Current ACERecord held in 
	 * the parser and parses then next Contig
	 * 
	 * @return ACERecord object which holds one
	 * contig from the Assembly file
	 */
	public ACERecord next() {
		currentrecord = new ACERecord();
		parseLine(currentsw, currentline);
		try{
			while((currentline=mReader.readLine()) != null && !currentline.startsWith("CO ")){
				//space or { added else it could look like a sequence (particularly CT) #facepalm
				if(currentline.startsWith("BQ ")){
					/*
					 * All the files I've seen have no quality data on the same line
					 * as the BQ, but I don't trust biologists thus I have this check:
					 */
					currentline = currentline.substring(2, currentline.length());
					if(currentline.trim().length() == 0){
						//IF all good, go ahead and ignore the line for parsing
						currentsw = BREAK;
					}
					else{
						//IF there is data, hope to god it's quality data and parse it
						currentsw = BQ;
					}
					parseLine(currentsw, currentline);
					currentsw = BQ;
				}
				else if(currentline.startsWith("AF ")){
					currentsw = AF;
					parseLine(currentsw, currentline);
				}
				else if(currentline.startsWith("BS ")){
					currentsw = BS;
					parseLine(currentsw, currentline);
				}
				else if(currentline.startsWith("QA ")){
					currentsw = QA;
					parseLine(currentsw, currentline);
				}
				else if(currentline.startsWith("RD ")){ 
					currentsw = RD;
					parseLine(currentsw, currentline);
					currentsw = RD_;
				}
				else if(currentline.startsWith("CT{")){ 
					currentsw = CT;
					parseLine(currentsw, currentline);
				}
				else if(currentline.startsWith("}")){
					currentsw = BREAK;
				}
				else if(currentline.startsWith("DS ")){ 
					currentsw = DS;
					parseLine(currentsw, currentline);
				}
				else if(currentline.length() == 0){
					currentsw = BREAK;
				}
				else{
					parseLine(currentsw, currentline);
				}
			}
		}
		catch(IOException io){
			logger.error("Failed to parse file", io);
		}
		if(currentline == null){
			currentsw = EOF;
		}
		else{
			currentsw=CO;
		}
		currentrecord.finalise();
		return currentrecord;
	}
	
	private void parseLine(int sw, String line){
		switch(sw){
			case 0: parseAS(line);break;//AS
			case 1: parseFirstCO(line);break; //CO
			case 2: this.currentrecord.addCurrentSequence(line);//CO_
			case 3: this.currentrecord.addQuality(line);break;//BQ
			case 4: parseFirstRD(line);break;//RD
			case 5: this.currentrecord.addCurrentSequence(line);break;//RD_
			case 6: parseAF(line);break;//AF
			case 7: parseBS(line);break;//BS
			case 8: parseQA(line);break;//QA
			case 9: break;//CT <-- not yet dealt with
			case 10: break;//DS
			case 11: break;//Break == Do nothing
			case 12: logger.warn("EOF reached, this message should not be reached, which means if you're reading this... err..?");//DO Nothing
			default: logger.error("This message indicates an error occured previously");
		}
	}
	
	
	
	private void parseFirstCO(String line){
		String[] s = line.split(" ");
		if(s.length > 5){
			this.currentrecord.setContigName(s[1]);
			Integer l = Tools_String.parseString2Int(s[2]);
			if(l != null){
				this.currentrecord.setExpectedLength(l);
			}
			else{
				logger.error("Parse Abberation, number after contig name is NaN");
			}
			l = Tools_String.parseString2Int(s[3]);
			if(l!=null)this.currentrecord.setNumberOfReads(l);
			else logger.error("Error parsing number of reads for contig " + line);			
			l = Tools_String.parseString2Int(s[4]);
			if(l!=null)this.currentrecord.setNumberOfRegions(l);
			else logger.error("Error parsing number of BS regions for contig " + line);
//			if(s[5].length() == 1){
//				char c = s[5].charAt(0);
//				//As yet i've not seen any use for this  
//			}
			logger.trace("Parsed contig: " + s[1] + " as having AS: " + s[3] + " & BS: " + l);
		}
		else{
			logger.error("Contig line has less data than expected "+ line);
		}
		currentsw=CO_;
	}
	
	private void parseFirstRD(String line){
		String[] s = line.split(" ");
		if(s.length > 4){
			this.currentrecord.setReadName(s[1]);
			Integer l = Tools_String.parseString2Int(s[2]);
			if(l != null){
				this.currentrecord.setExpectedLength(l);
			}
			else{
				logger.error("Parse Abberation, number after contig name is NaN");
			}
			Integer l2 = Tools_String.parseString2Int(s[3]);
			Integer l3 = Tools_String.parseString2Int(s[4]);
			if(l2 == 0 && l3 == 0){
				
			}
			else{
				if(warn ==0){
					logger.warn("This Parser has no implementation for dealing with RD data that is not 0");
					warn++;
				}
			}
		}
		else{
			logger.error("RD line has less data than expected: "+line);
		}
		currentsw=CO_;
	}
	
	private void parseAS(String line){
		String[] s = line.split(" ");
		if(s.length >2){
			logger.info("File claims to contain " + s[s.length-2] + " contigs made of "+s[s.length-1]+" reads");
			Integer i = Tools_String.parseString2Int(s[s.length-1]);
			if(i != null)this.reads = i; 
			i = Tools_String.parseString2Int(s[s.length-2]);
			if(i != null)this.contigs = i;
			
		}
		else{
			logger.error("AS data line too small, parse error: " +line);
		}
	}
	
	private void parseAF(String line){
		String[] s = line.split(" ");
		if(s.length >3){
			Integer l = Tools_String.parseString2Int(s[3]);
			if(l != null && s[2].length() ==1){
				this.currentrecord.addOffSet(s[1], l, s[2].charAt(0));
			}
			else{
				logger.error("Offset data is borked line: "+line);
			}
		}
		else{
			logger.error("AF data line too small, parse error");
		}
	}
	
	private void parseBS(String line){
		String[] s = line.split(" ");
		if(s.length >3){
			Integer l1 = Tools_String.parseString2Int(s[1]);
			Integer l2 = Tools_String.parseString2Int(s[2]);
			if(l1 != null && l2 !=null){
				this.currentrecord.addRegion(l1, l2, s[3]);
			}
			else{
				logger.error("Offset data is borked line: "+line);
			}
		}
		else{
			logger.error("AF data line too small, parse error");
		}
	}
	
	private void parseQA(String line){
		String[] s = line.split(" ");
		if(s.length >4){
			Integer l1 = Tools_String.parseString2Int(s[1]);
			Integer l2 = Tools_String.parseString2Int(s[2]);
			Integer l3 = Tools_String.parseString2Int(s[3]);
			Integer l4 = Tools_String.parseString2Int(s[4]);
			
			if(l1 != null && l2 !=null && l3 != null && l4 != null){
				this.currentrecord.addQA(l1,l2,l3,l4);
			}
			else{
				logger.error("Offset data is borked line: "+line);
			}
		}
		else{
			logger.error("AF data line too small, parse error");
		}
	}

	/**
	 * 
	 * @return returns the number of contigs 
	 * in the ACE file as stated by the header
	 * of the ACE file. Note, if the ACE file
	 * is malformed this may be different from
	 * what is really there
	 */
	public int getContigSize() {
		return contigs;
	}

	/**
	 * 
	 * @return the number of reads
	 * in the assembly as stated by the File Header
	 */
	public int getReadsSize() {
		return reads;
	}

}



