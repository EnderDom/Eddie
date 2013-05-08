package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;


import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Converters;

public class Task_Convert extends TaskXTwIO{
	
	int conversiontype;
	private static int BAM2SAM = 1;
	private static int ACE2FNA = 2;
	private static int XML2FASTA = 3;
	//private static int FASTA2FASTQ = 3;
	Logger logger = Logger.getLogger("Converterz");
	String id_tag;
	String seq_tag;
	
	
	public Task_Convert(){
		
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("sam2bam"))conversiontype = BAM2SAM;
		if(cmd.hasOption("ace2fna"))conversiontype = ACE2FNA;
		if(cmd.hasOption("xml2fasta"))conversiontype = XML2FASTA;
		id_tag = getOption(cmd, "name", "id");
		seq_tag = getOption(cmd, "seq", "sequence");
		
	}
	
	public void parseOpts(Properties props){
	
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("sam2bam", false, "Convert SAM/BAM to BAM/SAM"));
		options.addOption(new Option("ace2fna", false, "Converts ACE"));
		options.addOption(new Option("xml2fasta", false, "Grabs 2 tags in xml and makes fasta (-name and -seq)"));
		options.addOption(new Option("name", true, "For sequence name tag in xml"));
		options.addOption(new Option("seq", true, "For sequence tag in xml"));
		//options.addOption(new Option("fasta2fastq", false, "Convert Fasta and Qual file to Fastq"));
		//options.addOption(new Option("q","qual", true, "Quality file if needed"));
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(input ==null || output == null){
			logger.error("Input/Output not set");
			setCompleteState(TaskState.ERROR);
			return;
		}
		File in = new File(input);
		File out = new File(output);
		if(input != null && in.isFile() && (!out.exists() || overwrite)){
			if(conversiontype==BAM2SAM){
				logger.info("Converter Successful: "+Tools_Converters.SAM2BAM(in, out));
			}
			if(conversiontype==ACE2FNA){
				logger.info("Converter Successful: "+Tools_Converters.ACE2FNA(in, out));
			}
			if(conversiontype==XML2FASTA){
				try {
					logger.info("ID tag is " + id_tag + " and " + " Seq tag is " + seq_tag);
					logger.info("Converter Successful: "+Tools_Converters.XML2Fasta(in, out, id_tag, seq_tag));
				} catch (XMLStreamException e) {
					logger.error("Failed to convert xml to fasta!",e);
				} catch (IOException e) {
					logger.error("Failed to convert xml to fasta!",e);
				}
			}
			else{
				logger.warn("No Conversion Set");
			}
		}
		else{
			if(new File(output).exists()) logger.error("Output already exists!"); 
			else logger.error("Input not a file");
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}

}
