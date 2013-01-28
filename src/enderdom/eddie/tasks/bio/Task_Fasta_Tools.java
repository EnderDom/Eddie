package enderdom.eddie.tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.fasta.FastaParser;
import enderdom.eddie.bio.interfaces.BioFileType;

import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

/*
 * This is a fucking mess
 */
public class Task_Fasta_Tools extends TaskXTwIO{
	
	
	Fasta fasta;
	private boolean stats;
	private int trim;
	private boolean NoOut;
	private int trimRowNs;
	private int trimPercNs;
	private String string1;
	private String string2;
	private String rename;
	private int offset;
	private boolean replace;
	private boolean convert;
	private String[] inputs;
	private String[] quals;
	private boolean shorttitles;
	
	public Task_Fasta_Tools(){
		trimRowNs = -1;
		trimPercNs = -1;
		trim =-1;
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		/*
		 * Convert Fasta&qual to Fastq
		 */
		if(input == null || output == null){
			logger.error("No Input/output");
			return;
		}
		output = FilenameUtils.removeExtension(output);
		if(this.inputs == null || this.inputs.length  == 0){
			logger.error("No input files included");
			return;
		}
		
		if(this.detectFileType(inputs[0]) == BioFileType.FASTA){
			logger.info("Detected as FASTA");
			fasta = new Fasta();
			fasta.setFastq(false);
			FastaParser parser = new FastaParser(fasta);
			if(shorttitles)parser.setShorttitles(true);
			try {
				for(int i =0;i < inputs.length; i++){
					logger.info("Parsing: " + FilenameUtils.getBaseName(inputs[i]));
					parser.parseFasta(new File(inputs[i]));
					if(quals.length > 0) parser.parseQual(new File(quals[i]));
				}
				Logger.getRootLogger().debug("Fasta Parsed, saving...");
				subRun();
				if(!NoOut){
					if(!convert){ 
						if(quals.length > 0)fasta.save2FastaAndQual(output);
						else fasta.save2Fasta(new File(output+".fasta"));
					}
					else fasta.save2Fastq(new File(output+".fastq"));
				}
			}
			catch (Exception e) {
				Logger.getRootLogger().error("Error parsing Fasta file", e);
			}
		}
		/*
		 * Convert Fastq to Fasta And Qual
		 */
		else if(this.detectFileType(inputs[0]) == BioFileType.FASTQ){
			logger.info("Detected as FASTQ");
			fasta = new Fasta();
			fasta.setFastq(true);
			FastaParser parser = new FastaParser(fasta);
			if(shorttitles)parser.setShorttitles(true);
			try {
				for(int i =0;i < inputs.length; i++){
					logger.info("Parsing: " + FilenameUtils.getBaseName(inputs[i]));
					parser.parseFasta(new File(inputs[i]));
				}
				Logger.getRootLogger().debug("Fastq Parsed, saving...");
				subRun();
				if(!NoOut){
					if(convert) fasta.save2FastaAndQual(output);
					else{ 
						fasta.save2Fastq(output);
					}
				}
			}
			catch (Exception e) {
				logger.error("Error parsing Fastq file", e);
			}
		}
		else{
			logger.warn("No support for files with unknown file extensions as yet." +
					" Please rename files to .fasta/.fna/.fastq/.qual");
		}

		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
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
		if(replace){
			logger.info(" Renaming Sequences...");
			int u = this.fasta.replaceNames(string1,string2);
			logger.info(u+" Sequences renamed");
		}
		if(rename != null){
			if(replace)logger.warn("replace and rename should not be set together.");
			logger.info(" Renaming Sequences...");
			int u = this.fasta.renameSeqs(rename, offset);
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
		
		//OTHER fasta tools
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta files");
		options.addOption(new Option("q", "qual", true, "Optional quality file for convert fastas & qual -> fastq"));
		options.getOption("o").setDescription("Output file or files");
		options.addOption(new Option("trim", true, "Trim Sequences Using below this value ie -trim 100"));
		options.addOption(new Option("stats", false, "Print Statistics for Fasta/q files"));
		options.addOption(new Option("convert", false, "Convert files to another file type"));
		options.addOption(new Option("trimPercNs", true, "Remove any sequence where the percentage of Ns is greater than this INTEGER value"));
		options.addOption(new Option("trimRowNs", true, "Remove any sequences with a row of Ns greater than this"));
		options.addOption(new Option("rename", true, "Renames string with counter ie -rename \"Contig\" would rename Contig1, Contig2 etc"));
		options.addOption(new Option("offset", true, "if rename is set, this offsets the counter, so -rename Contig -offset 50 would rename Contig50, Contig51"));
		options.addOption(new Option("replace", true, "Replace a with b in fasta names use " +
				">< between find and replace, ie -replace \"Contig_><Contigous File\" would " +
				"replace fasta names with >Contig_1 to >Contigous File1"));
		options.addOption(new Option("s","short", false, "Use Short titles, names are truncated to first space (Needed to match fasta qual)"));
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("replace")){
			string1 = new String();
			string2 = new String();
			String s = cmd.getOptionValue("replace");
			if(s.indexOf("><")!=-1 && s.length() >2){
				String[] s2 = s.split("><");
				string1 = string1 + s2[0];
				if(s.length() > 1){
					string2 = string2+ s2[1];
				}
				replace = true;
			}
			else logger.warn("replace syntax requires separator >< between find and replace strings");
		}
		if(cmd.hasOption("trimPercNs")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("trimPercNs"));
			if(i != null){
				this.trimPercNs = i;
			}
			else{
				logger.warn("TrimPercNs, but is not a number");
			}
		}
		if(cmd.hasOption("trimRowNs")){
			Integer i =Tools_String.parseString2Int(cmd.getOptionValue("trimRowNs"));
			if(i != null){
				this.trimRowNs = i;
			}
			else{
				logger.warn("TrimRowNs, but is not a number");
			}
			
		}
		if(cmd.hasOption("offset")){
			Integer i =Tools_String.parseString2Int(cmd.getOptionValue("offset"));
			if(i != null){
				this.offset = i;
			}
			else{
				logger.warn("Offset set, but is not a number");
			}
			
		}
		if(cmd.hasOption("rename")){
			rename = cmd.getOptionValue("rename");
		} 
		if(cmd.hasOption("q")){
			quals = cmd.getOptionValues("q");
		} 
		if(cmd.hasOption("trim")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("trim"));
			if(i != null){
				this.trim = i;
			}
			else{
				logger.warn("Trim set, but is not a number");
			}
		}
		if(cmd.hasOption("stats")){
			stats=true;
		}
		if(cmd.hasOption("convert")){
			this.convert = true;
		}
		if(cmd.hasOption("s")){
			this.shorttitles = true;
		}
		if(cmd.hasOption("i")){
			this.inputs = cmd.getOptionValues("i"); 
		}
		if(this.output == null){
			this.NoOut = true;
		}
		else if(this.output.length() == 0){
			this.NoOut = true;
		}
	}
	
	public Options getOptions(){
		return this.options;
	}

}

