package bio.assembly;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import tools.Tools_String;

/*
 * Tool for Parsing Ace Files
 * 
 * Used this as a reference for format specifications http://www.cbcb.umd.edu/research/contig_representation.shtml
 */
public class ACEParser {

	public ACEHandler handler;

	public ACEParser(){
		
	}
	
	public ACEParser(ACEHandler handler){
		this.handler = handler;
	}
	
	/*
	 *TODO TEST THIS METHOD!!!
	 */
	public int parseAce(File acefile) throws IOException{
		if(handler == null)throw new IOException("No Handler set for this parser");
		int count=0;
		int multi=0;
		FileInputStream fis = new FileInputStream(acefile);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		String line = "";
		StringBuilder consensus = new StringBuilder();
		StringBuilder buff = new StringBuilder();
		StringBuilder qualbuff = new StringBuilder();
		int swit =0;
		String readlate = new String("");
		int linecount = 0;
		while((line = reader.readLine()) != null){
			
			if(line.startsWith("CO")){
				swit = 0;
				if(consensus.length() > 0){
					handler.setRefConsensus(consensus.toString());
					handler.setRefLength(consensus.length());
					consensus = new StringBuilder();
				}
				String[] bits = line.split(" ");
				if(bits.length > 1){
					handler.setRefName(bits[1]);
				}
				if(bits.length > 5){
					handler.setNoOfBases(Tools_String.parseString2Int(bits[2]));
					handler.setNoOfReads(Tools_String.parseString2Int(bits[3]));
					handler.setBaseSegments(Tools_String.parseString2Int(bits[4]));
					handler.setOrientation(bits[5].toCharArray()[0]);
				}
				else{
					Logger.getRootLogger().error("Missing details in Contig/Reference Header (CO)");
				}
				count++;
			}
			else if(line.startsWith("BQ")){
				if(qualbuff.length() > 0){
					handler.setRefConsensusQuality(qualbuff.toString());
					qualbuff = new StringBuilder();
				}
				swit = 1;
			}
			else if(line.startsWith("AF")){
				String[] bits = line.split(" ");
				if(bits.length > 1){
					handler.addQNAME(bits[1]);
					handler.addOrientation(bits[2].toCharArray()[0], bits[1]);
					handler.addPOS(Tools_String.parseString2Int(bits[3]), bits[1]);
				}
			}
			else if(line.startsWith("BS")){
				//I believe this can be ignored.
			}
			else if(line.startsWith("RD")){
				if(buff.length() > 0){
					handler.addSEQ(buff.toString(), readlate);
					buff = new StringBuilder("");
				}
				String[] bits = line.split(" ");
				if(bits.length > 1){
					readlate = bits[1];
					/*
					 * Gone ahead and ignore the rest of the RD data
					 *  as it can be calculated or got elsewhere
					 */
				}
				else{
					throw new IOException("Parse Read Data from ACE error");	
				}
				swit = 2;
			}
			else if(line.startsWith("QA")){
				String[] bits = line.split(" ");
				if(bits.length > 5){
					handler.addRange(Tools_String.parseString2Int(bits[2]), Tools_String.parseString2Int(bits[3]), readlate);
					handler.addRangePadded(Tools_String.parseString2Int(bits[4]), Tools_String.parseString2Int(bits[5]), readlate);
				}
			}
			else{
				switch(swit){
					case 0 : consensus.append(line);break; //Reading Contig lines
					case 1 : qualbuff.append(line); break; //Reading Quality Lines
					case 2 : buff.append(line);break; //Reading Read Data Lines
					default: break; //Do Nothing
				}
			}
			if(multi==1000){
				multi=0;
				System.out.print("\rParsing Line: "+linecount);
			}
			multi++;
			linecount++;
		}
		System.out.println();
		Logger.getRootLogger().info("Parsed "+ linecount+ " lines into "+ count+ " sequences");
		return count;
	}
		
}
