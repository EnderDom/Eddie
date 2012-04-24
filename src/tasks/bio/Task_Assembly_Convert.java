package tasks.bio;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import net.sf.picard.io.IoUtil;
import net.sf.picard.sam.SamFormatConverter;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.Tools_System;

public class Task_Assembly_Convert extends TaskXT{
	
	int conversiontype;
	private static int BAM2SAM = 1;
	Logger logger = Logger.getLogger("Converterz");
	
	//TODO add more data
	public Task_Assembly_Convert(){
		
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("sam2bam"))conversiontype = BAM2SAM;
	}
	
	public void parseOpts(Properties props){
	
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("sam2bam", false, "Convert SAM/BAM to BAM/SAM"));
		
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(input != null && new File(input).isFile() && !new File(output).exists()){
			if(conversiontype==BAM2SAM){
				SamFormatConverter converter = new SamFormatConverter();
				File in = new File(input);
				if(output == null){
					output = new String("out");
					logger.warn("Output was not set");
				}
				File out = new File(output);
				try{
					SAMFileReader reader = new SAMFileReader(IoUtil.openFileForReading(in));
					SAMFileHeader header = reader.getFileHeader();
					try{
						header.getSortOrder();
					}
					catch(Exception e){
						logger.warn("SortOrder is not recognised by picard.");
						logger.warn("4 w3 81t 0f h4x1n9 n0w...");
						header.setSortOrder(SortOrder.unsorted);
					}
			        SAMFileWriter writer = (new SAMFileWriterFactory()).makeSAMOrBAMWriter(header, true, out);
			        for(Iterator<?> iterator = reader.iterator(); iterator.hasNext(); writer.addAlignment((SAMRecord)iterator.next()));
			        reader.close();
			        writer.close();
				}
				catch(Exception e){
					logger.error("Error converting SAM/BAM", e);
				}
				converter.OUTPUT = new File(output);
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
	    setComplete(finished);
	}
}
