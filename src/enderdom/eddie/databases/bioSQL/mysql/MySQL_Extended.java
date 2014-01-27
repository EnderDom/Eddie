package enderdom.eddie.databases.bioSQL.mysql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_Array;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_Contig;
import enderdom.eddie.tools.bio.Tools_NCBI;
import enderdom.eddie.bio.assembly.BasicContig;
import enderdom.eddie.bio.sequence.Contig;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.GenericSequenceXT;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.SequenceObjectXT;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.bioSQL.psuedoORM.BasicBioSequence;
import enderdom.eddie.databases.bioSQL.psuedoORM.BioSequence;
import enderdom.eddie.databases.bioSQL.psuedoORM.Bioentry;
import enderdom.eddie.databases.bioSQL.psuedoORM.Dbxref;
import enderdom.eddie.databases.bioSQL.psuedoORM.Dbxref_Bioentry_Link;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.bioSQL.psuedoORM.Taxonomy;
import enderdom.eddie.databases.manager.DatabaseManager;

/**
 * 
 * @author Dominic M. Wood
 *
 * Methods to sordid to go into BioSQL
 *
 */
public class MySQL_Extended implements BioSQLExtended{

	private Logger logger = Logger.getRootLogger();
	private ResultSet set;
	
	private int depthcount;
	private int depthid;
	//Taxon info stuff
	private int layerdepth;
	private int valuesize;
	
	
	/******************************************************************/
	/* 
	 * NEW TABLES HERE 
	 */ 
	 /******************************************************************/
	
	/*
	 * Table for compatibility with previous versions of db
	 * 
	 * Yes I know storing versioning as a varchar is a bit of
	 * a wtf.
	 *
	 */
	public boolean addLegacyVersionTable(DatabaseManager manager, String version, String dbversion) throws Exception {
		String info_table = "CREATE TABLE IF NOT EXISTS info ("+
		  "Numb INT(11) NOT NULL auto_increment,"+
		  "DerocerasVersion VARCHAR(30),"+
		  "DatabaseVersion VARCHAR(30),"+
		  "LastRevision DATE,"+
		  "PRIMARY KEY (Numb)) "+MySQL_BioSQL.innodb+"=INNODB;";
		String insert = new String("INSERT INTO info (Numb, DerocerasVersion, DatabaseVersion, LastRevision) VALUES (1, 'v"+version+"', 'v"+dbversion+"', '"+Tools_System.getDateNow("yyyy-MM-dd")+"')"+
				" ON DUPLICATE KEY UPDATE DerocerasVersion='v"+version+"', DatabaseVersion='v"+dbversion+"', LastRevision='"+Tools_System.getDateNow("yyyy-MM-dd")+"' ;");
		try{
			Statement st = manager.getCon().createStatement();
			st.executeUpdate(info_table);
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create info table string ", se);
			return false;
		}
		
	}
	

	public boolean addRunTable(DatabaseManager manager) {
		//Should be run after bioentry_dbxref mods
		logger.debug("Creating "+runtable+" table...");
		String table = "CREATE TABLE IF NOT EXISTS "+runtable+" (" +
				"run_id INT(10) UNSIGNED NOT NULL auto_increment, " +
				"run_date date NOT NULL, " +
				"runtype VARCHAR(20) BINARY NOT NULL, " +
				"parent_id INT(10) UNSIGNED, " +
			  	"program VARCHAR(40) BINARY NOT NULL, " +
			  	"version VARCHAR(40) BINARY, " +
			  	"dbname VARCHAR(40) BINARY, " +
			  	"source VARCHAR(80) BINARY, " +
			  	"params TEXT, " +
			  	"comment TEXT, " +
			 	"PRIMARY KEY (run_id)" +
			 	") "+MySQL_BioSQL.innodb+"=INNODB;";
		try{
			Statement st = manager.getCon().createStatement();
			st.executeUpdate(table);
			//Note, this will fail if addBioentryDbxrefsCols() has not been called previously
			String key1 = "ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKbioentry_dbxref_run "+
			"FOREIGN KEY (run_id) REFERENCES "+runtable+"(run_id) ON DELETE CASCADE;";
			st.executeUpdate(key1);
			key1 = "ALTER TABLE "+BioSQLExtended.runtable+" ADD CONSTRAINT FKrun_parent FOREIGN KEY (parent_id) REFERENCES run(run_id) ON DELETE CASCADE";
			st.executeUpdate(key1);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create "+runtable+" table", se);
			return false;
		}
	}
	
	/**
	 * Yet another extra table, this is to link different sequences
	 * to different source tissues, upload a sequencing run to 
	 * run database and thus link this to the bioentry, allowing 
	 * you to block grab data from one sequencing run
	 */
	public boolean addRunBioentryTable(DatabaseManager manager){
		String table = 	"CREATE TABLE bioentry_run ( "+
						"bioentry_id        INT(10) UNSIGNED NOT NULL,"+
						"run_id          INT(10) UNSIGNED NOT NULL,"+
						"rank  		   SMALLINT,"+
						"PRIMARY KEY (bioentry_id,run_id)"+
						") "+MySQL_BioSQL.innodb+"=INNODB;";
		String key1 = 	"ALTER TABLE bioentry_run ADD CONSTRAINT FKrun_id_biorun "+
		       			"FOREIGN KEY (run_id) REFERENCES run(run_id) "+
						"ON DELETE CASCADE;";
		String key2 =	"ALTER TABLE bioentry_run ADD CONSTRAINT FKbioentry_id_biorun "+
		       			"FOREIGN KEY (bioentry_id) REFERENCES bioentry(bioentry_id) "+
						"ON DELETE CASCADE;"; 
		String key3 =   "ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKdbxref_bioentry_run " +
				"FOREIGN KEY (run_id) REFERENCES run(run_id) ON DELETE CASCADE;";
		try{
			Statement st = manager.getCon().createStatement();
			logger.debug("Building bioentry_run table....");
			st.executeUpdate(table);
			st.executeUpdate(key1);
			st.executeUpdate(key2);
			st.executeUpdate(key3);
			st.close();
			return true;
		}
		catch(SQLException e){
			logger.error("Failed to create bioentry_run table", e);
			return false;
		}
	}
	
	/**
	 * This could be done by adding the full dbxref
	 * as a bioentry, but I really don't want half the 
	 * ncbi database in here when I want even use the data
	 */
	public boolean addDbxTaxons(DatabaseManager manager){
		String alters[] = new String[]{
				"ALTER TABLE dbxref ADD COLUMN (ncbi_taxon_id INT(10) UNSIGNED);",
				"ALTER TABLE dbxref ADD 'description' TEXT NOT NULL AFTER 'ncbi_taxon_id'",
				"CREATE INDEX dbxref_tax  ON dbxref(ncbi_taxon_id);"
		};
		try{
			Statement st = manager.getCon().createStatement();
			for(String s: alters)st.executeUpdate(s);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to alter bioentry_dbxref table", se);
			return false;
		}
	}

	public boolean addAssemblyTable(DatabaseManager manager){
		logger.debug("Creating assembly table...");
		String table = "CREATE TABLE IF NOT EXISTS assembly (" +
			"contig_bioentry_id INT(10) UNSIGNED NOT NULL,"+
			"read_bioentry_id INT(10) UNSIGNED NOT NULL,"+
			"read_version SMALLINT,"+
			"run_id INT(10) UNSIGNED NOT NULL,"+
			"trimmed TINYINT,"+
			"range_start INT(10),"+
			"range_end INT(10),"+
			"offset INT(10),"+
			"UNIQUE (contig_bioentry_id,read_bioentry_id,run_id)"+
			")"+MySQL_BioSQL.innodb+"=INNODB;";
		try{
			Statement st = manager.getCon().createStatement();
			logger.debug("Building assembly table....");
			st.executeUpdate(table);
			String key1 = "ALTER TABLE assembly ADD CONSTRAINT FKcontig_bioentry_id "+
					"FOREIGN KEY (contig_bioentry_id) REFERENCES bioentry(bioentry_id) ON DELETE CASCADE;";
			logger.debug("Adding bioentry foreign key for contig");
			st.executeUpdate(key1);
			key1 = "ALTER TABLE assembly ADD CONSTRAINT FKread_bioentry_id "+
					"FOREIGN KEY (read_bioentry_id) REFERENCES bioentry(bioentry_id) ON DELETE CASCADE;";
			logger.debug("Adding bioentry foreign key for read");
			st.executeUpdate(key1);
			key1 = "ALTER TABLE assembly ADD CONSTRAINT FKrun_id "+
			"FOREIGN KEY (run_id) REFERENCES run(run_id) ON DELETE CASCADE;";
			logger.debug("Adding bioentry foreign key for run id");
			st.executeUpdate(key1);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create assembly table", se);
			return false;
		}
	}
	

	public boolean addBioentryDbxrefCols(DatabaseManager manager) {
		String alters[] = new String[]{
				"ALTER TABLE bioentry_dbxref ADD COLUMN (hit_no INT(6) UNSIGNED);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (run_id INT(10) UNSIGNED NOT NULL);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (evalue DOUBLE PRECISION);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (score MEDIUMINT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_startpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_endpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_frame TINYINT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_startpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_endpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_frame TINYINT);",
				"CREATE INDEX bioentry_dbxref_evalue ON bioentry_dbxref(evalue);",
				"ALTER TABLE bioentry_dbxref DROP PRIMARY KEY, ADD PRIMARY KEY (bioentry_id,dbxref_id,rank,hit_no,run_id);",
		};
		try{
			Statement st = manager.getCon().createStatement();
			for(String s: alters)st.executeUpdate(s);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to alter bioentry_dbxref table", se);
			return false;
		}
	}

	
	/* ***************************************************************
	 * 
	 * METHOD EXTENSIONS HERE 
	 * 
	 ******************************************************************/

	
	public String[][] getUniqueStringFields(DatabaseManager manager, String[] fields, String table){
		String[][] ret = new String[fields.length][0];
		try{
			Statement st = manager.getCon().createStatement();
			for(int i =0; i < fields.length; i++){
				LinkedList<String> results = new LinkedList<String>();
				String sql = "SELECT DISTINCT("+fields[i]+") FROM " + table;
				set = st.executeQuery(sql);
				while(set.next()){
					results.add(set.getString(fields[i]));
				}
				if(results.size() > ret[i].length){
					String[][] str = new String[fields.length][results.size()];
					for(int j = 0; j < i; j++){
						for(int k =0; k < ret[i].length;k++){
							str[j][k] = ret[j][k];
						}
					}
					ret = str;
				}
				for(int j =0;j < results.size(); j++){
					ret[i][j]=results.get(j);
				}
			}
			
			st.close();
		}
		catch(SQLException sq){
			logger.error("Failed to run getUniqueFields query");
		}
		
		return ret;
	}
	
	public double getDatabaseVersion(DatabaseManager manager) {
		String g = "SELECT DatabaseVersion FROM info WHERE NUMB=1";
		try{
			//JUST shoved this here as getDatabaseVersion is always called
			//when database used
			MySQL_BioSQL.useEngine(manager.getCon());
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(g);
			String r ="";
			while(set.next()){
				r = set.getString("DatabaseVersion");
			}
			set.close();
			st.close();
			Double b = null;
			if(r.startsWith("v")){
				b = Tools_String.parseString2Double(r.substring(1));
			}
			else{
				b = Tools_String.parseString2Double(r);
			}
			if(b == null){
				return -1;
			}
			else{
				return b;
			}
		}
		catch(SQLException sq){
			logger.error("Failed to retrieve database index, this may be because database isn't created yet.", sq);
			return -1;
		}
	}
	
	public boolean addEddie2Database(DatabaseManager manager) {
		String insert = new String("INSERT INTO biodatabase (name, authority, description) VALUES ('"+programname+"', '"+authority+"', '"+description+"')");
		try{
			Statement st = manager.getCon().createStatement();
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create biodatabase table", se);
			return false;
		}
	}
	
	public int getEddieFromDatabase(DatabaseManager manager){
		String insert = new String("SELECT biodatabase_id FROM biodatabase WHERE name='"+programname+"';");
		int db =-1;
		try{
			Statement st = manager.getCon().createStatement();
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				db = set.getInt("biodatabase_id");
			}
			set.close();
			st.close();
		}
		catch(SQLException se){
			logger.error("Failed to get Eddie ID from database", se);
		}
		return db;
	}


	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager manager, int run_id){
		HashMap<String, String> names = new HashMap<String, String>();
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT identifier, name FROM bioentry INNER JOIN assembly ON bioentry.bioentry_id=assembly.contig_bioentry_id WHERE run_id="+run_id);
			while(set.next()){
				names.put(set.getString("name"),set.getString("identifier"));
			}
			set.close();
			st.close();
		}
		catch(SQLException sq){
			logger.error("Failure to retrieve contigname and identifier data", sq);
		}
		return names;
	}
	
	//Really could do with ORM but w/e
	public int[][] getReads(DatabaseManager manager, int bioentry_id){
		String sql = new String("SELECT read_bioentry_id, run_id, range_start, range_end FROM assembly WHERE contig_bioentry_id=?");
		LinkedList<Integer> values = new LinkedList<Integer>();
		try{
			PreparedStatement readFromContigGET = manager.getCon().prepareStatement(sql);
			readFromContigGET.setInt(1,bioentry_id);
			set = readFromContigGET.executeQuery();
			while(set.next()){
				for(int i =1; i < 5 ;i++)values.add(set.getInt(i));
			}
			int[][] ret = new int[4][values.size()/4];
			for(int i =0; i < values.size();i++)ret[i%4][i/4] = values.get(i);
			set.close();
			readFromContigGET.close();
			return ret;
		}
		catch(SQLException sq){
			logger.error("Failed to get reads using contig id", sq);
			return null;
		}		
	}
	
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, int run_id){
		String sql = new String("SELECT contig_bioentry_id FROM assembly WHERE read_bioentry_id=? AND run_id=?");
		try{
			PreparedStatement contigFromReadGET = manager.getCon().prepareStatement(sql);
			contigFromReadGET.setInt(1,bioentry_id);
			contigFromReadGET.setInt(2,run_id);
			set = contigFromReadGET.executeQuery();
			int ret = 0;
			while(set.next()){
				ret= set.getInt(1);
				break;
			}
			set.close();
			contigFromReadGET.close();
			return ret;
		}
		catch(SQLException sq){
			logger.error("Failed to get reads using contig id", sq);
			return -1;
		}		
	}
	
	public int getBioEntryId(DatabaseManager manager, String name, boolean fuzzy, int biodatabase_id, int runid){
		int entry =-1;
		entry = manager.getBioSQL().getBioEntry(manager.getCon(), name, name, biodatabase_id, runid);
		if(entry == -1){
			entry = manager.getBioSQL().getBioEntrywName(manager.getCon(), name, runid);
		}
		//This could lead to unexpected results, probably 
		//should be removed if to be used by anyone other than me
		if(entry == -1 && fuzzy){
			logger.debug("Could not get id with "+name);
			String[] s = Tools_Contig.stripContig(name);
			if(s == null)return entry;
			else{
				for(String sa : s){
					entry = manager.getBioSQL().getBioEntrywName(manager.getCon(), sa, runid);
					if(entry != -1){
						logger.info("Retrieve data based on name " + sa);
						break;
					}
					else{
						logger.debug("Could not get id with "+sa);
					}
				}
			}
		}
		else{
			logger.trace("Entry retrieved without fuzziness required");
		}
		if(entry==0)return -1;
		return entry;
	}
	
	public Bioentry getBioentry(Connection con, int bioentry_id){
		String sql = "SELECT bioentry_id, biodatabase_id," +
				"taxon_id, name, accession, identifier," +
				" division, description, version FROM bioentry " +
				"WHERE bioentry_id="+bioentry_id;
		Bioentry e = null;
		try{
			Statement st = con.createStatement();
			set = st.executeQuery(sql);
			while(set.next()){
				e = new Bioentry(set.getInt("bioentry_id"), set.getInt("biodatabase_id"),
						set.getInt("taxon_id"), set.getString("name"), 
						set.getString("accession"),	set.getString("identifier"),
						set.getString("division"), set.getString("description"), 
						set.getInt("version"));
			}
			set.close();
			st.close();
		}
		catch(SQLException sq){
			logger.error("Failed to retrieve the bioentry with bioentry_id: " + bioentry_id);
		}
		return e;
	}
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int run_id, int rank, int hit_no){
		
		String sql = "SELECT COUNT(1) AS bool FROM bioentry_dbxref WHERE bioentry_id=? AND dbxref_id=? AND run_id=? AND hit_no=? AND rank=?";
		try {
			PreparedStatement dbxrefEXIST = manager.getCon().prepareStatement(sql);
			dbxrefEXIST.setInt(1, bioentry_id);
			dbxrefEXIST.setInt(2, dbxref_id);
			dbxrefEXIST.setInt(3, run_id);
			dbxrefEXIST.setInt(4, hit_no);
			dbxrefEXIST.setInt(5, rank);
			set = dbxrefEXIST.executeQuery();
			Integer i =-1;
			while(set.next()){
				i = set.getInt(1);
				break;
			}
			set.close();
			dbxrefEXIST.close();
			return i > 0;
		} 
		catch (SQLException e) {
			logger.error("Failed to get information for bioentry_dbxref entry with sql "+sql, e);
		}
		return false;
	}
	
	

	public boolean mapRead2Contig(DatabaseManager manager, int contig_id, int read_id, int read_version, int runid, int start, int stop, int offset, boolean trimmed){
		
		String sql = "INSERT INTO assembly (contig_bioentry_id, read_bioentry_id, read_version, run_id, trimmed, range_start, range_end, offset)" +
				" VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE trimmed=?,range_start=?, range_end=?";
		try {
			PreparedStatement assemblySET = manager.getCon().prepareStatement(sql);
			assemblySET.setInt(1, contig_id);
			assemblySET.setInt(2, read_id);
			assemblySET.setInt(3, read_version);
			assemblySET.setInt(4, runid);
			if(trimmed){ 
				assemblySET.setInt(5, 1);
				assemblySET.setInt(8, 1);
			}
			else{
				assemblySET.setInt(5, 0);
				assemblySET.setInt(8, 0);
			}
			assemblySET.setInt(6, start);
			assemblySET.setInt(7, stop);
			assemblySET.setInt(9, start);
			assemblySET.setInt(10, stop);
			assemblySET.setInt(11, offset);
			assemblySET.execute();
			assemblySET.close();
			return addRunBioentry(manager, contig_id, runid);
		} 
		catch(SQLException e){
			logger.error("Failed to insert assembly data into database", e);
			return false;
		}
	}
	
	/* *****************************************************************************************
	 * 
	 * 
	 *										REMOVE (DELETES)
	 * 
	 * 
	 *******************************************************************************************/

	
	public int unmapAssembly(DatabaseManager manager, int run_id){
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT COUNT(contig_bioentry_id) FROM "+BioSQLExtended.assembly+" WHERE run_id="+run_id);
			set.next();
			int c=set.getInt(1);
			if(c>10000)logger.info("Attempting to remove " + c + " records...");
			else logger.debug("Attempting to remove " + c + " record from 2 tables");
			st.execute("DELETE FROM "+BioSQLExtended.assembly+" WHERE run_id="+run_id);
			set = st.executeQuery("SELECT COUNT(contig_bioentry_id) FROM "+BioSQLExtended.assembly+" WHERE run_id="+run_id);
			set.next();
			int d = set.getInt(1);
			if(d!=0)logger.warn("Failed to remove all records with run_id="+ run_id + " after explict expression, new ones uploaded?");
			set.close();
			st.close();
			return c-d;
		}
		catch(SQLException sq){
			logger.error("Failed to unmap assembly for "+run_id , sq);
			return -1;
		}
	}

	public int removeBioentrysWRunID(DatabaseManager manager, int run_id){
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT COUNT(bioentry_id) FROM bioentry INNER JOIN bioentry_run USING(bioentry_id) WHERE run_id="+run_id);
			set.next();
			int c=set.getInt(1);
			
			if(c>10000)logger.info("Attempting to remove " + c + " record from 2 tables, this may take a while...");
			else logger.debug("Attempting to remove " + c + " record from 2 tables");
			
			st.execute("DELETE bioentry, bioentry_run FROM bioentry INNER JOIN bioentry_run USING(bioentry_id) WHERE run_id="+run_id);
			set = st.executeQuery("SELECT COUNT(bioentry_id) FROM bioentry INNER JOIN bioentry_run USING(bioentry_id) WHERE run_id="+run_id);
			set.next();
			int d = set.getInt(1);
			if(d!=0)logger.warn("Failed to remove all records with run_id="+ run_id + " after explict expression, new ones uploaded?");
			set.close();
			st.close();
			return c-d;
		}
		catch(SQLException sq){
			logger.error("Failed to unmap assembly for "+run_id , sq);
			return -1;
		}
	}
	
	public boolean removeRun(DatabaseManager manager, int run_id){
		try{
			Statement st = manager.getCon().createStatement();
			st.execute("DELETE FROM run WHERE run_id="+run_id);
			st.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to unmap assembly for "+run_id , sq);
			return false;
		}
	}
	
	
	/* *****************************************************************************************
	 * 
	 * 
	 *										GETS (SELECTS)
	 * 
	 * 
	 *******************************************************************************************/

	public int[] getRunId(DatabaseManager manager, String programname, String runtype){
		String sql = new String("SELECT run_id FROM run WHERE runtype='"+runtype+"'");
		if(programname != null)sql+=" AND program LIKE '"+programname+"'"; 
		LinkedList<Integer> ins = new LinkedList<Integer>();
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			while(set.next()){
				ins.add(set.getInt(1));
			}
			set.close();
			st.close();
			return Tools_Array.ListInt2int(ins);
		}
		catch(SQLException sq){
			logger.error("Failed to get run id." , sq);
			return new int[]{-1};
		}
	}

	public Run getRun(DatabaseManager manager, int run_id){
		String sql = new String("SELECT run_date, runtype, parent_id, program, version, dbname,source, params, comment FROM run WHERE run_id="+run_id);
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			Run r = null;
			while(set.next()){
				r= new Run(run_id, set.getDate(1) ,set.getString(2), set.getInt(3),
						set.getString(4), set.getString(5), set.getString(6), 
						set.getString(7),set.getString(8), set.getString(9));
				break;
			}
			set.close();
			st.close();
			return r;
		}
		catch(SQLException sq){
			logger.error("Failed to get run id." , sq);
			return null;
		}
	}
	
	
	public int getRunIdFromInfo(DatabaseManager manager, Run r){
		String sql = "SELECT run_id FROM run WHERE run_date=? AND" +
				" runtype=? AND parent_id=? AND program=? AND " +
				"version=? AND dbname=? AND source=? AND params=? AND comment=?";
		try{
			PreparedStatement runGET = manager.getCon().prepareStatement(sql);
			runGET.setDate(1, Tools_System.util2sql(r.getDate()));
			runGET.setString(2, r.getRuntype());
			if(r.getParent_id() != null) runGET.setInt(3, r.getParent_id());
			else runGET.setNull(3, Types.INTEGER);
			if(r.getProgram() != null) runGET.setString(4, r.getProgram());
			else runGET.setNull(4, Types.VARCHAR);
			if(r.getVersion() != null) runGET.setString(5, r.getVersion());
			else runGET.setNull(5, Types.VARCHAR);
			if(r.getDbname() != null) runGET.setString(6, r.getDbname());
			else runGET.setNull(6, Types.VARCHAR);
			if(r.getSource() != null) runGET.setString(7, r.getSource());
			else runGET.setNull(7, Types.VARCHAR);
			if(r.getParams() != null) runGET.setString(8, r.getParams());
			else runGET.setNull(8, Types.VARCHAR);
			if(r.getComment() != null) runGET.setString(9, r.getComment());
			else runGET.setNull(9, Types.VARCHAR);
			
			sql = runGET.toString();
			set = runGET.executeQuery();
			int i =-1;
			while(set.next()){
				i= set.getInt("run_id");
			}
			set.close();
			runGET.close();
			return i;
		}
		catch(SQLException e){
			logger.error("Failed to retrieve Run id using SQL: " + sql, e);
			return -1;
		}
	}
	
	public int[] getRunIdFromBioentryIDs(Connection con, int bioentry_id){
		String sql = "SELECT run_id FROM bioentry_run WHERE bioentry_id="+bioentry_id;
	
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(sql);
			LinkedList<Integer> ints = new LinkedList<Integer>();
			while(set.next()){
				ints.add(set.getInt(1));
			}
			set.close();
			st.close();
			return Tools_Array.ListInt2int(ints);
		}
		catch(SQLException e){
			logger.error("Failed to retrieve run ids for bioentry_id:" + bioentry_id, e);
		}
		return null;
	}
	
	public BioSequence[] getBioSequences(DatabaseManager manager, int bioentry_id){
		String sql = "SELECT biosequence.version, biosequence.length, biosequence.alphabet, " +
				"biosequence.seq, bioentry.identifier FROM biosequence " +
				"INNER JOIN bioentry USING(bioentry_id) WHERE bioentry_id=?";
		LinkedList<BioSequence> biosequences = new LinkedList<BioSequence>();
		try {
			PreparedStatement bioSequenceGET = manager.getCon().prepareStatement(sql);
			bioSequenceGET.setInt(1, bioentry_id);
			set = bioSequenceGET.executeQuery();
			while(set.next()){
				BioSequence s = new BasicBioSequence(set.getString(5),bioentry_id, set.getInt(1), set.getInt(2), set.getString(3), set.getString(4));
				biosequences.add(s);
			}
			set.close();
			bioSequenceGET.close();
			return biosequences.toArray(new BioSequence[0]);
		} 
		catch(SQLException e){
			logger.error("Failed to insert assembly data into database", e);
			return null;
		}
	}
	
	
	public Dbxref getDbxRef(Connection con, int dbxref_id){
		String query = new String("SELECT dbxref_id, dbname, accession, " +
				"version, ncbi_taxon_id, description FROM dbxref WHERE dbxref_id="+dbxref_id);	
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(query);
			Dbxref d = null;
			while(set.next()){
				d= new Dbxref(set.getInt(1),set.getString(2),set.getString(3), 
						set.getInt(4),set.getInt(5), set.getString(6));
			}
			set.close();
			st.close();
			return d;
		}
		catch(SQLException e){
			logger.error("Failed to retrieve dbxref record for dbxref id " + dbxref_id, e);
		}
		return null;
	}
	
	public Dbxref_Bioentry_Link[] getDbxRef_Bioentry_Links(Connection con, int bioentry_id, int run_id){
		String query = new String("SELECT bioentry_id, dbxref_id, rank, " +
				"hit_no, run_id, evalue, score, dbxref_startpos, dbxref_endpos, dbxref_frame, " +
				"bioentry_startpos, bioentry_endpos, bioentry_frame FROM bioentry_dbxref " +
				"WHERE bioentry_id="+bioentry_id);
		if(run_id > 0)query+=" AND run_id="+run_id;
		
		LinkedList<Dbxref_Bioentry_Link> dbxrefs = new LinkedList<Dbxref_Bioentry_Link>();
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(query);
			while(set.next()){
				Dbxref_Bioentry_Link d = new Dbxref_Bioentry_Link(
						set.getInt(1),set.getInt(2),
						set.getInt(3),set.getInt(4),
						set.getInt(5),set.getDouble(6),
						set.getInt(7),set.getInt(8),
						set.getInt(9),set.getInt(10),
						set.getInt(11),set.getInt(12),
						set.getInt(13)
				);
				dbxrefs.add(d);
			}
			set.close();
			st.close();
			return dbxrefs.toArray(new Dbxref_Bioentry_Link[0]);
		}
		catch(SQLException e){
			logger.error("Failed to retrieve bioentry_dbxref_id link for bioentry id " + bioentry_id, e);
		}
		return null;
	}
	
	/* *****************************************************************************************
	 * 
	 * 
	 *										INSERTS
	 * 
	 * 
	 *******************************************************************************************/
	

	public boolean setBioentry2Dbxref(DatabaseManager manager, int bioentry_id,  int dbxref_id, int run_id, Double evalue, Integer score, Integer dbxref_startpos,
		Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame, int hit, int hsp){
		String sql = "INSERT IGNORE INTO bioentry_dbxref (bioentry_id, dbxref_id, run_id, hit_no, rank, evalue,score, dbxref_startpos,"+
			"dbxref_endpos, dbxref_frame, bioentry_startpos, bioentry_endpos, bioentry_frame) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			//System.out.println(bioentry_id);
			PreparedStatement dbxrefGET = manager.getCon().prepareStatement(sql);
			dbxrefGET.setInt(1, bioentry_id);
			dbxrefGET.setInt(2, dbxref_id);
			dbxrefGET.setInt(3, run_id);
			dbxrefGET.setInt(4, hit);
			dbxrefGET.setInt(5, hsp);
			
			if(evalue != null) dbxrefGET.setDouble(6, evalue);
			else dbxrefGET.setNull(6, Types.DOUBLE);
			if(score != null) dbxrefGET.setInt(7, score);
			else dbxrefGET.setNull(7, Types.INTEGER);
			if(dbxref_startpos != null) dbxrefGET.setInt(8, dbxref_startpos);
			else dbxrefGET.setNull(8, Types.INTEGER);
			if(dbxref_endpos != null) dbxrefGET.setInt(9, dbxref_endpos);
			else dbxrefGET.setNull(9, Types.INTEGER);
			if(dbxref_frame != null) dbxrefGET.setInt(10, dbxref_frame);
			else dbxrefGET.setNull(10, Types.INTEGER);
			if(bioentry_startpos != null) dbxrefGET.setInt(11, bioentry_startpos);
			else dbxrefGET.setNull(11, Types.INTEGER);
			if(bioentry_endpos != null) dbxrefGET.setInt(12, bioentry_endpos);
			else dbxrefGET.setNull(12, Types.INTEGER);
			if(bioentry_frame != null) dbxrefGET.setInt(13, bioentry_frame);
			else dbxrefGET.setNull(13, Types.INTEGER);
			dbxrefGET.execute();
			dbxrefGET.close();
			return true;
		} 
		catch (SQLException e) {
			logger.error("Failed to add bioentry_dbxref entry", e);
			return false;
		}
	}
	
	public boolean updateDbxref(DatabaseManager manager, int bioentry_id,  int dbxref_id, int run_id, Double evalue, Integer score, Integer dbxref_startpos,
		Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame, int hit, int hsp){

		String sql = "UPDATE bioentry_dbxref SET evalue=?, score=?, dbxref_startpos=?,"+
				" dbxref_endpos=?, dbxref_frame=?, bioentry_startpos=?, bioentry_endpos=?, bioentry_frame=?" +
				" WHERE bioentry_id=? AND dbxref_id=? AND run_id=? AND rank=? AND hit_no=?";
		
		try {
			//System.out.println(bioentry_id);
			PreparedStatement dbxrefUP = manager.getCon().prepareStatement(sql);
			dbxrefUP.setInt(9, bioentry_id);
			dbxrefUP.setInt(10, dbxref_id);
			dbxrefUP.setInt(11, run_id);
			dbxrefUP.setInt(12, hsp);
			dbxrefUP.setInt(13, hit);
			
			if(evalue != null) dbxrefUP.setDouble(1, evalue);
			else dbxrefUP.setNull(1, Types.DOUBLE);
			if(score != null) dbxrefUP.setInt(2, score);
			else dbxrefUP.setNull(2, Types.INTEGER);
			if(dbxref_startpos != null) dbxrefUP.setInt(3, dbxref_startpos);
			else dbxrefUP.setNull(3, Types.INTEGER);
			if(dbxref_endpos != null) dbxrefUP.setInt(4, dbxref_endpos);
			else dbxrefUP.setNull(4, Types.INTEGER);
			if(dbxref_frame != null) dbxrefUP.setInt(5, dbxref_frame);
			else dbxrefUP.setNull(5, Types.INTEGER);
			if(bioentry_startpos != null) dbxrefUP.setInt(6, bioentry_startpos);
			else dbxrefUP.setNull(6, Types.INTEGER);
			if(bioentry_endpos != null) dbxrefUP.setInt(7, bioentry_endpos);
			else dbxrefUP.setNull(7, Types.INTEGER);
			if(bioentry_frame != null) dbxrefUP.setInt(8, bioentry_frame);
			else dbxrefUP.setNull(8, Types.INTEGER);
			dbxrefUP.execute();
			dbxrefUP.close();
			return true;
		} 
		catch (SQLException e) {
			logger.error("Failed to update bioentry_dbxref entry", e);
			return false;
		}
	}

	
	public boolean setRun(DatabaseManager manager, Date date, String runtype, Integer parent, String program, String version, String dbname, String source, String params, String comment){		
		String forerr = "No SQL statement generated";
		try{
			PreparedStatement runSET = manager.getCon().prepareStatement(
					"INSERT INTO run (run_date, runtype, parent_id, program," +
					" version, dbname, source, params, comment) VALUES (?,?,?," +
					"?,?,?,?,?,?);");
			runSET.setDate(1, date);
			runSET.setString(2, runtype);
			if(parent == null)runSET.setNull(3, Types.INTEGER);
			else runSET.setInt(3, parent);
			runSET.setString(4, program);
			if(version == null)runSET.setNull(5, Types.VARCHAR);
			else runSET.setString(5, version);
			if(dbname == null)runSET.setNull(6, Types.VARCHAR);
			else runSET.setString(6, dbname);
			if(source == null)runSET.setNull(7, Types.VARCHAR);
			else runSET.setString(7, source);
			if(params == null)runSET.setNull(8, Types.VARCHAR);
			else runSET.setString(8, params);
			if(comment == null)runSET.setNull(9, Types.VARCHAR);
			else runSET.setString(9, comment);
			
			forerr= runSET.toString();
			logger.trace("Attempting to execute " + forerr);
			runSET.execute();
			runSET.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to insert run, SQL: "+forerr, sq);
			return false;
		}
	}

	public String[] getContigNames(DatabaseManager manager, int r, int i) {
		String sql = new String("SELECT bioentry.accession FROM assembly INNER JOIN bioentry ON " +
				"bioentry.bioentry_id=assembly.contig_bioentry_id WHERE assembly.run_id="+r+" LIMIT 0,"+i);
		Statement st;
		String[] ress = new String[i];
		try {
			st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			int c =0;
			while(set.next()){
				ress[c] = set.getString(1);
				c++;
			}
			set.close();
			st.close();
		} 
		catch (SQLException e) {
			logger.error(e);
		}
		return ress;
	}

	public SequenceList getContigsAsList(DatabaseManager manager, SequenceList l, int run_id) {
		String sql;
		Statement st;
		if( run_id < 0){
			sql = "SELECT bioentry.bioentry_id, bioentry.identifier, seq FROM biosequence INNER JOIN bioentry ON bioentry.bioentry_id=biosequence.bioentry_id WHERE division='CONTIG'";
		}
		else{
			sql = "SELECT bioentry.bioentry_id, bioentry.identifier, seq FROM biosequence INNER JOIN bioentry ON bioentry.bioentry_id=biosequence.bioentry_id " +
					"INNER JOIN assembly ON bioentry.bioentry_id=assembly.contig_bioentry_id WHERE division='CONTIG' AND run_id="+run_id;
		}
		try {
			st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			while(set.next()){
				l.addSequenceObject(new GenericSequence(set.getString(2), set.getString(3)));
			}
			System.out.println();
			set.close();
			st.close();
		} 
		catch (SQLException e) {
			logger.error(e);
		}
		
		return l;
	}
	


	public Contig getContig(DatabaseManager manager, int bioentry_id) {
		Contig c = new BasicContig();

		BioSequence b = this.getBioSequences(manager, bioentry_id)[0];
		SequenceObjectXT consensus = new GenericSequenceXT(b.getIdentifier(), b.getSequence());
		c.setConsensus(consensus);
		int[][] reads = this.getReads(manager, bioentry_id);
		for(int i =0; i < reads[0].length;i++){
			b = this.getBioSequences(manager, reads[0][i])[0];
			SequenceObjectXT read = new GenericSequenceXT(b.getIdentifier(), b.getSequence());
			read.setOffset(reads[3][i], 0);
			c.addSequenceObject(read);
		}
		return c;
	}
	

	public Taxonomy getTaxonomyFromSQL(DatabaseManager manager,
			Integer biosql_id, Integer ncbi_id) {
		Taxonomy T = new Taxonomy();
		if(biosql_id == null && ncbi_id == null){
			logger.error("Both biosql_id and ncbi_id set to null");
		}
		else if(ncbi_id != null){
			biosql_id = manager.getBioSQL().getTaxonIdwNCBI(manager.getCon(), ncbi_id);
		}
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT * FROM taxon WHERE taxon_id='"+biosql_id+"'");
			while(set.next()){
				T.setNcbi_taxid(set.getInt("ncbi_taxon_id"));
				T.setParent_taxid(set.getInt("parent_taxon_id"));
				T.setNode_rank(set.getString("node_rank"));
				T.setGenetic_code(set.getInt("genetic_code"));
				T.setMitogenetic_code(set.getInt("mito_genetic_code"));
				T.setRight_value(set.getInt("right_value"));
				T.setLeft_value(set.getInt("left_value"));
			}
			set.close();
			set = st.executeQuery("SELECT * FROM taxon_name WHERE taxon_id='"+biosql_id+"'");
			while(set.next()){
				if(set.getString("name_class").equals("ScientificName")){
					T.setSciencename(set.getString("name"));
				}
				else if (set.getString("name_class").equals("CommonName")){
					T.setCommonname(set.getString("name"));
				}
			}
			set.close();
			st.close();
		}
		catch(SQLException sq){
			logger.error("Failed to get taxon stuff",sq);
		}
		return T;
	}
	
	public int[][] getTaxonPerAssembly(DatabaseManager manager, int[] listoftaxids, int blast_run_id, double evalue, int hit_no){
		int[][] retur = new int[][]{listoftaxids, new int[listoftaxids.length]};
		try{
			String sql = "SELECT COUNT(bioentry_id) AS COUNT FROM bioentry_dbxref INNER JOIN dbxref USING (dbxref_id)" +
					" WHERE dbxref.ncbi_taxon_id IN (SELECT taxon.ncbi_taxon_id FROM taxon" +
					" INNER JOIN taxon AS include ON (taxon.left_value BETWEEN include.left_value AND include.right_value)" +
					" WHERE include.ncbi_taxon_id=?) AND bioentry_dbxref.run_id="+blast_run_id;
			if(evalue != -1){
				sql+= " AND bioentry_dbxref.evalue<"+evalue;
			}
			if(hit_no !=-1){
				sql+= " AND bioentry_dbxref.hit_no<="+hit_no;
			}

			PreparedStatement st = manager.getCon().prepareStatement(sql);
			int c=0;
			for(int i : listoftaxids){
				st.setInt(1,i);
				set = st.executeQuery();
				while(set.next()){
					retur[1][c] = set.getInt("COUNT");
				}
				System.out.print("\r "+c + " of "+retur[0].length + " is    "+ retur[1][c] +"   " );
				c++;
			}
			set.close();
			st.close();
			System.out.println();
			return retur;
		}
		catch(SQLException s){
			logger.error("Failed to get count for taxids", s);
		}
		return retur;
		
	}
	
	/**
	 * 
	 * @param manager
	 * @return returns a hashmap 
	 * of ncbi_taxon_id is key and name
	 * of node_rank as value
	 */
	public HashMap<Integer, String> getNodeRank(DatabaseManager manager, String node_rank){
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT ncbi_taxon_id, taxon_name.name FROM taxon INNER JOIN taxon_name USING(taxon_id) WHERE taxon.node_rank LIKE '"+node_rank+"';");
			HashMap<Integer, String> map = new HashMap<Integer, String>();
			while(set.next())map.put(set.getInt(1), set.getString(2));
			return map;
		}
		catch(SQLException sq){
			logger.error("Could not count fields",sq);
			return null;
		}
	}
	
	public int[] subTaxons(DatabaseManager manager, int ncbi_taxon_id){
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT ncbi_taxon_id FROM taxon INNER JOIN taxon AS include ON (taxon.left_value BETWEEN include.left_value AND"+
	                " include.right_value) WHERE include.ncbi_taxon_id="+ncbi_taxon_id);
			LinkedList<Integer> is = new LinkedList<Integer>();
			while(set.next())is.add(set.getInt(1));
			set.close();
			st.close();
			return Tools_Array.ListInt2int(is);
		}
		catch(SQLException sq){
			logger.error("Could not count fields",sq);
			return null;
		}
	}
	
	/**
	 * Return the number of accessions which have no taxid
	 * attached to the databse
	 * 
	 * @param manager
	 * @param database
	 * @return
	 */
	private int getTaxonCount(DatabaseManager manager, String database){
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT COUNT(dbxref_id) AS count FROM dbxref WHERE ncbi_taxon_id IS NULL AND dbname='"+database+"'");
			int size=-1;
			while(set.next())size=set.getInt(1);
			set.close();
			st.close();
			return size;
		}
		catch(SQLException sq){
			logger.error("Could not count fields",sq);
			return -1;
		}
	}
	
	public boolean updateTaxonParents(DatabaseManager manager){
		int size=1;
		int records=1;
		int iters=0;
		try{
			Statement st = manager.getCon().createStatement();
	
			int[] ids = null;
			while((records>0 || size>0) && iters<4){
				
				//Deal with records missing parent_ids
				ResultSet set = st.executeQuery("SELECT COUNT(ncbi_taxon_id) AS COUNT FROM taxon WHERE parent_taxon_id IS NULL");
				while(set.next())size=set.getInt("COUNT");
				logger.debug(size+" records found without ids");
				if(size != 0){
					set = st.executeQuery("SELECT ncbi_taxon_id FROM taxon WHERE parent_taxon_id IS NULL");
					ids = new int[size+1];
					logger.info("Retrieving IDs for "+ size+ " taxon records");
					int c=0;
					while(set.next()){
						ids[c] = set.getInt("ncbi_taxon_id");
						c++;
					}
					for(int i =0; i < ids.length;i++) {
						if(ids[i] >1){//Quick hack because I ballsed up the array sizing for some reason
							new Taxonomy(ids[i]).upload2DB(manager, true);
							System.out.print("\r"+(i+1)+" of "+ids.length +" complete");
						}
						if(ids[i] == Tools_NCBI.ncbi_root_taxon){
							logger.debug("NCBI ID "+Tools_NCBI.ncbi_root_taxon+" is assumed to be" +
									" the root and has no parent, removing from list");
							size--;
						}
					}
					System.out.println();
				}
				
				//Deal with missing records
				set.close();
				set = st.executeQuery("SELECT COUNT(DISTINCT(`parent_taxon_id`)) AS COUNT FROM `taxon` WHERE `parent_taxon_id`" +
						" NOT IN (SELECT `ncbi_taxon_id` FROM taxon);");
				while(set.next())records=set.getInt("COUNT");
				set.close();
				logger.debug(records+" ids found without records");
				if(records != 0){
					set = st.executeQuery("SELECT DISTINCT(`parent_taxon_id`) AS id FROM `taxon` WHERE `parent_taxon_id`" +
							" NOT IN (SELECT `ncbi_taxon_id` FROM taxon);");
					logger.info("Adding "+records+" missing records");
					ids = new int[records+1];
					int c=0;
					while(set.next())ids[c++] = set.getInt("id");
	
					for(int i =0;i < ids.length; i++){
						if(ids[i] > 0){
							new Taxonomy(ids[i]).upload2DB(manager, true);
							System.out.print("\r"+(i+1)+" of "+ids.length +" complete");
						}
					}
					
				}
				
				System.out.println();
				iters++;
			}
			if(iters > 3){
				logger.warn("Method re-iterated 4 times, cancelling..." +
						" this may be a bug, if it isn't just rerun task again");
			}
			st.close();
		}
		catch(SQLException e){
			logger.error("Failed to populate parents");
			return false;
		}
		
		return true;
	}
	
	public boolean updateAccs(DatabaseManager manager, String localdb, NCBI_DATABASE ncbidb){
		int c=0;
		int d =0;
		int err=0;
		try{
			int size = getTaxonCount(manager, localdb);
			if(size != 0){
				Statement st = manager.getCon().createStatement();
				set = st.executeQuery("SELECT accession, dbxref_id FROM dbxref WHERE ncbi_taxon_id IS NULL AND dbname='"+localdb+"'");
				HashMap<Integer, String> map = new HashMap<Integer, String>();
				logger.info("Retrieve the ncbi taxons from ncbi");
				while(set.next()){
					int id = set.getInt("dbxref_id");
					String acc = set.getString("accession");
					map.put(id, acc);
					c++;
					System.out.print("\r"+c+" of " + size*2 + "  phase 1      ");
				}
				System.out.println();
				set.close();
				st.close();
				
				logger.info("Updating database with ncbi taxons, any missing taxons will be added");
				PreparedStatement pst = manager.getCon().prepareStatement("UPDATE dbxref SET ncbi_taxon_id=? WHERE dbxref_id=?");
				for(Integer i : map.keySet()){
					Integer p = null;
					String s = Tools_NCBI.getTaxIDFromAccession(ncbidb, map.get(i));
					if(s != null){
						p = Tools_String.parseString2Int(s);
					}
					if(p != null){
						boolean cont = true;
						if(manager.getBioSQL().getTaxonIdwNCBI(manager.getCon(), p) < 1){
							if(!new Taxonomy(s).upload2DB(manager, false)){
								logger.error("Error failed to upload missing taxonomy for protein with accession " + map.get(i));
								cont=false;
								err++;
							}
						}
						if(cont){
							pst.setInt(1, p);
							pst.setInt(2, i);
							pst.execute();
							System.out.print("\r"+(c+d)+" of " + size*2 + "  phase 2      ");
						d++;
						}
					}
					else err++;
				}
				System.out.println();
				System.out.println((c+d)-err*2+" tax ids updated, with " +err+ " errors where taxid not uploaded");
				pst.close();
				return true;
			}
			else{
				logger.error("No references with null taxid for the database " + localdb);
				
			}
		}
		catch(Exception e){
			logger.error("Failed to update accs with taxon ids", e);
		}
		return false;
	}
	

	public boolean depthTraversalTaxon(DatabaseManager manager, int root_id){
		try{
			PreparedStatement children = manager.getCon().prepareStatement("SELECT ncbi_taxon_id FROM taxon WHERE parent_taxon_id=?");
			PreparedStatement setleft = manager.getCon().prepareStatement("UPDATE taxon SET left_value=? WHERE ncbi_taxon_id=?");
			PreparedStatement setright = manager.getCon().prepareStatement("UPDATE taxon SET right_value=? WHERE ncbi_taxon_id=?");
			valuesize=0;
			set = manager.getCon().createStatement().executeQuery("SELECT COUNT(taxon_id) AS COUNT FROM taxon;");
			while(set.next())valuesize=set.getInt(1)*2;
			depthcount =1;
			depthid = root_id;
			layerdepth=1;
			walktree(children, setleft, setright);
			System.out.println();
			set.close();
			children.close();
			setleft.close();
			setright.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to run depth traversal for taxons ", sq);
			return false;
		}
		
	}
	
	private void walktree(PreparedStatement children, PreparedStatement setleft, PreparedStatement setright){
		try{
			System.out.print("\rCount: "+depthcount+" of "+valuesize+" at depth " + layerdepth);
			layerdepth++;
			int current = depthid;
			setleft.setInt(1, depthcount++);
			setleft.setInt(2, current);
			setleft.execute();
			children.setInt(1, current);
			//I don't think we can execute another query
			//before this is closed so will have to pop a list
			set.close();
			set = children.executeQuery();
			LinkedList<Integer> ins = new LinkedList<Integer>();
			while(set.next()){
				ins.add(set.getInt(1));
			}
			
			for(Integer i : ins){
				depthid=i;
				walktree(children, setleft, setright);
			}
			layerdepth--;
			setright.setInt(1, depthcount++);
			setright.setInt(2, current);
			setright.execute();
		}
		catch(SQLException sq){
			logger.error("Failed to run depth traversal for taxons ", sq);
		}
	}


	public boolean addRunBioentry(DatabaseManager manager, int bioentry, int runid) {
		String sql2 = "INSERT IGNORE INTO bioentry_run (bioentry_id, run_id, rank) VALUES (?,?,?)";
		try {
			PreparedStatement bioen_runSET = manager.getCon().prepareStatement(sql2);
			bioen_runSET.setInt(1, bioentry);
			bioen_runSET.setInt(2, runid);
			bioen_runSET.setInt(3, 0);
			bioen_runSET.execute();
			bioen_runSET.close();
			return true;
		} 
		catch(SQLException e){
			logger.error("Failed to insert assembly data into database", e);
			return false;
		}
	}
	
//	public List<Dbxref> getDbxref(DatabaseManager manager, int dbxref, String accession, int taxid, String dbname){
//		StringBuffer sql = new StringBuffer("SELECT dbxref_id, dbname, accession, version, ncbi_taxon_id FROM dbxref WHERE ");
//		LinkedList<String> s =  new LinkedList<String>();
//		if(dbxref > 0) s.add("dxref_id="+dbxref+ " ");
//		if(accession != null)s.add("accession='"+accession+"' ");
//		if(taxid > 0)s.add("ncbi_taxon_id="+taxid+" ");
//		if(dbname!= null)s.add("dbname='"+dbname+"'");
//		if(s.size() == 0)logger.error("No where statements added, will select everything");
//		if(s.size() == 1){
//			sql.append(s.get(0));
//		}
//		else{
//			for(int i=0 ; i < s.size() ;i++){
//				if(i != s.size()-1){
//					sql.append(s);
//					sql.append("AND ");
//				}
//				else{
//					sql.append(s);
//					sql.append(';');
//				}
//			}
//		}		
//		LinkedList<Dbxref> refs = new LinkedList<Dbxref>();
//		try{
//			Statement st = manager.getCon().createStatement();
//			set = st.executeQuery(sql.toString());
//			while(set.next()){
//				Dbxref ref = new Dbxref();
//				ref.setAccession(set.getString("accession"));
//				ref.setDbname(set.getString("dbname"));
//				ref.setDbxref_id(set.getInt("dbxref_id"));
//				ref.setTaxon_id(set.getInt("ncbi_taxon_id"));
//				ref.setVersion(set.getInt("version"));
//			}
//		}
//		catch(SQLException sq){
//			logger.error("Failed to execute " + sql.toString(), sq);
//		}
//		return refs;
//	}
	
	public int getHitCount(DatabaseManager manager, int run_id,double evalue, int hit_no, int hsp_no, int score, boolean unique){
		String field = unique ? "DISTINCT(dbxref_id)" : "dbxref_id" ;
		StringBuffer sql = new StringBuffer("SELECT COUNT("+field+") AS COUNT FROM bioentry_dbxref WHERE ");
		LinkedList<String> s =  new LinkedList<String>();
		if(run_id > 0 )s.add("run_id="+run_id+" ");
		if(evalue >=0 )s.add("evalue<"+evalue+" ");
		if(hit_no > 0)s.add("hit_no<="+hit_no+" ");
		if(hsp_no > 0)s.add("rank<="+hsp_no+" ");
		if(score > 0)s.add("score<="+score+" ");
		if(s.size() == 0)logger.warn("No where statements added, will select everything");
		else sql.append(s.get(0));
		for(int i =0; i < s.size(); i++) if(i!=0)sql.append("AND "+s.get(i));
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql.toString());
			int count =-1;
			while(set.next()){
				count = set.getInt("COUNT");
				break;
			}
			set.close();
			st.close();
			return count;
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + sql.toString(), sq);
		}
		return -1;
	}
	
	public int getHitCountwReadCount(DatabaseManager manager, int run_id,double evalue, int hit_no, int hsp_no, int score){
		StringBuffer sql = new StringBuffer("SELECT COUNT(DISTINCT(read_bioentry_id)) AS COUNT FROM bioentry_dbxref " +
				"INNER JOIN assembly ON bioentry_dbxref.bioentry_id=assembly.contig_bioentry_id WHERE ");
		LinkedList<String> s =  new LinkedList<String>();
		if(run_id > 0 )s.add("bioentry_dbxref.run_id="+run_id+" ");
		if(evalue >=0 )s.add("bioentry_dbxref.evalue<"+evalue+" ");
		if(hit_no > 0)s.add("bioentry_dbxref.hit_no<="+hit_no+" ");
		if(hsp_no > 0)s.add("bioentry_dbxref.rank<="+hsp_no+" ");
		if(score > 0)s.add("bioentry_dbxref.score<="+score+" ");
		if(s.size() == 0)logger.warn("No where statements added, will select everything");
		else sql.append(s.get(0));
		for(int i =0; i < s.size(); i++) if(i!=0)sql.append("AND "+s.get(i));
		try{
			logger.debug(sql);
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql.toString());
			int count =-1;
			while(set.next()){
				count = set.getInt("COUNT");
				break;
			}
			set.close();
			st.close();
			return count;
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + sql.toString(), sq);
		}
		return -1;
	}
	
	
	/*
	 * Equivalent to this, but java won't let me set local variable in database :(
	 * 	SET @runtot:=0; SELECT q1.EVALUE, (@runtot := @runtot + COUNT) AS CUMULATIVE FROM (SELECT ...
	 */
	public boolean cumulativeCountQuery(DatabaseManager manager, File output, int blastRun, int hit_no, boolean header){
		StringBuffer sql = new StringBuffer("SELECT bioentry_dbxref.evalue AS EVALUE, COUNT(bioentry_dbxref.evalue)" +
				" AS COUNT FROM bioentry_dbxref " +
				" WHERE bioentry_dbxref.rank=1");
		sql.append(" AND bioentry_dbxref.run_id=");
		sql.append(blastRun);
		if(hit_no != -1){
			sql.append(" AND hit_no<=");
			sql.append(hit_no);
		}
		sql.append(" GROUP BY bioentry_dbxref.evalue ORDER BY bioentry_dbxref.evalue;");
		
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql.toString());
			FileWriter fstream = new FileWriter(output, false);
			BufferedWriter out = new BufferedWriter(fstream);
			String n = Tools_System.getNewline();
			int cumulative=0;
			if(header)out.write("EVALUE HITS"+n);
			out.flush();
			while(set.next()){
				cumulative+=set.getInt(2);
				out.write(set.getDouble(1) + " " + cumulative + n);
				out.flush();
			}
			set.close();
			st.close();
			out.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + sql.toString(), sq);
			return false;
		} catch (IOException e) {
			if(output != null)logger.error("Failed to write to file " + output.getPath());
			else logger.error("File output is null, can't write to null file");
			return false;
		}
	}
	
	public boolean runSpeciesQuery(DatabaseManager manager, File output, int blastRun, double evalue, int hit_no, boolean taxids){
		String sql = "SELECT dbxref.ncbi_taxon_id AS taxid, taxon_name.name AS taxname," +
				" COUNT(dbxref.ncbi_taxon_id) AS count FROM dbxref INNER JOIN taxon USING" +
				" (ncbi_taxon_id) INNER JOIN taxon_name USING (taxon_id)" +
				" INNER JOIN bioentry_dbxref USING (dbxref_id)" +
				" WHERE bioentry_dbxref.run_id="+blastRun;
				if(hit_no > 0)sql+=" AND bioentry_dbxref.rank=1 AND bioentry_dbxref.hit_no="+hit_no;
				if(evalue >0 )sql+=" AND bioentry_dbxref.evalue<"+evalue+"";
				sql+=" AND taxon_name.name_class='ScientificName' GROUP BY taxid ORDER BY count DESC;";
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			FileWriter fstream = new FileWriter(output, false);
			BufferedWriter out = new BufferedWriter(fstream);
			String n = Tools_System.getNewline();
			String writ = (taxids) ? "TaxonID,Count"+n:"\"Species\",Count"+n; 
			out.write(writ);
			out.flush();
			while(set.next()){
				writ = taxids ? set.getInt(1) +",":"\""+set.getString(2)+"\",";
				out.write(writ+set.getInt(3)+ n);
				out.flush();
			}
			set.close();
			st.close();
			out.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + sql.toString(), sq);
			return false;
		} catch (IOException e) {
			if(output != null)logger.error("Failed to write to file " + output.getPath());
			else logger.error("File output is null, can't write to null file");
			return false;
		}
	}


	public boolean resetDepth(DatabaseManager manager) {
		String query = "UPDATE taxon SET left_value=NULL, right_value=NULL;";
		try{
			Statement st = manager.getCon().createStatement();
			st.execute(query);
			st.close();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + query, sq);
		}
		return false;
	}
	
	
	public int getSimilarRun(DatabaseManager manager, Run run, int date_range){
		int ret = -1;
		long range =date_range;
		
		try{
			String query = "SELECT run_id, run_date FROM run WHERE " +
					"runtype='"+run.getRuntype()+"' AND program='"+run.getProgram()+"' AND version='"+
					run.getVersion()+"' AND dbname='"+run.getDbname()+"' AND params='"+run.getParams()+"'";
			Statement st = manager.getCon().createStatement();
			ResultSet set = st.executeQuery(query);
			while(set.next()){
				java.sql.Date r = set.getDate("run_date");
				long l = Math.abs(r.getTime()-run.getDate().getTime());
				l /=86400; //(60*60*24)
				if(l < range){
					range = l;
					ret=set.getInt("run_id");
				}
			}
			set.close();
			st.close();
			return ret;
		}
		catch(SQLException sq){
			logger.error("Failed to conduct SQL for similar runs", sq);
			return -1;
		}
	}
	
	public int[] getDbCoverageAssembly(DatabaseManager manager, int hit, int hsp, double evalue, int run_id, boolean meta_assembly){
		
		int total =0;
		int overrep=0;
		String query =null;
		if(!meta_assembly){
			query = "SELECT dbxref_id, dbxref_startpos, dbxref_endpos " +
				"FROM bioentry_dbxref WHERE hit_no<="+hit+" AND rank<="+hsp+" " +
				"AND run_id="+run_id+" ORDER BY dbxref_id, dbxref_startpos, dbxref_endpos";
		}
		else{
			query = "SELECT dbxref_id, dbxref_startpos, dbxref_endpos, bioentry_id  " +
					"FROM bioentry_dbxref INNER JOIN assembly ON assembly.contig_bioentry_id=" +
					"bioentry_dbxref.bioentry_id WHERE hit_no<="+hit+" AND rank<="+hsp+" " +
					"AND assembly.run_id="+run_id+" GROUP BY bioentry_id ORDER BY dbxref_id, dbxref_startpos, dbxref_endpos";
		}
		
		int dbxref=0;
		int dbxref1=-1;
		
		LinkedList<Integer> starts = new LinkedList<Integer>();
		LinkedList<Integer> stops = new LinkedList<Integer>();
		int count=0;
		logger.debug(query);
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(query);
			while(set.next()){
				dbxref = set.getInt(1);
				if(dbxref1>-1 && dbxref != dbxref1){
					if(starts.size() == 1){
						total+=stops.get(0)-starts.get(0);
					}
					else{
						int[] ret = Tools_Array.getGapsAndCoverage(
								Tools_Array.ListInt2int(starts), 
							Tools_Array.ListInt2int(stops));
						total+=ret[0];
						overrep+= ret[1];
					}
					starts.clear();
					stops.clear();
				}
				starts.add(set.getInt(2));
				stops.add(set.getInt(3));
				dbxref1=dbxref;
				count++;
				if(count%100==0)System.out.print("\r"+count+"      ");
			}
			set.close();
			st.close();
			if(starts.size() == 1){
				total+=stops.get(0)-starts.get(0);
			}
			else if(starts.size() > 1){
				int[] ret = Tools_Array.getGapsAndCoverage(
						Tools_Array.ListInt2int(starts), 
						Tools_Array.ListInt2int(stops));
				total+=ret[0];
				overrep+= ret[1];
			}
			System.out.println("\r"+count+"  Done    ");
			return new int[]{total, overrep};
		}
		catch(SQLException sq){
			logger.error("Failed to execute " + query, sq);
		}
		return new int[]{-1,-1};
	}
	
	/**
	 * 
	 * @param manager
	 * @param contig_id bioentry_id of contig to use to find reads shared with this contig
	 * @param exclude_runs Exclude runs, ie Meta assemblies. If you don't exclude the meta assemblies
	 * the will be included in the final count (usually doubling the contig_id contig, but in the case
	 * of multiple different meta assemblies using these contigs this may screw everything up so
	 * exclude them)
	 * @return an array of int[2][n] where n is the number of contigs which share the same reads
	 * as contig given by contig_id. These are stored in int[0][n] and
	 *  ordered from highest to lowest and includes the given contig_id (at top presumably).
	 *  int[1][n] includes the number of reads the contig, represented at int[0][n], shares
	 *  with contig_id.
	 */
	public int[][] getSharedReads(DatabaseManager manager, int contig_id, int[] exclude_runs){
		String query = "SELECT assembly.contig_bioentry_id AS ID, COUNT(*) AS COUNT " +
				"FROM assembly WHERE read_bioentry_id IN " +
				"(SELECT assembly.read_bioentry_id FROM assembly WHERE assembly.contig_bioentry_id="+contig_id+") ";
		if(exclude_runs != null){
			for(int i =0; i < exclude_runs.length;i++){
				query+="AND run_id<>"+exclude_runs[i]+" ";
			}
		}
		query+=	"GROUP BY assembly.contig_bioentry_id ORDER BY COUNT DESC";
		try{
			Statement st = manager.getCon().createStatement();
			ResultSet set = st.executeQuery(query);
			LinkedList<Integer> ids = new LinkedList<Integer>();
			LinkedList<Integer> counts = new LinkedList<Integer>();
			while(set.next()){
				ids.add(set.getInt(1));
				counts.add(set.getInt(2));
			}
			set.close();
			st.close();
			return new int[][]{Tools_Array.ListInt2int(ids), Tools_Array.ListInt2int(counts)};
		}
		catch(SQLException sq){
			logger.error("Failed to run to following SQL for getSharedReads: "+query, sq);
		}
		return null;
	}
	
	public String[][] getListOfContigsfromMetaAssembly(DatabaseManager manager, int run_id){

		String[][] results = null;
		
		String query = "SELECT assembly.contig_bioentry_id, identifier, " +
				"biosequence.length, run2.program FROM assembly " +
				"INNER JOIN bioentry ON assembly.contig_bioentry_id" +
				"=bioentry.bioentry_id INNER JOIN biosequence ON " +
				"bioentry.bioentry_id=biosequence.bioentry_id " +
				"INNER JOIN bioentry_dbxref ON assembly.contig_bioentry_id" +
				"=bioentry_dbxref.bioentry_id INNER JOIN run ON run.run_id" +
				"=bioentry_dbxref.run_id INNER JOIN run AS run2 ON " +
				"run2.run_id=run.parent_id WHERE assembly.run_id="+run_id+" " +
				"GROUP BY bioentry.bioentry_id ORDER BY " +
				"bioentry_dbxref.run_id, biosequence.length DESC";
		//I know right?
		try{
			Statement st = manager.getCon().createStatement();
			ResultSet set = st.executeQuery(query);
			LinkedList<String> bioentrys = new LinkedList<String>();
			LinkedList<String> name = new LinkedList<String>();
			LinkedList<String> program = new LinkedList<String>();
			LinkedList<String> length = new LinkedList<String>();
			
			while(set.next()){
				bioentrys.add(set.getInt(1)+"");
				name.add(set.getString(2));
				program.add(set.getString(3));
				length.add(set.getString(4));
			}
			
			results = new String[4][bioentrys.size()];
			for(int i =0; i < bioentrys.size();i++){
				results[0][i]=bioentrys.get(i);
				results[1][i]=name.get(i);
				results[2][i]=program.get(i);
				results[3][i]=length.get(i);
			}
			
			set.close();
			st.close();
			
		}
		catch(SQLException sq){
			logger.error("Failed to run to following SQL (hold onto your butts): "+query, sq);
		}
		return results;
	}

}
