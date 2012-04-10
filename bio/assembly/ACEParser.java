package bio.assembly;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

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
		int linecount = 0;
		//HORRIBLE workaround for multireaderror:
		LinkedHashMap<String, String> hack = new LinkedHashMap<String, String>();
		String current = "";
		
		while((line = reader.readLine()) != null){

			if(line.startsWith("CO")){
				if(consensus.length() > 0){
					handler.setRefConsensus(consensus.toString());
					consensus = new StringBuilder();
					hack = new LinkedHashMap<String, String>();
				}
				String[] bits = line.split(" ");
				if(bits.length > 1){
					handler.setRefName(bits[1]);
				}
				if(bits.length > 5){
					
					Integer x = Tools_String.parseString2Int(bits[2]);
					Integer y = Tools_String.parseString2Int(bits[3]);
					Integer z = Tools_String.parseString2Int(bits[4]);
					if(x != null)handler.setNoOfBases(x);
					if(y != null)handler.setNoOfReads(y);
					if(z != null)handler.setBaseSegments(z);
					handler.setOrientation(bits[5].toCharArray()[0]);
				}
				else{
					Logger.getRootLogger().error("Missing details in Contig/Reference Header (CO)");
				}
				count++;
				swit = 0;
			}
			else if(line.startsWith("CT{")){
				//COMMENT
				swit =3;
			}
			else if(line.startsWith("BQ")){
				if(consensus.length() > 0){
					handler.setRefConsensus(consensus.toString());
					consensus = new StringBuilder();
					hack = new LinkedHashMap<String, String>();
				}
				if(qualbuff.length() > 0){
					handler.setRefConsensusQuality(qualbuff.toString());
					qualbuff = new StringBuilder();
				}
				swit = 1;
			}
			else if(line.startsWith("AF")){
				String[] bits = line.split(" ");
				if(bits.length > 1){
					String tmp = handler.addQNAME(bits[1]);
					hack.put(bits[1], tmp);
					handler.addOrientation(bits[2].toCharArray()[0], tmp);
					handler.addPOS(Tools_String.parseString2Int(bits[3]), tmp);
				}
				swit =3;
			}
			else if(line.startsWith("BS")){
				swit =3;
				//I believe this can be ignored.
			}
			else if(line.startsWith("RD")){
				String[] bits = line.split(" ");
				if(bits.length > 1){
					if(buff.length() > 0){
						handler.addSEQ(buff.toString(), current);
						current = hack.get(bits[1]);
						buff = new StringBuilder("");
					}
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
				if(bits.length > 4){
					handler.addRange(Tools_String.parseString2Int(bits[1]), Tools_String.parseString2Int(bits[2]), current);
					handler.addRangePadded(Tools_String.parseString2Int(bits[3]), Tools_String.parseString2Int(bits[4]), current);
				}
				else{
					Logger.getRootLogger().warn("Length of QA to small");
				}
				swit =3;
			}
			else if(line.startsWith("AS")){
				String[] bits = line.split(" ");
				if(bits.length > 1){
					Integer i = Tools_String.parseString2Int(bits[2]);
					if(i != null)handler.setNoOfReads(i);
				}
				else{
					Logger.getRootLogger().warn("AS too small");
				}
				swit =3;
			}
			else{
				switch(swit){
					case 0 : consensus.append(line);break; //Reading Contig lines
					case 1 : qualbuff.append(line+" "); break; //Reading Quality Lines
					case 2 : buff.append(line);break; //Reading Read Data Lines
					case 3 : break; //Reading Read Data Lines
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
		handler.setRefConsensus(consensus.toString());
		handler.setRefConsensusQuality(qualbuff.toString());
		handler.addSEQ(buff.toString(), current);
		
		System.out.println();
		Logger.getRootLogger().info("Parsed "+ linecount+ " lines into "+ count+ " sequences");
		return count;
	}
		
}
