package enderdom.eddie.bio.interfaces;

import java.util.Iterator;
import java.util.LinkedHashMap;

public interface ContigList extends Iterator<Contig>{
	
	public static int ACE = 0;
	public static int SAM = 1;
	public static int BAM = 2; 
	
	public Contig getContig(String name);
	
	public Contig getContig(int i);
	
	public String[] getContigNames();
	
	public LinkedHashMap<String, String> getConsensusAsMap();
	
	public int size();
	
	
	
}
