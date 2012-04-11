package tasks.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import bio.assembly.ACEFileParser;
import bio.assembly.ACERecord;

import tasks.TaskXT;
import tools.Tools_File;
import tools.Tools_System;

public class Task_ChimeraAnalysis extends TaskXT{
	
	private String blastfolders; //PAth containg the blast files
	HashMap<String, String>contig2file;
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))blastfolders=cmd.getOptionValue("b");
	}
	
	public void parseOpts(Properties props){
		logger.trace("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "blastfolder", true, "folder containing individual blast XMLs (MultiBlast not supported yet)"));
		options.getOption("i").setDescription("Input assembly file (only ACE currently)");
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		this.contig2file = new HashMap<String, String>();
		File in = new File(input);
		File blastfolder = new File(blastfolders);
		if(in.isFile() && blastfolder.isDirectory()){
		if(filetype == null)filetype = this.detectFileType(in.getName());
			if(!filetype.equals("ACE")){
				logger.warn("File does not have expected suffix '.ace'");
			}
			try{
				ACEFileParser parse = new ACEFileParser(new FileInputStream(in));
				int count=0;
				while(parse.hasNext()){
					ACERecord record = parse.next();
					this.contig2file.put(record.getContigName(), record.getContigName()+".xml");
					System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
					count++;
				}
				System.out.println();
				logger.info("Mapping Contigs to Blast files... Please wait...");
				this.contig2file= Tools_File.mapFiles(contig2file, blastfolder);
			}
			catch(IOException e){
				logger.error("Failed to parse ACE file properly",e);
			}
		}
		else{
			if(!in.isFile()){
				logger.error("Input specified is not a file");
			}
			if(!blastfolder.isDirectory()){
				logger.error("Blastfolder is not a directory");
			}
			printHelpMessage();
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
}
