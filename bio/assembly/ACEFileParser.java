package bio.assembly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;

import tools.Tools_String;

import net.sf.samtools.util.BufferedLineReader;

/**
 * 
 * An eventual replacement for the ACEParser
 * 
 * Similar to SAMTextParser
 * 
 * Also stores the linenumber of each contig, so
 * if a specific contig is stored
 * 
 * Used this as a reference for format specifications http://www.cbcb.umd.edu/research/contig_representation.shtml
 * though somewhat lacking in general :(
 * 
 */

public class ACEFileParser implements Iterator<ACERecord>{

	
	private int contigs;
	private int reads;
	private BufferedLineReader mReader;
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
	
	public ACEFileParser(File file) throws FileNotFoundException{
		this(new FileInputStream(file));
	}
	
	public ACEFileParser(InputStream stream){
        init(stream);
	}
	
	private void init(InputStream stream){
		mReader = new BufferedLineReader(stream);
		currentline=new String();
		while((currentline=mReader.readLine()) != null && !currentline.startsWith("CO ")){
        	if(currentline.startsWith("AS ")){
        		parseLine(AS, currentline);
        	}
        }
        currentsw=CO;
	}
	
	public boolean hasNext() {
		if(currentline == null) return false;
		else return true;
	}
	
	public void remove() {
		this.currentrecord=null;
	}

	public ACERecord next() {
		currentrecord = new ACERecord();
		parseLine(currentsw, currentline);
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
		if(currentline == null){
			currentsw = EOF;
		}
		else{
			currentsw=CO;
		}
		currentrecord.finalise();
		return currentrecord;
	}
	
	public void parseLine(int sw, String line){
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
	
	
	
	public void parseFirstCO(String line){
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
	
	public void parseFirstRD(String line){
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
	
	public void parseAS(String line){
		String[] s = line.split(" ");
		if(s.length >2){
			logger.info("File claims to contain " + s[1] + " contigs made of "+s[2]+" reads");
			Integer i = Tools_String.parseString2Int(s[1]);
			if(i != null)this.contigs = i; 
			i = Tools_String.parseString2Int(s[2]);
			if(i != null)this.reads = i;
			
		}
		else{
			logger.error("AS data line too small, parse error: " +line);
		}
	}
	
	public void parseAF(String line){
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
	public void parseBS(String line){
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
	
	public void parseQA(String line){
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

	public int getContigSize() {
		return contigs;
	}

	public int getReadsSize() {
		return reads;
	}
	
}



