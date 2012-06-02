package tasks.bio;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import databases.manager.DatabaseManager;

import bio.sequence.FourBitSequence;

import tasks.TaskXT;
import tools.Tools_File;
import tools.Tools_String;
import tools.Tools_System;
import tools.bio.Tools_Sequences;

public class Task_BioTools extends TaskXT{
	
	private boolean rc;
	private boolean fuzzy;
	private int index;
	private String input;
	private String output;
	private String contig;
	private FourBitSequence sequence;
	private boolean strip;
	
	public Task_BioTools(){
		setHelpHeader("--This is the Help Message for the General Bio Tools Task--");
		index = -1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		logger.trace("Parsing args for Biotools task");
		if(cmd.hasOption("rc"))rc = true;
		if(cmd.hasOption("i"))input = cmd.getOptionValue("i");
		if(cmd.hasOption("o"))output = cmd.getOptionValue("o");
		if(cmd.hasOption("c"))contig = cmd.getOptionValue("c");
		if(cmd.hasOption("f"))fuzzy = true;
		if(cmd.hasOption("strip"))strip = true;
		if(cmd.hasOption("s"))sequence = new FourBitSequence(cmd.getOptionValue("s"));
		if(cmd.hasOption("xindex")){
			Integer temp = Tools_String.parseString2Int(cmd.getOptionValue("xindex"));
			if(temp != null)index = temp;
			else logger.error("-xindex must be an actual number, stop being so bloody daft");
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("rc","reversecomp", false, "Reverse & Compliment String (To rc all seqs in fasta, see fasta tools)"));
		options.addOption(new Option("s","sequence", true, "Input sequence via commandline ie -s ATCGTGCTACG"));
		options.addOption(new Option("i","input", true, "Use input file as source"));
		options.addOption(new Option("c","contig", true, "Use this sequence, if input set this will be from that, else from database"));
		options.addOption(new Option("f","fuzzy", false, "Try to retrieve with fuzzy naming, if -c is not found"));
		options.addOption(new Option("x","xindex", true, "Index of sequence in fasta to use (0 == is the first)"));
		options.addOption(new Option("output", true, "If you wish to output to file, else output is printed to console"));
		options.addOption(new Option("strip", false, "Remove any -/* from sequence before doing anything"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		if(sequence != null);
		else{
			sequence = Tools_Sequences.getSequenceFromSomewhere(logger, new DatabaseManager(this.ui, this.password), input, contig, index, fuzzy, strip);
			if(sequence == null){
				logger.error("Sequence returned was null, premature terminating");
				return;
			}
		}
		if(rc){
			if(output == null){
				String s = Tools_String.splitintolines(60, sequence.getAsStringRevComp());
				System.out.println(s);
			}
			else{
				if(contig == null)contig = "Sequence1";
				contig = ">"+contig + " RC" + Tools_System.getNewline();
				String s = contig + Tools_String.splitintolines(60, sequence.getAsStringRevComp());
				Tools_File.quickWrite(s, new File(output), false);
			}
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
}


