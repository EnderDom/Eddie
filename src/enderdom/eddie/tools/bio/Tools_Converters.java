package enderdom.eddie.tools.bio;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.sequence.Contig;

import net.sf.picard.io.IoUtil;
import net.sf.picard.sam.SamFormatConverter;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileHeader.SortOrder;

public class Tools_Converters {
	
	private static Logger logger = Logger.getRootLogger();

	/**
	 * 
	 * @param in Input file, should be SAM OR BAM
	 * @param output will output BAM or SAM
	 * @return true if not errors
	 */
	public static boolean SAM2BAM(File in, File output){
		SamFormatConverter converter = new SamFormatConverter();
		try{
			SAMFileReader reader = new SAMFileReader(IoUtil.openFileForReading(in));
			SAMFileHeader header = reader.getFileHeader();
			try{
				header.getSortOrder();
			}
			catch(Exception e){
				logger.warn("SortOrder is not recognised by picard.");
				header.setSortOrder(SortOrder.unsorted);
			}
	        SAMFileWriter writer = (new SAMFileWriterFactory()).makeSAMOrBAMWriter(header, true, output);
	        for(Iterator<?> iterator = reader.iterator(); iterator.hasNext(); writer.addAlignment((SAMRecord)iterator.next()));
	        reader.close();
	        writer.close();
		}
		catch(Exception e){
			logger.error("Error converting SAM/BAM", e);
			return false;
		}
		converter.OUTPUT = output;
		return true;
	}
	
	/**
	 * 
	 * @param in (ACE file)
	 * @param output (Fasta nucleotide file)
	 * @return true if not errors
	 */
	public static boolean ACE2FNA(File in, File output){
		try{
			ACEFileParser parser = new ACEFileParser(in);
			Contig record = null;
			Fasta fasta = new Fasta();
			while(parser.hasNext()){
				record = parser.next();
				fasta.addSequenceObject(record.getConsensus());
			}
			if(fasta.save2Fasta(output) != null){
				logger.error("File was not successfully saved");
				return false;
			}
			else{
				logger.info("Fasta appeared to save successfully");
				return true;
			}
		}
		catch(Exception e){
			logger.error("Error Saving Fasta", e);
			return false;
		}
	}
	
	//TODO Test
	/**
	 * Haven't got round to testing this...
	 
	 * @param in
	 * @param output
	 * @return true if no errors
	 */
	public static boolean ACE2FAQ(File in, File output){
		try{
			ACEFileParser parser = new ACEFileParser(in);
			Contig record = null;
			Fasta fasta = new Fasta();
			fasta.setFastq(true);
			while(parser.hasNext()){
				record = parser.next();
				fasta.addSequence(record.getConsensus().getIdentifier(), record.getConsensus().getSequence());
				fasta.addQuality(record.getConsensus().getIdentifier(), record.getConsensus().getQuality());
			}
			if(fasta.save2Fastq(output) != null){
				logger.error("File was not successfully saved");
				return false;
			}
			else{
				logger.info("Fastq appeared to save successfully");
				return true;
			}
		}
		catch(Exception e){
			logger.error("Error Saving Fastq", e);
			return false;
		}
	}
	
}
