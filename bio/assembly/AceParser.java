package bio.assembly;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//Tool for Parsing Ace Files
public class AceParser {

	public AceHandler handler;

	//TODO STUB :: unfinished implementation
	
	public AceParser(){
		
	}
	
	public AceParser(AceHandler handler){
		this.handler = handler;
	}
	
	/*
	 * Untested
	 */
	public int parseAce(File acefile){
		int count=0;
		try{
			FileInputStream fis = new FileInputStream(acefile);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			StringBuffer consensus = new StringBuffer();
			StringBuffer buff = new StringBuffer();
			int swit =0;
			while((line = reader.readLine()) != null){
				if(line.indexOf("CO") == 0){
					swit = 0;
					if(consensus.length() > 0){
						handler.addConsensus(consensus);
						consensus = new StringBuffer();
					}
					handler.addConsensusName(line.substring(3, line.length()));
					count++;
				}
				else if(line.indexOf("BQ") == 0){
					swit = 1;
				}
				else if(line.indexOf("AF") == 0){
					swit = 2;
				}
				else if(line.indexOf("RD") == 0){
					if(buff.length() > 0){
						handler.addRead(buff);
						buff = new StringBuffer();
					}
					handler.addReadName(line.substring(3, line.length()));
					swit = 3;
				}
				else if(swit ==0){
					consensus = consensus.append(line);
				}
				else if(swit ==3){
					buff = buff.append(line);
				}
			}
		}
		catch(IOException exe){
			exe.printStackTrace();
		}
		return 0;
	}
	
}
