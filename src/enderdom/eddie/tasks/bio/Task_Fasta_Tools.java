package enderdom.eddie.tasks.bio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_NCBI;

/*
 * This is a fucking mess
 * 
 * This is moving towards just a mess
 */
public class Task_Fasta_Tools extends TaskXTwIO{
	
	
	Fasta fasta;
	private boolean stats;
	private int trim;
	private int trimRowNs;
	private int trimPercNs;
	private boolean convert;
	private String trimAtString;
	private boolean lengths;
	private String quals;
	private boolean dmw;
	
	public Task_Fasta_Tools(){
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		output = FilenameUtils.removeExtension(output);
		try{
			if(quals != null)fasta = (Fasta)SequenceListFactory.getSequenceList(this.input, this.quals);
			else fasta = (Fasta)SequenceListFactory.getSequenceList(this.input);
			subRun();
			if(output != null && !lengths){
				if(!convert)fasta.saveFile(new File(output), fasta.getFileType());
				else{
					if(fasta.getFileType() == BioFileType.FASTA){
						fasta.saveFile(new File(output), BioFileType.FASTQ);
					}
					else if(fasta.getFileType() == BioFileType.FASTQ){
						fasta.saveFile(new File(output), BioFileType.FAST_QUAL);
					}
					else{
						logger.error("Did not distinguish what filtype the input was");
					}
				}
			}
		}
		catch(Exception e){
			logger.error("Whatever you wanted to do failed", e);
			this.setCompleteState(TaskState.ERROR);
			return;
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	//TODO replace with SequenceList methods
	public void subRun(){
		if(trimRowNs != -1){
			logger.info("Removing Sequences with rows>="+trimRowNs+" N...");
			int u = this.fasta.removeSequencesWithNs(trimRowNs);
			logger.info(u+" Sequences Removed");
		}
		if(trimPercNs != -1){
			logger.info("Removing Sequences with N perc >= "+trimPercNs+"...");
			int u = this.fasta.removeSequencesWithPercNs(trimPercNs);
			logger.info(u+" Sequences Removed");
		}
		if(trim != -1){
			logger.info(" Trimming Sequences...");
			int u = this.fasta.trimSequences(this.trim);
			logger.info(u+" Sequences trimmed");
		}
		if(trimAtString != null){
			logger.info(" Trimming Sequences...");
			int u = this.fasta.trimNames(trimAtString);
			logger.info(u+" Sequences renamed");
		}
		if(stats){
			logger.info("Retrieving Statistics...");
			System.out.println("Total No. of Sequences: 	" + this.fasta.getNoOfSequences());
			long[] stats= fasta.getAllStats();
			System.out.println("No. of bp: " + stats[0] + "bp");
			System.out.println("Min Sequence Length: " + stats[1] + "bp");
			System.out.println("Max Sequence Length: " + stats[2] + "bp");
			System.out.println("n50: " + stats[3]);
			System.out.println("n90: " + stats[4]);
			System.out.println("Sequences >500bp " + stats[5]);
			System.out.println("Sequences >1Kb " + stats[6]);
		}
		if(lengths){
			logger.info("Writing lengths to file");
			int[] lens = this.fasta.getListOfLens();
			String n = Tools_System.getNewline();
			try{
				FileWriter fstream = new FileWriter(new File(output), false);
				BufferedWriter out = new BufferedWriter(fstream);
				int j=0;
				for(int i : lens){
					out.write(i+n);
					out.flush();
					j++;
					if(j%1000==0)System.out.print("\r"+j+" of " + lens.length);
				}
				System.out.print("\r"+lens.length+" of " + lens.length);
				System.out.println();
				out.close();
			}
			catch(IOException io){
				logger.error("Failed to output list of lengths to " + output);
			}
		}
		if(dmw){
			logger.debug("Renaming sequences...");
			HashMap<String, String> str = new HashMap<String, String>();
			for(String name : this.fasta.keySet()){
				String specie =Tools_NCBI.getSpecies(name);
				String[] s = specie.split(" ");
				if(s.length > 1)specie = s[0].substring(0,1).toUpperCase()+"."+s[1];
				str.put(name, Tools_NCBI.getNCBIGi(name)+"_" + specie);
			}
			fasta.renameNames(str);
		}
		//OTHER fasta tools
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta file");
		options.addOption(new Option("q", "qual", true, "Optional quality file for convert fastas & qual -> fastq"));
		options.getOption("o").setDescription("Output file or files");
		options.addOption(new Option("trim", true, "Trim each sequences in fasta of length below this value ie -trim 100"));
		options.addOption(new Option("stats", false, "Print Statistics for Fasta/q files"));
		options.addOption(new Option("convert", false, "Convert files to another file type"));
		options.addOption(new Option("trimPercNs", true, "Remove any sequence where the percentage of Ns is greater than this INTEGER value"));
		options.addOption(new Option("trimRowNs", true, "Remove any sequences with a row of Ns greater than this"));
		options.addOption(new Option("lengths", false, "Print a list of lengths to the output"));
		options.addOption(new Option("trimAtString", true, "Trims the fasta name after the first occurance " +
				"of string ie -trimAtString \">Contig\" would change >Contig2121 to >Contig"));
		options.addOption(new Option("s","short", false, "Use Short titles, names are truncated to first space (Needed to match fasta qual)"));
		options.addOption(new Option("dmwName", false, "Use Dominic Wood Naming scheme for fasta with ncbi blast names "));
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		this.trimPercNs = getOption(cmd, "trimPercNs", -1);
		this.trimRowNs = getOption(cmd, "trimRowNs", -1);
		this.quals = getOption(cmd, "q", null);
		this.trim = getOption(cmd, "trim", -1);
		this.trimAtString = getOption(cmd, "trimAtStrin", null);
		this.stats = cmd.hasOption("stats");
		this.convert = cmd.hasOption("conver");
		this.lengths = cmd.hasOption("lengths");
		this.dmw = cmd.hasOption("dmwName");
	}
	
	public Options getOptions(){
		return this.options;
	}

}

