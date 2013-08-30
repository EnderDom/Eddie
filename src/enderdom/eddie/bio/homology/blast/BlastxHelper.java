package enderdom.eddie.bio.homology.blast;

import java.math.BigDecimal;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

/**
 * 
 * @author EnderDom
 *
 * A lazy add-ons class for blastx
 * although in theory would work with any
 * blast XML class if I ever were to get around to
 * writing them.
 *
 */
public class BlastxHelper {

	public BlastObject blastx;
	public DatabaseManager manager;
	private int contig_id;
	Logger logger = Logger.getRootLogger();
	private int run_id;
	private String date = "00-00-0000";

	/*
	 * This value specifies the range the run must fall within to
	 * use a run id within this range. ie if the blast was conducted 
	 * within x days of blast already in the database and this 
	 * blast run had the same settings, lump these together,
	 * alternatively -1 (settable by Task)    
	 */
	public int date_range = 21; 
	
	
	public BlastxHelper(BlastObject o){
		this.blastx = o;
	}
	
	public BlastObject getBlastXML(){
		return this.blastx;
	}

	public String getBlastProgram(){
		return blastx.getBlastTagContents("BlastOutput_program");
	}
	
	public String getBlastVersion(){
		return blastx.getBlastTagContents("BlastOutput_version");
	}
	
	public String getBlastDatabase(){
		 return FilenameUtils.getBaseName(blastx.getBlastTagContents("BlastOutput_db"));
	}
	
	public String getHitAccession(int index) throws Exception{
		return blastx.getHitTagContents("Hit_accession", index);
	}
	
	public double getHspEvalue(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_evalue", hit_num, hsp_num);
		return new BigDecimal(s).doubleValue();
	}
	
	public int getHspScore(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_score", hit_num, hsp_num);
		Integer d = Tools_String.parseString2Int(s);
		if(d != null)return d;
		else throw new Exception("An error occured attempting to parse the hsp score " +s );
	}
	
	/**
	 * Order is Hit-Start, Hit-Stop, Hit-Frame, Query-Start, Query-Stop, Query-Frame
	 * 
	 * This is straight from blast xml so will be 1-based 
	 * ie 1bp == first bp == Seq[0]
	 * 
	 * @param hit_num hit number
	 * @param hsp_num hsp number
	 * @return a integer array containing the hit-from, hit-to, hit-frame,
	 * query-from, query-to, query-frame
	 */
	public int[] getStartsStopsFrames(int hit_num, int hsp_num) throws Exception{
		int[] arr = new int[6];
		String[] tags = new String[]{"Hsp_hit-from", "Hsp_hit-to","Hsp_hit-frame",
				"Hsp_query-from", "Hsp_query-to","Hsp_query-frame"};
		for(int j =0; j < tags.length; j++){
			String s = blastx.getHspTagContents(tags[j], hit_num, hsp_num);
			if(s!=null){
				Integer i = Tools_String.parseString2Int(s);
				if(i != null){
					arr[j]=i;
				}
				else throw new Exception("Failed to parse tag " + tags[j] + " to integer, tag value: " +s);
			}
			else{
				if(this.getHitAccession(hit_num).equals("Unknown")){
					logger.warn("Known issue were blast ouputs an Unknown for value, recommended to re-blast this sequence");
					throw new Exception("Failed to parse tag " + tags[j] + " for HIT:"+hit_num+" HSP:" + hsp_num);
				}
				else{
					throw new Exception("Failed to parse tag " + tags[j] + " for HIT:"+hit_num+" HSP:" + hsp_num);
				}
				
			}
		
		}
		return arr;
	}

	public int getContig_id() {
		return contig_id;
	}

	public void setContig_id(int contig_id) {
		this.contig_id = contig_id;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public void setRun_id(int run_id){
		if(run_id != -1){
			this.date_range =-1;
		}
		this.run_id = run_id;
	}
	
	public int getRun_id(){
		return this.run_id;
	}
	
	
	public String getParametersAsString(){
		StringBuffer buffer = new StringBuffer();
		int k =0;
		for(String key : blastx.getKeys()){
			if((k = key.indexOf("Parameters_")) != -1){
				buffer.append('-');
				buffer.append(key.substring(k, key.length()));
				buffer.append(' ');
				buffer.append(blastx.get(key));
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Runs a method to relevant Blast XML data into
	 * the BioSQL database.
	 * @param manager The biosql Default DatabaseManager
	 * 
	 * @param fuzzy In the event the sequence names are similar
	 * but not exactly the same as the blast-query names this will check
	 * for similar naming conventions. This is mainly here for me, as 
	 * CLCBio fasta output is named as ConsensusfromContigX whilst
	 * the ace file has naming Contig_X
	 * 
	 * @param dbname ie genbank, swiss, uniprot, go, kegg, interpro etc...
	 * 
	 * @return -1 if failed, return 0 if uploaded successfully, return >0
	 * if some sequences already uploaded, value represents how many
	 */
	public int[] upload2BioSQL(DatabaseManager manager, boolean fuzzy, String dbname, boolean force){
		int[] values = new int[]{-1,0,0};
		String nom = null;
		if(run_id < 1){ 
			Run run = new Run();
			run.setRuntype("blast");
			run.setProgram(this.getBlastProgram());
			run.setVersion(this.getBlastVersion());
			run.setDbname(dbname);
			run.setParams(this.getParametersAsString());
			run.setDateValue(this.date, Tools_System.SQL_DATE_FORMAT);
			if(run.validate()){
				this.run_id = run.getSimilarRun(manager, this.date_range);
				if(this.run_id > 0){
					logger.trace("Similar Run was found and ID retrieved");
				}
				else{
					logger.trace("No similar run, uploaded as new run");
					this.run_id = run.uploadRun(manager);
					if(this.run_id != -1){
						logger.trace("Run was uploaded and ID retrieved as " + run_id);	
					}
					else{
						logger.error("Failed to retrieve a run id, cannot upload");
						values[0]=-1;
						return values;
					}
				}
			}
			else{
				logger.error("Run failed to validate?");
				for(String s : run.getValidationErrors()){
					if(s.length() > 0)logger.error(s);
				}
				return values;
			}
		}
		if(run_id < 1){
			logger.error("Run id was not correctly set");
			return values;
		}
		if(contig_id < 1){
			nom = blastx.getBlastTagContents("BlastOutput_query-def");
			contig_id =  manager.getBioSQLXT().getBioEntryId(manager, nom, fuzzy, manager.getEddieDBID());
		}
		if(contig_id < 1){
			logger.error("Failed to upload to mysql as query-ID for "+nom+", was not found in database");
			return values;
		}
		else{
			if(dbname == null){
				logger.warn("Not Database name set, using default: 'unknown'");
				dbname = "unknown";
			}
			try{
				//Values set to 0 as no errors
				values[0]=0;
				for(int i =1; i < blastx.getNoOfHits()+1; i++){
					String acc = getHitAccession(i);
					int dbx_ref =  manager.getBioSQL().getDBxRef(manager.getCon(), dbname, acc);
					if(dbx_ref < 1)manager.getBioSQL().addDBxref(manager.getCon(), dbname, acc, 0);
					dbx_ref =  manager.getBioSQL().getDBxRef(manager.getCon(), dbname, acc);
					if(dbx_ref < 1){
						logger.error("Could not upload accession " + acc);
					}
					for(int rank=1 ; rank < blastx.getNoOfHsps(i)+1; rank++){
						if(!manager.getBioSQLXT().existsDbxRefId(manager, contig_id, dbx_ref, run_id, rank, i)){
							int[] pos = this.getStartsStopsFrames(i, rank);
							manager.getBioSQLXT().setBioentry2Dbxref(manager, contig_id, dbx_ref, run_id, this.getHspEvalue(i, rank), this.getHspScore(i, rank), pos[0], pos[1], pos[2],
									pos[3], pos[4], pos[5], i, rank);
							values[0]++;
						}
						else if(force){
							int[] pos = this.getStartsStopsFrames(i, rank);
							manager.getBioSQLXT().updateDbxref(manager, contig_id, dbx_ref, run_id, this.getHspEvalue(i, rank), this.getHspScore(i, rank), pos[0], pos[1], pos[2],
									pos[3], pos[4], pos[5], i, rank);
							values[2]++;
						}
						else{
							values[1]++;
						}
					}
				}
				return values;
			}
			catch(Exception e){
				String s = manager.getBioSQL().getBioEntryNames(manager.getCon(), this.getContig_id())[2];
				if(s == null)s = "contig id: " + this.getContig_id();
				logger.error("Error retrieving data from blast file, "+s, e );
				values[0]=-1;
				return values;
			}
		}
	}
}
