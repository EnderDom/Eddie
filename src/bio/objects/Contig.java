package bio.objects;

import bio.sequence.FourBitSequence;

public class Contig {

	private String name;
	private String[] reads;
	private FourBitSequence[] reads_seqs;
	private int[][] reads_pos;
	private short[] colors;
	private int readcount;
	private int readcounter;
	
	/*
	 * Bit of a rough and ready object created
	 * for the contig comparison, may be able 
	 * to intergrate into something already done, 
	 * like Jalview or something, probably pipe dreams though
	 */
	
	public Contig(){
	}
	
	public Contig(String name){
		this.name = name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
		
	public void setReadCount(int readcount){
		setReadCount(readcount, true, true, true);
	}
	
	public void setReadCount(int readcount, boolean init_reads, boolean init_reads_seqs, boolean init_reads_pos){
		this.readcount=readcount;
		if(init_reads){
			this.reads = new String[readcount];
		}
		if(init_reads_seqs){
			this.reads_seqs = new FourBitSequence[readcount];
		}
		if(init_reads_pos){
			this.reads_pos = new int[2][readcount];
		}
	}
	
	public int getReadCount(){
		return this.readcount;
	}
	
	public int[][] getReadPositions(){
		return this.reads_pos;
	}
	
	public short[] getColors(){
		return this.colors;
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
	
}
