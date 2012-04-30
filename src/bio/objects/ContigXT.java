package bio.objects;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import tools.Tools_Array;
import tools.Tools_String;
import bio.objects.BasicContig;
import bio.sequence.FourBitSequence;
import bio.xml.XML_Blastx;

/* 
 * BasicContig, but holds additional data
 * Currently used for drawing contig images
 */

public class ContigXT extends BasicContig{
	
	private short[] colors;
	private int[][] blasts;
	private int[] read_ids;
	private int offset;
	private int[] contigs;
	private int[] uniqs;
	private int[] sizes;
	public static short TOP_MATCHED = 0;
	public static short COLOR_MATCHED = 1;
	public static short UNMATCHED = 2;
	private boolean colorinit;
	private Logger logger = Logger.getRootLogger();
	
	
	public void setBlasts(int[][] blasts){
		this.blasts = blasts;
	}
	
	public void setOffset(int offset){
		this.offset = offset;
	}
	
	public int getOffset(){
		return this.offset;
	}
	
	public void setReadCount(int readcount){
		this.readcount=readcount;
		this.reads = new String[readcount];
		this.reads_pos = new int[2][readcount];
		this.colors = new short[readcount];
		this.contigs = new int[readcount];
	}
	
	public short[] getColors(){
		return this.colors;
	}
	
	public void setColor(int index, short value){
		this.colors[index]= value;
	}
	
	public void setContig(int index, int id){
		this.contigs[index] = id;
	}
	
	public int[][] getBlasts(){
		return this.blasts;
	}
	
	public void setReadIDs(int[] read_ids){
		this.read_ids = read_ids;
	}
	                         
	public int[] getReadIDs(){
		return this.read_ids;
	}
	
	public void initColors(){
		uniqs = Tools_Array.getUniqueValues(this.contigs);
		sizes = new int[uniqs.length];
		for(int i = 0; i < contigs.length; i++){
			for(int j =0; j < uniqs.length; j++){
				if(contigs[i] == uniqs[j]){
					sizes[j]++;
					break;
				}
			}
		}
		//This needs to be betterified
		Tools_Array.sortBothByFirst(sizes, uniqs);
		for(int i = 0; i< contigs.length ; i++){
			for(int j = uniqs.length-1; j > -1; j--){
				if(contigs[i] == -1){
					colors[i] = UNMATCHED;
				}
				else{
					if(j == uniqs.length-1){
						colors[i] = TOP_MATCHED;
					}
					else{
						colors[i] = COLOR_MATCHED;
						break;
					}
				}
			}
		}
		colorinit=true;
	}
	
	public int getTopContig(){
		if(!colorinit)initColors();
		return uniqs[uniqs.length-1];
	}
	
	public boolean addRead(String name, FourBitSequence sequence, int start, int length, short color){
		if(readcounter < readcount){
			if(name != null){
				this.reads[readcounter] = name;
			}
			if(sequence !=null){
				this.reads_seqs[readcounter] = sequence;
			}
			reads_pos[0][readcount] = start;
			reads_pos[1][readcount] = length;
			colors[readcounter] = color;
			readcounter++;
			return true;
		}
		else{
			return false;
		}
	}
	
	public void overlayContig(ContigXT xt, int top){
		logger.debug("Overlaying Contig");
		
		if(uniqs[uniqs.length-1] != top){
			logger.debug("Recoloring...");
			for(int i =0; i < colors.length; i++){
				if(contigs[i] == top){
					colors[i] = TOP_MATCHED;
				}
				else if(colors[i] == TOP_MATCHED){
					colors[i] = COLOR_MATCHED;
				}
			}
		}
		int[][] reads_pos2 = xt.getReadPositions();
		int[] rids2 = xt.read_ids;
		int dif = 0;
		int c = 0;
		for(int i =0; i < reads_pos[0].length; i++){
			for(int j=0; j < reads_pos2[0].length; j++){
				if(read_ids[i] == rids2[j]){
					dif = reads_pos[0][i]-reads_pos2[0][j];
					c++;
				}
			}
		}
		int offset_init = (int)Math.round((double)dif/(double)c);
		if(offset_init < 0) this.offset=Math.abs(offset_init);
		else xt.offset = Math.abs(offset_init);
	}
	
	public void getBlastData (XML_Blastx blastx) throws Exception{
		int query_len = Tools_String.parseString2Int(blastx.getBlastTagContents("Iteration_query-len"));
		LinkedList<Integer> starts = new LinkedList<Integer>();
		LinkedList<Integer> stops = new LinkedList<Integer>();
		for(int j =0; j < blastx.getNoOfHits(); j++){
			for(int l =0; l < blastx.getNoOfHsps(j); l++){
				Integer frame = Tools_String.parseString2Int(blastx.getHspTagContents("Hsp_hit-frame", j,l));
				Integer start = Tools_String.parseString2Int(blastx.getHspTagContents("Hsp_hit-frame", j,l));
				Integer stop = Tools_String.parseString2Int(blastx.getHspTagContents("Hsp_hit-frame", j,l));
				if(frame != null && start != null && stop != null){
					if(frame < 0){
						start = query_len-start;
						stop = query_len-stop;
						int temp = start;
						start = stop;
						stop =temp;
					}
					starts.add(start);
					stops.add(stop);
				}										
			}
		}
		if(starts.size() != stops.size()) throw new Exception("balls");
		else {
			this.blasts = new int[2][starts.size()];
			for(int i =0;i < blasts[0].length; i++)blasts[0][i]=starts.get(i);
			for(int i =0;i < blasts[1].length; i++)blasts[1][i]=stops.get(i)-starts.get(i);
		}
	}
}
 