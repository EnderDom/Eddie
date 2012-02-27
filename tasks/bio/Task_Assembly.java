package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import bio.assembly.ACEObject;
import bio.assembly.ACEParser;
import bio.assembly.BasicAssemblyObject;
import bio.assembly.SAMParser;

import tasks.TaskXT;
import tools.Tools_Math;
import tools.Tools_String;
import tools.Tools_System;

public class Task_Assembly extends TaskXT{
	
	boolean coverage;
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
		if(cmd.hasOption("numbcontig"))contig=Tools_String.parseString2Int(cmd.getOptionValue("numbcontig"));
		if(cmd.hasOption("contig"))name=cmd.getOptionValue("contig");
		if(cmd.hasOption("range"))range=Tools_String.parseString2Int(cmd.getOptionValue("range"));
		if(range <1)range=100;
	}
	
	public void parseOpts(Properties props){
	
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("coverage", false, "Coverage analysis Ace File"));
		options.addOption(new Option("range", true, "Range Integer"));
		options.addOption(new Option("numbcontig", true, "Contig Number to analyse"));
		options.addOption(new Option("contig", true, "Contig Name to analyse"));
	}
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(testmode)runTest();
		else{
			if(coverage){
				ACEObject ace = getAce();
				if(contig != -1){
					name = ace.getRefName(contig);
				}
				if(name != null){
					try{
						double[] coverage_ranges = ace.getRangeOfCoverages(name, range);
						System.out.println("Coverage Analysis of " +name);
						System.out.println("#############################");
						System.out.println("#-----------DATA------------#");
						
						for(int i =0; i < coverage_ranges.length; i++)System.out.print(i*100+"-"+((i+1)*100)+ ",");
						System.out.println("\n");
						for(int i =0; i < coverage_ranges.length; i++)System.out.print(coverage_ranges[i]+ ",");
						System.out.println("\n");
						System.out.println("\n");
						System.out.println("#############################");
					}
					catch(Exception e){
						Logger.getRootLogger().error("Out of Cheese Error...", e);
					}
				}
				else{
					Logger.getRootLogger().info("Contig Name is null... not yet finished");
				}
			}
			else{
				Logger.getRootLogger().info("No Analysis set");
			}
		}
		
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public ACEObject getAce(){
		File ace = null;
		ACEObject obj = new ACEObject();
		if(input.endsWith(".ace") || input.endsWith(".ACE")){
			if((ace = getFile(input,2)) != null){
				Logger.getRootLogger().debug("Parsing ACE file");
				ACEParser parser = new ACEParser(obj);
				try {
					parser.parseAce(ace);
					Logger.getRootLogger().debug("Parsing Done");
					
				} 
				catch (Exception e) {
					Logger.getRootLogger().error("Error Parsing ACE file",e);
				}
			}
		}
		return obj;
	}
	
	public void runTest(){
		Logger.getRootLogger().debug("Testing Assembly Task");
		
		File ace = null;
		if(input.endsWith(".ace") || input.endsWith(".ACE")){
			if((ace = getFile(input,2)) != null){
				ACEObject obj = getAce();
				System.out.println("Coverage for " +obj.getRefName(2633));
				System.out.println(Tools_Math.round(obj.getAverageCoverageDepth(obj.getRefName(2633)),3));
			}
		}
		else if (input.endsWith(".sam") || input.endsWith(".SAM")){
			if((ace = getFile(input,2)) != null){
				BasicAssemblyObject obj = new BasicAssemblyObject();
				Logger.getRootLogger().debug("Parsing SAM file");
				SAMParser parser = new SAMParser(obj);
				try {
					parser.parseSAM(ace);
				} 
				catch (Exception e) {
					Logger.getRootLogger().error("Error Parsing SAM file",e);
				}
			}
		}
	}
	
}
