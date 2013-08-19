package enderdom.eddie.bio.sequence;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

public interface ContigList extends Iterator<Contig>{
	
	public Contig getContig(String name);
	
	public Contig getContig(int i);
	
	public String[] getContigNames();
	
	public LinkedHashMap<String, String> getConsensusAsMap();
	
	public int getNoOfContigs();
	
	public int getNoOfReads();
	
	public void addContig(Contig c);
	
	public void save(File f, BioFileType t) throws Exception;
	
	public void load(File f, File f1, BioFileType t)throws Exception;
	
	public void load(File f, BioFileType t) throws Exception;
	
}
