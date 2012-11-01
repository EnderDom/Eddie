package enderdom.eddie.bio.assembly;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.Contig;
import enderdom.eddie.bio.interfaces.ContigList;

public class ACERecordList implements ContigList{

	ACERecord[] records;
	int iteration =0;
	Logger logger = Logger.getRootLogger();
	
	public ACERecordList(ACERecord[] list){
		records = list;
	}
	
	public ACERecordList(List<ACERecord> list){
		this.records = List2RecordArray(list);
	}
	
	public ACERecordList(File f) throws IOException{
		ACEFileParser parser = new ACEFileParser(f);
		if(parser.getContigSize() != 0){
			logger.debug("Parser claims file has " + parser.getContigSize()+ " contigs");
			records = new ACERecord[parser.getContigSize()];
			int c=0;
			while(parser.hasNext()){
				System.out.print("\rParsing record "+(c+1)+ " of "+ parser.getContigSize()+"    " );
				records[c]=(ACERecord)parser.next();
				c++;
			}
			System.out.println();
			logger.debug("Parser gave us " + c+ " contigs");
		}
		else{
			LinkedList<ACERecord> recs = new LinkedList<ACERecord>();
			while(parser.hasNext())recs.add((ACERecord)parser.next());
			this.records= List2RecordArray(recs);
		}
	}
	
	
	public static ACERecord[] List2RecordArray(List<ACERecord> list){
		ACERecord[] records = new ACERecord[list.size()];
		for(int i =0; i < list.size();i++)records[i]=list.get(i);
		return records;
	}

	public boolean hasNext() {
		return iteration < this.records.length;
	}

	public Contig next() {
		iteration++;
		return records[iteration-1];
	}

	public void remove() {
		ACERecord[] tmp = new ACERecord[records.length-1];
		int i =0;
		for(; i < iteration; i++)tmp[i]=records[i];
		for(; i < tmp.length; i++)tmp[i]=records[i+1];
		this.records = tmp;
	}

	public Contig getContig(String name) {
		for(int i=0; i < records.length; i++){
			if(records[i].getConsensus().getName().contentEquals(name)){
				return records[i];
			}
		}
		return null;
	}

	public Contig getContig(int i) {
		return records[i];
	}

	public String[] getContigNames() {
		String[] names = new String[records.length];
		for(int i =0; i < names.length ;i++)names[i]=records[i].getConsensus().getName();
		return names;
	}

	public LinkedHashMap<String, String> getConsensusAsMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for(int i =0; i < records.length;i++){
			map.put(records[i].getConsensus().getName(), records[i].getConsensus().getSequence());
		}
		return map;
	}

	public int size() {
		return records.length;
	}
	
	
}
