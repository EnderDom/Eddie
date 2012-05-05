package bio.objects;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import tools.Tools_Array;
import tools.Tools_Math;
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
	public static short TOP_MATCHED =1;
	public static short COLOR_MATCHED = 2;
	public static short UNMATCHED =0;
	private boolean colorinit;
	private Logger logger = Logger.getRootLogger();
	
	
	/*
	 * Used to shift everything up to zero to more easily draw 
	 */
	public void tiltShift(){
		int min = Tools_Math.getMinValue(this.reads_pos[0]);
		if(min < 0){
			min = Math.abs(min);
			for(int i =0; i < this.reads_pos.length; i++){
				this.reads_pos[0][i] +=min; 
			}
		}
	}
	
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
		logger.trace("Read IDs set, array length" + read_ids.length);
		this.read_ids = read_ids;
		this.contigs = new int[read_ids.length];
	}
	                         
	public int[] getReadIDs(){
		return this.read_ids;
	}
	
	public void initColors(){
		logger.debug("Initialising colors for "+ getName());
		if(contigs.length > 0){
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
			Tools_Array.sortBothByFirst(sizes, uniqs);
			for(int i = 0; i< contigs.length ; i++){
				if(contigs[i] == uniqs[uniqs.length-1]){
					colors[i] = TOP_MATCHED;
				}
				else if(contigs[i] == -1){
					colors[i] = UNMATCHED;
				}
				else{
					colors[i] = COLOR_MATCHED;
				}
			}
			colorinit=true;
		}
		else{
			for(int i = 0; i< contigs.length ; i++){
				colors[i] = UNMATCHED;
			}
		}
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
			reads_pos[0][readcounter] = start;
			reads_pos[1][readcounter] = length;
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
		this.tiltShift();
		xt.tiltShift();
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
		if(offset_init < 0) this.offset+=Math.abs(offset_init);
		else xt.offset += Math.abs(offset_init);
	}
	
	public void getBlastData (XML_Blastx blastx) throws Exception{
		logger.debug("Retrieveing data from blastx...");
		int query_len = Tools_String.parseString2Int(blastx.getBlastTagContents("Iteration_query-len"));
		LinkedList<Integer> starts = new LinkedList<Integer>();
		LinkedList<Integer> stops = new LinkedList<Integer>();
		for(int j =1; j < blastx.getNoOfHits()+1; j++){
			for(int l =1; l < blastx.getNoOfHsps(j); l++){//Note +1 need to sort this out at some point>?????
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
	
	public int getContig(int index){
		return this.contigs[index];
	}
	
	public int[] getUniqs(){
		return this.uniqs;
	}
	
	public int[] getSizes(){
		return this.sizes;
	}
}
 