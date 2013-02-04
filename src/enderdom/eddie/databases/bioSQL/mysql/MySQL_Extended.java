package enderdom.eddie.databases.bioSQL.mysql;

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
import enderdom.eddie.tools.bio.Tools_Contig;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.bioSQL.psuedoORM.BioSequence;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;

/**
 * 
 * @author Dominic M. Wood
 *
 * Methods to sordid to go into BioSQL
 *
 */
public class MySQL_Extended implements BioSQLExtended{

	Logger logger = Logger.getRootLogger();
	ResultSet set;
	
	//GETS
	PreparedStatement dbxrefGET;
	PreparedStatement readFromContigGET;
	PreparedStatement contigFromReadGET;
	PreparedStatement bioSequenceGET;
	
	//SETS
	PreparedStatement assemblySET;
	PreparedStatement runSET;
	
	//EXISTS
	PreparedStatement dbxrefEXIST;
	
	/******************************************************************/
	/* 
	 * NEW TABLES HERE 
	 */ 
	 /******************************************************************/
	
	/**
	 * Table for compatibility with previous versions of db
	 * 
	 * Yes I know storing versioning as a varchar is a bit of
	 * a wtf.
	 */
	public boolean addLegacyVersionTable(DatabaseManager manager, String version, String dbversion) {
		String info_table = "CREATE TABLE IF NOT EXISTS info ("+
		  "Numb INT(11) NOT NULL auto_increment,"+
		  "DerocerasVersion VARCHAR(30),"+
		  "DatabaseVersion VARCHAR(30),"+
		  "LastRevision DATE,"+
		  "PRIMARY KEY (Numb)) TYPE=INNODB;";
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
	
//	public boolean addBioEntrySynonymTable(DatabaseManager manager) {
//		String table = "CREATE TABLE IF NOT EXISTS bioentry_synonym (" +
//				"bioentry_synonym_id INT(10) UNSIGNED NOT NULL auto_increment," +
//				"bioentry_id INT(10) UNSIGNED NOT NULL," +
//			  	"identifier   	VARCHAR(40) BINARY, " +
//				"PRIMARY KEY (bioentry_synonym_id)," +
//			 	"UNIQUE (identifier)," +
//			 	"PRIMARY KEY(bioentry_synonym_id)" +
//			") TYPE=INNODB;";
//		try{
//			Statement st = manager.getStatement();
//			st.executeUpdate(table);
//			String key1 = "ALTER TABLE bioentry_synonym ADD CONSTRAINT FKbioentry_synonym"+
//			"FOREIGN KEY (bioentry_id) REFERENCES bioentry(bioentry_id)";
//			st.executeUpdate(key1);
//
//			return true;
//		}
//		catch(SQLException se){
//			logger.error("Failed to create bioentry_synonym table", se);
//			return false;
//		}
//	}
	
	/**
	 * This table is supposed to store data of the specifics
	 * of the program used on a specific bioentry which can then
	 * be linked to tables like bioentry_dbxref. This is so the specifics of a run like
	 * interpro scan or blast can be saved (like date, which is super important
	 * as is need when citing)
	 * 
	 * 
	 * @param manager
	 * @return successfully created table
	 */
	public boolean addRunTable(DatabaseManager manager) {
		//Should be run after bioentry_dbxref mods
		logger.debug("Creating "+runtable+" table...");
		String table = "CREATE TABLE IF NOT EXISTS "+runtable+" (" +
				"run_id INT(10) UNSIGNED NOT NULL auto_increment, " +
				"run_date date NOT NULL, " +
				"runtype VARCHAR(20) BINARY NOT NULL, " +
			  	"program VARCHAR(40) BINARY NOT NULL, " +
			  	"version VARCHAR(40) BINARY, " +
			  	"dbname VARCHAR(40) BINARY, " +
			  	"params TEXT, " +
			  	"comment TEXT, " +
			 	"PRIMARY KEY (run_id)" +
			 	") TYPE=INNODB;";
		try{
			Statement st = manager.getCon().createStatement();
			st.executeUpdate(table);
			//Note, this will fail if addBioentryDbxrefsCols() has not been called previously
			String key1 = "ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKbioentry_dbxref_run "+
			"FOREIGN KEY (run_id) REFERENCES "+runtable+"(run_id) ON DELETE CASCADE;";
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
	 * After several different attempts at jamming assembly data 
	 * into the biosql database, this is my latest attempt.
	 * 
	 * Originally used a convoluted method of using pseudo terms & ontologies
	 * to style an assembly, but gave up and just created a new table, 
	 * this may break the database more, but its just a lot simpler.
	 * 
	 * So contig is contig_bioentry_id and read is read_bioentry_id
	 * version is the version of biosequence to use, so basically
	 * you should upload the aligned trimmed read as another 'version'
	 * of the read. This will mean you will have to check available version
	 * numbers, or alternative link a version to an assembly. 
	 * 
	 * As it uses run_id, the run id table should be created first
	 * 
	 * @param manager
	 * @return boolean succesfully created table
	 */
	public boolean addAssemblyTable(DatabaseManager manager){
		logger.debug("Creating assembly table...");
		String table = "CREATE TABLE IF NOT EXISTS assembly (" +
			"contig_bioentry_id INT(10) UNSIGNED NOT NULL,"+
			"read_bioentry_id INT(10) UNSIGNED NOT NULL,"+
			"read_version SMALLINT,"+
			"run_id INT(10) UNSIGNED NOT NULL,"+
			"trimmed TINYINT,"+ //0 == not trimmed
			"range_start INT(10),"+ //If trimmed this should just be the offset
			"range_end INT(10)"+
			")TYPE=INNODB;";
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
	
	
	/**
	 * This table alteration was added to hold more detailed information
	 * about the relationship between the external database reference and the
	 * bioentry. Without this there is now way of ascertaining how strong or weak the
	 * link between these two is. I have read several blogs lambasting the wholesale uploading of
	 * xml into databases, so I realise that recreating the flat file data in the database
	 * may be a bad mistake. However I see no alternative method or storing this data
	 * so it can be accessed from multiple locations by both eddie and any websites easily.
	 * 
	 * I have not included the hit number in this. For two reasons, one is that without the specifics
	 * of the blast run from perspective of the database are not known (for now). The second is that 
	 * there is no actual need for this. Uploaded hsps as multiple rows and just using score or e-value
	 * for ordering should suffice for recreating the pertinant data, IMHO.
	 * 
	 * @param manager
	 * 
	 * @return whether or not it succeeded
	 */
	public boolean addBioentryDbxrefCols(DatabaseManager manager) {
		String alters[] = new String[]{
				"ALTER TABLE bioentry_dbxref ADD COLUMN (run_id INT(10) UNSIGNED NOT NULL);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (evalue DOUBLE PRECISION);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (score MEDIUMINT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_startpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_endpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (dbxref_frame TINYINT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_startpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_endpos INT);",
				"ALTER TABLE bioentry_dbxref ADD COLUMN (bioentry_frame TINYINT);",
				"CREATE INDEX bioentry_dbxref_evalue ON bioentry_dbxref(evalue);"
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
		}
		catch(SQLException sq){
			logger.error("Failed to run getUniqueFields query");
		}
		return ret;
	}
	
	public double getDatabaseVersion(DatabaseManager manager) {
		String g = "SELECT DatabaseVersion FROM info WHERE NUMB=1";
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(g);
			String r ="";
			while(set.next()){
				r = set.getString("DatabaseVersion");
			}
			st.close();
			Double b = null;
			if(r.startsWith("v")){
				b = Tools_String.parseString2Double(r.substring(1));
			}
			if(b == null){
				return -1;
			}
			else{
				return b;
			}
		}
		catch(SQLException sq){
			logger.error("Failed to retrieve database index", sq);
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
			st.close();
		}
		catch(SQLException se){
			logger.error("Failed to get Eddie ID from database", se);
		}
		return db;
	}

	
	/**
	 * Returns the local name (ie from the ACE record, like Contig_1)
	 * and database identifier (ie CLCBio_Contig_0)
	 */
	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager manager, int run_id){
		HashMap<String, String> names = new HashMap<String, String>();
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery("SELECT identifier, name FROM bioentry INNER JOIN assembly ON bioentry.bioentry_id=assembly.contig_bioentry_id WHERE run_id="+run_id);
			while(set.next()){
				names.put(set.getString("name"),set.getString("identifier"));
			}
			st.close();
		}
		catch(SQLException sq){
			logger.error("Failure to retrieve contigname and identifier data", sq);
		}
		return names;
	}
	
	public int[] getReads(DatabaseManager manager, int bioentry_id){
		String sql = new String("SELECT read_bioentry_id FROM assembly WHERE contig_bioentry_id=?");
		LinkedList<Integer> values = new LinkedList<Integer>();
		try{
			readFromContigGET = MySQL_BioSQL.init(manager.getCon(), readFromContigGET, sql);
			readFromContigGET.setInt(1,bioentry_id);
			set = readFromContigGET.executeQuery();
			while(set.next()){
				values.add(set.getInt(1));
			}
			return Tools_Array.ListInt2int(values);
		}
		catch(SQLException sq){
			logger.error("Failed to get reads using contig id", sq);
			return null;
		}		
	}
	
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, int run_id){
		String sql = new String("SELECT contig_bioentry_id FROM assembly WHERE read_bioentry_id=? AND run_id=?");
		try{
			contigFromReadGET = MySQL_BioSQL.init(manager.getCon(), contigFromReadGET, sql);
			contigFromReadGET.setInt(1,bioentry_id);
			contigFromReadGET.setInt(2,run_id);
			set = contigFromReadGET.executeQuery();
			while(set.next()){
				return set.getInt(1);
			}
			return -1;
		}
		catch(SQLException sq){
			logger.error("Failed to get reads using contig id", sq);
			return -1;
		}		
	}
	
	public int getBioEntryId(DatabaseManager manager, String name, boolean fuzzy, int biodatabase_id){
		int entry =-1;
		entry = manager.getBioSQL().getBioEntry(manager.getCon(), name, name, biodatabase_id);
		if(entry == -1){
			entry = manager.getBioSQL().getBioEntrywName(manager.getCon(), name);
		}
		//This could lead to unexpected results, probably 
		//should be removed if to be used by anyone other than me
		if(entry == -1 && fuzzy){
			logger.debug("Could not get id with "+name);
			String[] s = Tools_Contig.stripContig(name);
			if(s == null)return entry;
			else{
				for(String sa : s){
					entry = manager.getBioSQL().getBioEntrywName(manager.getCon(), sa);
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
			logger.debug("Entry retrieved without fuzziness required");
		}
		return entry;
	}
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int run_id, int rank){
		
		String sql = "SELECT COUNT(1) AS bool FROM bioentry_dbxref WHERE bioentry_id=? AND dbxref_id=? AND run_id=? AND rank=?";
		try {
			dbxrefEXIST = MySQL_BioSQL.init(manager.getCon(), dbxrefEXIST, sql);
			dbxrefEXIST.setInt(1, bioentry_id);
			dbxrefEXIST.setInt(2, dbxref_id);
			dbxrefEXIST.setInt(3, run_id);
			dbxrefEXIST.setInt(4, rank);
			set = dbxrefEXIST.executeQuery();
			while(set.next()){
				return (set.getInt(1) > 0);
			}
		} 
		catch (SQLException e) {
			logger.error("Failed to add bioentry_dbxref entry", e);
		}
		return false;
	}
	
	/** INDEV function
	 * 
	 * 
	 * @param manager database manager
	 * @param contig_id to map to read, this should be a bioentry_id previously identified
	 * @param read_id to map to contig, this should be a bioentry_id previously identified
	 * @param runid
	 * @param offset, where the read  
	 * @param start of read alignment to contig relative to read, ie 4 = offset+4 for alignment
	 * @param stop where the read stops alignment
	 * @param trimmed whether or not the read was trimmed
	 * 
	 * @return successful or not
	 */
	public boolean mapRead2Contig(DatabaseManager manager, int contig_id, int read_id, int read_version, int runid, int start, int stop, boolean trimmed){
		String sql = "INSERT INTO assembly (contig_bioentry_id, read_bioentry_id, read_version, run_id, trimmed, range_start, range_end) VALUES (?,?,?,?,?,?,?)";
		try {
			assemblySET = MySQL_BioSQL.init(manager.getCon(), assemblySET, sql);
			assemblySET.setInt(1, contig_id);
			assemblySET.setInt(2, read_id);
			assemblySET.setInt(3, read_version);
			assemblySET.setInt(4, runid);
			if(trimmed) assemblySET.setInt(5, 1); 
			else assemblySET.setInt(5, 0);
			assemblySET.setInt(6, start);
			assemblySET.setInt(7, stop);
			assemblySET.execute();
			return true;
		} 
		catch(SQLException e){
			logger.error("Failed to insert assembly data into database", e);
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
		String sql = new String("SELECT run_id FROM run WHERE program LIKE '"+programname+"' AND runtype='"+runtype+"'");
		LinkedList<Integer> ins = new LinkedList<Integer>();
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			while(set.next()){
				ins.add(set.getInt(1));
			}
			st.close();
			return Tools_Array.ListInt2int(ins);
		}
		catch(SQLException sq){
			logger.error("Failed to get run id." , sq);
			return new int[]{-1};
		}
	}

	public Run getRun(DatabaseManager manager, int run_id){
		String sql = new String("SELECT run_date, runtype, program, version, dbname,params, comment FROM run WHERE run_id="+run_id);
		try{
			Statement st = manager.getCon().createStatement();
			set = st.executeQuery(sql);
			while(set.next()){
				return new Run(run_id, set.getDate(1),set.getString(2), set.getString(3), set.getString(4), set.getString(5), set.getString(6),set.getString(7));
			}
			return null;
		}
		catch(SQLException sq){
			logger.error("Failed to get run id." , sq);
			return null;
		}
	}
	
	public BioSequence[] getBioSequences(DatabaseManager manager, int bioentry_id){
		String sql = "SELECT version, length, alphabet, seq FROM biosequence WHERE bioentry_id=?";
		LinkedList<BioSequence> biosequences = new LinkedList<BioSequence>();
		try {
			bioSequenceGET = MySQL_BioSQL.init(manager.getCon(), bioSequenceGET, sql);
			bioSequenceGET.setInt(1, bioentry_id);
			set = bioSequenceGET.executeQuery();
			while(set.next()){
				biosequences.add(new BioSequence(bioentry_id, set.getInt(1), set.getInt(2), set.getString(3), set.getString(4)));
			}
			return biosequences.toArray(new BioSequence[0]);
		} 
		catch(SQLException e){
			logger.error("Failed to insert assembly data into database", e);
			return null;
		}
	}
	
	/* *****************************************************************************************
	 * 
	 * 
	 *										INSERTS
	 * 
	 * 
	 *******************************************************************************************/
	
	/**
	 * @see #addBioentryDbxrefCols(Connection)
	 * 	 
	 * rank is can actually be set as null, but as this will
	 * often be use for blast hsp references, i am using it to denote
	 * mutliple hsps between the same bioentry and dbx
	 * 
	 * @param con SQL Connection
	 * @param bioentry_id
	 * @param dbxref_id
	 * @param rank
	 * @param evalue
	 * @param score
	 * @param dbxref_startpos
	 * @param dbxref_endpos
	 * @param dbxref_frame
	 * @param bioentry_startpos
	 * @param bioentry_endpos
	 * @param bioentry_frame
	 * @return boolean , false if execute failed or sql exception thrown 
	 */
	public boolean setDbxref(DatabaseManager manager, int bioentry_id, int run_id, int dbxref_id, int rank, Double evalue, Integer score, Integer dbxref_startpos,
		Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame){
		String sql = "INSERT INTO bioentry_dbxref (bioentry_id, dbxref_id, run_id, rank, evalue,score, dbxref_startpos,"+
			"dbxref_endpos, dbxref_frame, bioentry_startpos, bioentry_endpos, bioentry_frame) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			dbxrefGET = MySQL_BioSQL.init(manager.getCon(), dbxrefGET, sql);
			dbxrefGET.setInt(1, bioentry_id);
			dbxrefGET.setInt(2, dbxref_id);
			dbxrefGET.setInt(3, run_id);
			dbxrefGET.setInt(4, rank);
			
			if(evalue != null) dbxrefGET.setDouble(5, evalue);
			else dbxrefGET.setNull(5, Types.DOUBLE);
			if(score != null) dbxrefGET.setInt(6, score);
			else dbxrefGET.setNull(6, Types.INTEGER);
			if(dbxref_startpos != null) dbxrefGET.setInt(7, dbxref_startpos);
			else dbxrefGET.setNull(7, Types.INTEGER);
			if(dbxref_endpos != null) dbxrefGET.setInt(8, dbxref_endpos);
			else dbxrefGET.setNull(8, Types.INTEGER);
			if(dbxref_frame != null) dbxrefGET.setInt(9, dbxref_frame);
			else dbxrefGET.setNull(9, Types.INTEGER);
			if(bioentry_startpos != null) dbxrefGET.setInt(10, bioentry_startpos);
			else dbxrefGET.setNull(10, Types.INTEGER);
			if(bioentry_endpos != null) dbxrefGET.setInt(11, bioentry_endpos);
			else dbxrefGET.setNull(11, Types.INTEGER);
			if(bioentry_frame != null) dbxrefGET.setInt(12, bioentry_frame);
			else dbxrefGET.setNull(12, Types.INTEGER);
			dbxrefGET.execute();
			return true;
		} 
		catch (SQLException e) {
			logger.error("Failed to add bioentry_dbxref entry", e);
			return false;
		}
	}

	
	public boolean setRun(DatabaseManager manager, Date date, String runtype, String program, String version, String dbname, String params, String comment){		
		try{
			runSET = MySQL_BioSQL.init(manager.getCon(), runSET, "INSERT INTO run (run_date, runtype, program, version, dbname, params, comment) VALUES (?,?,?,?,?,?,?);");
			runSET.setDate(1, date);
			runSET.setString(2, runtype);
			runSET.setString(3, program);
			if(version == null)runSET.setNull(4, Types.VARCHAR);
			else runSET.setString(4, version);
			if(dbname == null)runSET.setNull(5, Types.VARCHAR);
			else runSET.setString(5, dbname);
			if(params == null)runSET.setNull(6, Types.VARCHAR);
			else runSET.setString(6, params);
			if(comment == null)runSET.setNull(7, Types.VARCHAR);
			else runSET.setString(7, comment);
			
			logger.trace("Attempting to execute " + runSET.toString());
			runSET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to insert run", sq);
			return false;
		}
	}

}
