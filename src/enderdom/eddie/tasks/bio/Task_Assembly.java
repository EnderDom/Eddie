package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACEObject;
import enderdom.eddie.bio.assembly.ACEParser;
import enderdom.eddie.bio.assembly.ACERecord;

import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Sequences;

@SuppressWarnings("deprecation")
public class Task_Assembly extends TaskXTwIO{
	
	boolean coverage;
	boolean getfasta;
	boolean stats;
	int range;
	int contig;
	String name;
	
	public Task_Assembly(){
		contig =-1;
		range =100;
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("coverage"))coverage =true;
		if(cmd.hasOption("contig"))name=cmd.getOptionValue("contig");
		if(cmd.hasOption("range"))range=Tools_String.parseString2Int(cmd.getOptionValue("range"));
		if(cmd.hasOption("getfasta"))getfasta=true;
		if(cmd.hasOption("stats"))stats=true;
		if(range <1)range=100;
		if(cmd.hasOption("numbcontig")){
			Integer a = Tools_String.parseString2Int(cmd.getOptionValue("numbcontig"));
			if(a!=null)contig=a;
		}
	}
	
	public void parseOpts(Properties props){
	
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("coverage", false, "Coverage analysis Ace File"));
		options.addOption(new Option("stats", false, "Get Statistics regarding file"));
		options.addOption(new Option("range", true, "Range Integer"));
		options.addOption(new Option("numbcontig", true, "Contig Number to analyse"));
		options.addOption(new Option("contig", true, "Contig Name to analyse"));
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(testmode)runTest();
		else{
			if(stats){
				try {
					ACEFileParser parse = new ACEFileParser(new FileInputStream(this.input));
					int count=0;
					int totalread=0;
					long totalbp = 0;
					int[] lengths = new int[parse.getContigSize()];
					while(parse.hasNext()){
						ACERecord record = parse.next();
						System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
						lengths[count] = record.getConsensus().getActualLength();
						count++;
						totalread+=record.getNoOfReads();
						totalbp+=record.getTotalBpofReads();
					}
					long[] stats = Tools_Sequences.SequenceStats(lengths);
					System.out.println();
					System.out.println("No. of Contigs: " + count);
					System.out.println("Total Contig Length: " + stats[0]+ "bp");
					System.out.println("Min-Max Lengths: " + stats[1] +"-"+stats[2] + "bp");
					System.out.println("n50: " + stats[3]);
					System.out.println("n90: " + stats[4]);
					System.out.println("Contigs >500bp: " + stats[5]);
					System.out.println("Contigs >1Kbp: " + stats[6]);
					System.out.println("Total No. of Reads: " + totalread);
					System.out.println("Total No. of Bp: " + totalbp);
					
				}
				catch (FileNotFoundException e) {
					logger.error("No file called " + this.input,e);
				} catch (IOException e) {
					logger.error("Could not parse " + this.input,e);
				}
			}
			else if(coverage){//TODO upgrade
				logger.debug("Coverage Option Set");
				ACEObject ace = getAce();
				if(contig != -1){
					name = ace.getRefName(contig);
					logger.debug("Contig : " + name + " retrieved");
				}
				if(name != null){
					try{
						double[] coverage_ranges = ace.getRangeOfCoverages(name, range);
						System.out.println("Coverage Analysis of " +name);
						System.out.println("#############################");
						System.out.println("#-----------DATA------------#");
						
						for(int i =0; i < coverage_ranges.length; i++)System.out.print(i*range+"-"+((i+1)*range)+ ",");
						System.out.println("\n");
						for(int i =0; i < coverage_ranges.length; i++)System.out.print(coverage_ranges[i]+ ",");
						System.out.println("\n");
						System.out.println("\n");
						System.out.println("#############################");
					}
					catch(Exception e){
						logger.error("Out of Cheese Error...", e);
					}
				}
				else{
					logger.info("Contig Name is null... not yet finished");
				}
			}
			else{
				logger.info("Not Task set");
			}			
		}
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	

	public ACEObject getAce(){
		File ace = new File(input);
		ACEObject obj = new ACEObject();
		if(ace.exists()){
			if(!input.endsWith(".ace") && !input.endsWith(".ACE")){
				logger.warn("Warning the specified input does not have the standard file tag");
			}
			logger.debug("Parsing ACE file");
			ACEParser parser = new ACEParser(obj);
			try {
				parser.parseAce(ace);
				logger.debug("Parsing Done");
			}
			catch (Exception e) {
				logger.error("Error Parsing ACE file",e);
			}
		}
		else{
			logger.error("Ace file does not exist")	;
		}
		return obj;
	}
	//TODO test padded string	
	public void runTest(){
		logger.debug("Testing Assembly Task");
		
		
	}
	
}

