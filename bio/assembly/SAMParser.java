package bio.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import tools.Tools_String;
/*
 * SAM specs got from http://samtools.sourceforge.net/SAM1.pdf
 * 
 */
public class SAMParser {

	public SAMHandler handler;

	/*
	 * Note this file format uses the retarded 1 base system aka bioindexing system
	 * The first base of a sequence's index is 1 not 0
	 */
	
	public SAMParser(){
		
	}
	
	public SAMParser(SAMHandler handler){
		this.handler = handler;
	}
	
	public int parseSAM(File samfile) throws IOException{
		if(handler == null)throw new IOException("No Handler set for this parser");
		int count=0;
		int multi=0;
		FileInputStream fis = new FileInputStream(samfile);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		String line = "";
		int  linecount=0;
		boolean warn1 = false;//Just so don't fill the screen full of warnings
		boolean warn2 = false; 
		while((line = reader.readLine()) != null){
			
			if(line.startsWith("@")){
				/*
				 * Header Line
				 * 
				 * */
				if(line.startsWith("@HD")){
					String[] bits = line.split("	");
					boolean format = false;
					for(int i =0; i < bits.length; i++){
						if(bits[i].startsWith("VN:")){
							handler.setFormatVersion(bits[i].substring(3));
							format = true;
						}
						else if(bits[i].startsWith("SO:")){
							String sort = bits[i].substring(3);
							int sorttype = 0;
							for(int j =0; j< SAMHandler.sorttypes.length; j++){
								if( SAMHandler.sorttypes[i].equalsIgnoreCase(sort))sorttype =i;
							}
							handler.setSortType(sorttype);
						}
					}
					if(!format && !warn1)Logger.getRootLogger().warn("SAM File is breaking format conventions, no format version set." +
							" This is none critical so no error thrown but may indicate issues with this file.");warn1=true;
				}
				/*
				 * Reference sequence dictionary
				 * 
				 * */
				else if(line.startsWith("@SQ")){
					String[] bits = line.split("	");
					boolean format = false;
					boolean format2 = false;
					for(int i =0; i < bits.length; i++){
						if(bits[i].startsWith("SN:")){
							handler.setRefName(bits[i].substring(3));
							format = true;
						}
						if(bits[i].startsWith("LN:")){
							int l =Tools_String.parseString2Int(bits[i].substring(3));
							handler.setRefLength(l);
							if(l > -1)	format2 = true; //If parseString2Int fails this will fail
						}
						if(bits[i].startsWith("AS:")){
							handler.setGenomeAssemblyID(bits[i].substring(3));
						}
						if(bits[i].startsWith("M5:")){
							handler.setSequenceMD5(bits[i].substring(3));
						}
						if(bits[i].startsWith("SP:")){
							handler.setSpecies(bits[i].substring(3));
						}
						if(bits[i].startsWith("UR:")){
							handler.setURI(bits[i].substring(3));
						}
					}
					if(!format){
						throw new IOException("SAM File is breaking format conventions, Reference sequence name (SN:) missing on @SQ line. @ line "+linecount);
					}
					if(!format2){
						throw new IOException("SAM File is breaking format conventions, Reference sequence length(LN:)  missing on @SQ line. @ line"+linecount);
					}
					count++;
				}
				else if(line.startsWith("@RG")){
					//TODO Support this part of the file
				}
				else if(line.startsWith("@PG")){
					String[] bits = line.split("	");
					boolean format = false;
					for(int i =0; i < bits.length; i++){
						if(bits[i].startsWith("ID:")){
							handler.setProgramID(bits[i].substring(3));
							format = true;
						}
						if(bits[i].startsWith("PN:")){
							handler.setProgramName(bits[i].substring(3));
						}
						if(bits[i].startsWith("CL:")){
							handler.setCommandLine(bits[i].substring(3));
						}
						if(bits[i].startsWith("PP:")){
							handler.setPGIDChain(bits[i].substring(3));
						}
						if(bits[i].startsWith("VN:")){
							handler.setProgramVersion(bits[i].substring(3));
						}
					}
					if(!format && !warn2)Logger.getRootLogger().warn("SAM File is breaking format conventions, no assembler ID set." +
							" This is none critical so no error thrown but may indicate issues with this file. @ line ");warn2=true;
				}
			}
			else{
				String[] bits = line.split("	");
				/*
				 * The mandatory number of bits is 11, with additional pieces of information appended to this
				 *  */
				if(bits.length > 10){
					parseReadLine(bits, handler);
				}
				else{
					throw new IOException("SAM file type not supported. Too few variables in String");
				}
			}
			if(multi==1000){
				multi=0;
				System.out.print("\rParsing Line: "+linecount);
			}
			multi++;
			linecount++;
		}
		System.out.println("Warning: May not be complete buffers need to be purged");
		Logger.getRootLogger().info("Parsed "+ linecount+ " lines into "+ count+ " sequences");
		return count;
	}

	
	public void parseReadLine(String[] bits, SAMHandler handler){
		
		/*
		 * Default Fields to be parsed
		 */
		handler.addQNAME(bits[0]);
		handler.addFLAG(Tools_String.parseString2Int(bits[1]), bits[0]);
		handler.addRNAME(bits[2], bits[0]);
		handler.addPOS(Tools_String.parseString2Int(bits[3]), bits[0]);
		handler.addMAPQ(Tools_String.parseString2Int(bits[4]), bits[0]);
		handler.addCIGAR(bits[5], bits[0]);
		handler.addRNEXT(bits[6], bits[0]);
		handler.addPNEXT(Tools_String.parseString2Int(bits[7]), bits[0]);
		handler.addTLEN(Tools_String.parseString2Int(bits[8]), bits[0]);
		handler.addSEQ(bits[9], bits[0]);
		handler.addQUAL(bits[10], bits[0]);
		/*
		 *Additional fields  
		 */
		if(bits.length > 10){
			for(int i =11; i < bits.length; i++){
				//TODO
			}
		}
	}
	
	
	
}
