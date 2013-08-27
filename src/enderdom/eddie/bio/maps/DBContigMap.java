package enderdom.eddie.bio.maps;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_Array;
import enderdom.eddie.tools.Tools_Math;

import enderdom.eddie.databases.manager.DatabaseManager;

public class DBContigMap {

	/**
	 * Creates A map of read IDs for a Contig
	 * Then links the reads to contigs in another 
	 * assembly. Division1 is the assembly that contig_id
	 * contig_name is tied to through term_id. Division1 should
	 * be a 6 letter string.
	 * 
	 */
	
	private int contig_id =-1;
	private String contig_name;
	private int[] read_ids;
	private int[] contig_ids;
	private int eddiedbid =-1;
	private int[] uniqs;
	private int[] sizes;
	private boolean built;
	private int run_id;
	
	private Logger logger = Logger.getRootLogger();
	
	/**
	 * Default Constructor
	 * 
	 * @param div2 (The 6 letter division id of the assembly you wish to match
	 * this contig to)
	 */
	public DBContigMap(int run_id){
	
	}
	
	/**
	 * Gets Read ids from the database and adds them to this
	 * object as an int array
	 * 
	 * @param manager
	 * @return true if all went well
	 */
	public boolean retrieveReadIDs(DatabaseManager manager){
		int[][] r = manager.getBioSQLXT().getReads(manager, contig_id);
		return r == null ? false : (read_ids = r[0]).length > 0;  
	}
	
	public boolean retrieveContigs(DatabaseManager manager){
		if(this.read_ids == null) {
			logger.warn("You cannot retrieve the contigs, before first getting the read_ids");
			if(!retrieveReadIDs(manager))return false;
		}
		
		this.contig_ids = new int[read_ids.length];
		if(read_ids.length > 0){
			for(int i =0 ; i < read_ids.length ; i++){
				contig_ids[i] = manager.getBioSQLXT().getContigFromRead(manager, read_ids[i], run_id);
			}
			
			/*
			 *  Creating list of uniq values
			 *  then ordering them by size
			 *  where index=0 is id with smallest prevalence
			 *  and index=len-1 is the largest
			 */
			uniqs = Tools_Array.getUniqueValues(contig_ids);
			sizes = new int[uniqs.length];
			for(int i = 0; i < contig_ids.length; i++){
				for(int j =0; j < uniqs.length; j++){
					if(contig_ids[i] == uniqs[j]){
						sizes[j]++;
						break;
					}
				}
			}
			Tools_Array.sortBothByFirst(sizes, uniqs);
			built = true;
			logger.debug("Total of " + uniqs.length + " unique contigs ");
		}
		return true;
	}
	
	/**
	 * 
	 * @param manager
	 * @param contig_name
	 * @return true if all ids > -1
	 * else false if either the EddieDBID or
	 * the contig_id could not be retrieved
	 */
	public boolean retrieveContigId(DatabaseManager manager, String contig_name){
		this.contig_name = contig_name;
		if(eddiedbid < 0){
			eddiedbid = manager.getEddieDBID();
			if(eddiedbid < 0)return false;
		}
		return (this.contig_id=manager.getBioSQL().getBioEntry(manager.getCon(),
				contig_name, null, eddiedbid)) > -1; 
	}
	

	/**
	 * See buildMap(DatabaseManager manager, int contig_id)
	 * @param manager
	 * @param contig_name
	 * @return true if all methods return true
	 */
	public boolean buildMap(DatabaseManager manager, String contig_name){
		if(!retrieveContigId(manager, contig_name)) return false;
		return buildMap(manager, this.contig_id);
	}
	
	/**
	 * Runs the various methods to build
	 * up the map. Will break early an return
	 * false if a stage fails, as downstream stages 
	 * are reliant
	 * 
	 * @param manager
	 * @param contig_name
	 * @return true if all methods return true
	 */
	public boolean buildMap(DatabaseManager manager, int contig_id){
		this.contig_id=contig_id;
		if(!retrieveReadIDs(manager)) return false;
		if(!retrieveContigs(manager)) return false;
		return true;
	}
	
	/**
	 * 
	 * @return the id 
	 * of the top matched contig
	 * this can be -1 if most reads
	 * do not match another contig
	 */
	public int getTopContig(){
		if(built){
			return uniqs[uniqs.length-1];
		}
		else{
			logger.error("Must first build map before you can get top contig!");
			return -1;
		}
	}
	
	/**
	 * 
	 * @return this id of the top matched contig
	 * unless that is -1 (unmatched read) in which case the next
	 * highest contig that is not -1, unless there are none
	 * in which case it will still return -1
	 */
	public int getTopRealContig(){
		int ret = -1;
		for(int i =uniqs.length-1; i > -1; i--){
			ret = uniqs[i];
			if(ret != -1)break;
		}
		return ret;
	}
	
	/**
	 * 
	 * @param fraction
	 * @return id for all contigs which share a fraction 
	 * of reads with this contig which is greater than
	 * param fraction as well as their %
	 */
	public int[][] getContigsAboveThreshold(double fraction){
		if(fraction > 1.0){
			logger.warn("idiots");
			if(fraction <= 100.0)fraction = fraction/100.0;
		}
		double total = Tools_Math.sum(sizes);
		if(total != read_ids.length){
			logger.warn("Total Size "+total + " not equal to reads " + read_ids.length);
		}
		ArrayList<Integer> ints = new ArrayList<Integer>(sizes.length);
		ArrayList<Integer> percs = new ArrayList<Integer>(sizes.length);
		for(int i = uniqs.length-1 ; i  >-1; i--){
			double frac = (double)sizes[i]/total;
			if(frac > fraction){
				ints.add(uniqs[i]);
				percs.add((int)(frac*100));
			}
			else break; //As they've already been ordered, all further will be too small
		}
		return new int[][]{Tools_Array.ListInt2int(ints),Tools_Array.ListInt2int(percs)};
	}

	public int[] getContigIds(){
		return this.contig_ids;
	}
	
	public int[] getReadIds(){
		return this.read_ids;
	}
	
	public int getContigId(){
		return this.contig_id;
	}
	
	public String getContigName(){
		return this.contig_name;
	}
	
	public void setContigName(String s){
		this.contig_name = s;
	}
	
	public int[][] getUniqsAndSizes(){
		return new int[][]{uniqs, sizes};
	}
	
	/**
	 * 
	 * @return whether these two contigs
	 * contain the same number of reads shared
	 * they should, so if this returns false
	 * there is a bug in this method or one of the mapping
	 * methods
	 */
	public boolean validateMap(DBContigMap contig){
		int[][] a = contig.getUniqsAndSizes();
		int sizeOfThisInOther = 0;
		int sizeOfOtherInThis = 0;
		for(int i = 0 ; i < a[0].length; i++){
			if(a[0][i] == this.contig_id){
				sizeOfThisInOther = a[1][i];
			}
		} 
		a = this.getUniqsAndSizes();
		for(int i = 0 ; i < a[0].length; i++){
			if(a[0][i] == contig.getContigId()){
				sizeOfOtherInThis = a[1][i];
			}
		} 
		if(sizeOfThisInOther - sizeOfOtherInThis != 0){
			logger.error("Contig with id " + contig.getContigId() 
					+ " has " + sizeOfOtherInThis + " shared but "
					+ this.getContigId() + " has " + sizeOfThisInOther);
		}
		return sizeOfThisInOther == sizeOfOtherInThis;
	}

	public int getNoOfReads() {
		return this.read_ids.length;
	}
}
