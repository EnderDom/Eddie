package enderdom.eddie.bio.assembly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.picard.io.IoUtil;
import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SAMFileHeader.SortOrder;
import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.Contig;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.GenericSequenceXT;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.SequenceObjectXT;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.bio.Tools_Fasta;

public class SAMParseWrapper {
	
	public static Logger logger = Logger.getRootLogger();
	private static int sambase = 1;

	public static ArrayList<Contig> parseSAM(File f, File f2) throws FileNotFoundException, 
		UnsupportedTypeException, IOException {
		//Loading SAM file
		logger.info("Loading SAM file...");
		SAMFileReader reader = new SAMFileReader(IoUtil.openFileForReading(f));
		SAMFileHeader header = reader.getFileHeader();
		logger.debug("SAM header acquired: " + header.getTextHeader());
		//Fix the bug which comes from having unrecognised sort order
		try{
			header.getSortOrder();
		}
		catch(Exception e){
			logger.warn("SortOrder is not recognised by samtools.");
			header.setSortOrder(SortOrder.unsorted);
		}
		
		//Get Contigs
		logger.debug("Retrieving groups...");
		List<SAMSequenceRecord> conts = header.getSequenceDictionary().getSequences();
		//Build Contig Map
		logger.debug(conts.size()	+ " contigs retrieved");
		LinkedHashMap<String, Contig> contigs = new LinkedHashMap<String, Contig>(conts.size());

		//Get reference Sequences if available
		logger.debug("Loading reference sequences...");
		SequenceList refs = null;
		if(f2 != null)refs = SequenceListFactory.getSequenceList(f2);
		logger.debug(refs == null ? "References not included": refs.getNoOfSequences() + " Sequences Loaded");
		
		//Populate Contig List with references
		for(SAMSequenceRecord g : conts){
			Contig c = new BasicContig(g.getSequenceName());
			SequenceObjectXT s = null;
			s = refs!=null ? refs.getSequence(g.getSequenceName()).getAsSeqObjXT()
					: new GenericSequenceXT(g.getSequenceName(), "");
			if(s.getQuality() == null)s.setQuality(Tools_Fasta.getEmptyQual(s.getLength()));
			c.setConsensus(s);
			contigs.put(g.getSequenceName(), c);
		}
		
		//Populate Contig List with read data
		logger.debug("Iterating data...");
		Iterator<SAMRecord> iterator = reader.iterator();
		int count =0;
		while(iterator.hasNext()){
			SAMRecord re = iterator.next();
			if(contigs.containsKey(re.getReferenceName())){
				Contig c = contigs.get(re.getReferenceName());
				String[] redseqs = generateAlignedRead(re, c);
				SequenceObjectXT o = new GenericSequenceXT(re.getReadName(), redseqs[0], redseqs[1]);
				c.addSequenceObject(o);
				c.setOffset(o.getIdentifier(), 0,0);//No offset needed as read is aligned
				System.out.print("\r"+(count++));
			}
			else throw new IOException("Failed to parse SAM" +
					" file due to read not matching contig name ("+re.getReferenceName()+")");
		}
		System.out.println();
		logger.info("SAM file parsed.");
		return new ArrayList<Contig>(contigs.values());
	}
	
	public static String[] generateAlignedRead(SAMRecord rec, Contig c){
		StringBuffer b = new StringBuffer();
		StringBuffer q = new StringBuffer();
		int position = 1;
		String read = rec.getReadString();
		String qual = rec.getBaseQualityString();
		if(qual.length() < read.length())qual = null;
		for(AlignmentBlock block : rec.getAlignmentBlocks()){
			while(block.getReferenceStart() > position){
				b.append('-');
				if(qual !=null)q.append('!');
				position++;
			}
			c.addRegion(block.getReadStart()-1, block.getReadStart()+block.getLength()-1, rec.getReadName(), sambase);
			b.append(read.substring(block.getReadStart()-1, block.getReadStart()+block.getLength()-1));
			if(qual!=null)b.append(qual.substring(block.getReadStart()-1, block.getReadStart()+block.getLength()-1));
			position+=block.getLength();
		}
		
		return qual == null ? new String[]{b.toString(), null}
			: new String[]{b.toString(),q.toString()};
	}
	
	
	
}
