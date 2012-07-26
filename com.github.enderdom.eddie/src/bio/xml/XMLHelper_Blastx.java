package bio.xml;

import java.io.File;

import org.apache.log4j.Logger;

import databases.bioSQL.psuedoORM.Run;
import databases.manager.DatabaseManager;

import tools.Tools_String;
import tools.Tools_System;

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
public class XMLHelper_Blastx {

	public XML_Blastx blastx;
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
	public int date_range = 14; 
	
	public XMLHelper_Blastx(File file) throws Exception{
		new XML_Blastx(file);
		this.contig_id = -1;
	}
	
	public XMLHelper_Blastx(XML_Blastx blastx){
		this.blastx = blastx;
		this.contig_id = -1;
	}
	
	public XML_Blastx getBlastXML(){
		return this.blastx;
	}

	public String getBlastProgram(){
		return blastx.getBlastTagContents("BlastOutput_program");
	}
	
	public String getBlastVersion(){
		return blastx.getBlastTagContents("BlastOutput_version");
	}
	
	public String getBlastDatabase(){
			return new File(blastx.getBlastTagContents("BlastOutput_db")).getName();
	}
	
	public String getHitAccession(int index) throws Exception{
		return blastx.getHitTagContents("Hit_accession", index);
	}
	
	public double getHspEvalue(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_evalue", hit_num, hsp_num);
		Double d = Tools_String.parseString2Double(s);
		if(d != null)return d;
		else throw new Exception("An error occured attempting to parse the hsp evalue " +s );
	}
	
	public int getHspScore(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_evalue", hit_num, hsp_num);
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
			Integer i = Tools_String.parseString2Int(s);
			if(i != null)arr[j]=i;
			else throw new Exception("Failed to parse tag " + tags[i] + " to integer, tag value: " +s);
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
	
	public void getRun_id(int run_id){
		this.run_id = run_id;
	}
	
	
	public String getParametersAsString(){
		StringBuffer buffer = new StringBuffer();
		int k =0;
		for(String key : blastx.blastcache.keySet()){
			if((k = key.indexOf("Parameters_")) != -1){
				buffer.append('-');
				buffer.append(key.substring(k, key.length()));
				buffer.append(' ');
				buffer.append(blastx.blastcache.get(key));
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
	 * @return true if script ran with 
	 * no errors
	 */
	public boolean upload2BioSQL(DatabaseManager manager, boolean fuzzy, String dbname){
		
		if(run_id == -1){
			Run run = new Run();
			run.setRuntype("blast");
			run.setProgram(this.getBlastProgram());
			run.setVersion(this.getBlastVersion());
			run.setDbname(this.getBlastDatabase());
			run.setParams(this.getParametersAsString());
			run.setDateValue(this.date, Tools_System.SQL_DATE_FORMAT);
			if(run.validate()){
				this.run_id = run.uploadRun(manager);
			}
			else{
				logger.error("Run failed to validate?");
				for(String s : run.getValidationErrors()){
					if(s.length() > 0)logger.error(s);
				}
				return false;
			}
		}
		if(run_id == -1){
			logger.error("Run id was not correctly set");
			return false;
		}
		if(contig_id == -1){
			String nom = blastx.getBlastTagContents("BlastOutput_query-ID");
			contig_id =  manager.getBioSQLXT().getBioEntryId(manager, nom, fuzzy, manager.getEddieDBID());
		}
		if(contig_id == -1){
			logger.warn("Failed to upload to mysql as query-ID was not found in database");
			return false;
		}
		else{
			if(dbname == null){
				logger.warn("Not Database name set, using default: 'genbank'");
				dbname = "genbank";
			}
			try{
				for(int i =1; i < blastx.getNoOfHits(); i++){
					String acc = getHitAccession(i);
					int dbx_ref =  manager.getBioSQL().getDBxRef(manager.getCon(), dbname, acc);
					if(dbx_ref == -1)manager.getBioSQL().addDBxref(manager.getCon(), dbname, acc, 0);
					dbx_ref =  manager.getBioSQL().getDBxRef(manager.getCon(), dbname, acc);
					if(dbx_ref == -1){
						logger.error("Could not upload accession " + acc);
						return false;
					}
					for(int j=1 ; j < blastx.getNoOfHsps(i); j++){
						if(!manager.getBioSQLXT().existsDbxRefId(manager, contig_id, dbx_ref, run_id, j)){
							int[] pos = this.getStartsStopsFrames(i, j);
							manager.getBioSQLXT().setDbxref(manager, contig_id, dbx_ref, run_id, j, this.getHspEvalue(i, j), this.getHspScore(i, j), pos[0], pos[1], pos[2],
									pos[3], pos[4], pos[5]);
						}
					}
				}
				return true;
			}
			catch(Exception e){
				logger.error("Error retrieving data from blast file ", e );
				return false;
			}
		}
	}
}
