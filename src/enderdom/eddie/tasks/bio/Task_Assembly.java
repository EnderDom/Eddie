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

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_Array;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Sequences;

@SuppressWarnings("deprecation")
public class Task_Assembly extends TaskXTwIO{
	
	private boolean coverage;
	private boolean stats;
	private int range;
	private int contig;
	private String name;
	private int filter;
	
	public Task_Assembly(){
		filter = 0;
		contig =-1;
		range =100;
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("coverage"))coverage =true;
		if(cmd.hasOption("contig"))name=cmd.getOptionValue("contig");
		if(cmd.hasOption("range"))range=Tools_String.parseString2Int(cmd.getOptionValue("range"));
		if(cmd.hasOption("stats"))stats=true;
		if(range <1)range=100;
		if(cmd.hasOption("numbcontig")){
			Integer a = Tools_String.parseString2Int(cmd.getOptionValue("numbcontig"));
			if(a!=null)contig=a;
		}
		if(cmd.hasOption("statlenfilter")){
			Integer a = Tools_String.parseString2Int(cmd.getOptionValue("statlenfilter"));
			if(a!=null)filter=a;
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
		options.addOption(new Option("statlenfilter", true, "Filter out contigs smaller than arg bp in length"));
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
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
						ACERecord record = (ACERecord) parse.next();
						System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
						lengths[count] = record.getConsensus().getActualLength();
						if(lengths[count] < filter){
							lengths[count]=-1;
						}
						else{
							totalread+=record.getNoOfSequences();
							totalbp+=record.getQuickMonomers();
						}
						count++;
					}
					lengths = Tools_Array.IntArrayTrimAll(lengths, -1);
					System.out.println();
					System.out.println();
					System.out.println(count-lengths.length+" contigs removed " +
							"as they fell below the "+filter+"bp length");
					
					long[] stats = Tools_Sequences.SequenceStats(lengths);
					System.out.println();
					System.out.println("No. of Contigs: " + lengths.length);
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
					setCompleteState(TaskState.ERROR);
					return;
				} catch (IOException e) {
					logger.error("Could not parse " + this.input,e);
					setCompleteState(TaskState.ERROR);
					return;
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
						setCompleteState(TaskState.ERROR);
						return;
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
	    setCompleteState(TaskState.FINISHED);
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

