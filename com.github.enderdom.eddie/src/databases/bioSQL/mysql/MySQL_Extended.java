package databases.bioSQL.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import tools.Tools_Array;
import tools.Tools_String;
import tools.Tools_System;
import tools.bio.Tools_Contig;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;

/**
 * 
 * @author Dominic M. Wood
 *
 * Methods to sordid to go into BioSQL
 *
 */
public class MySQL_Extended implements BioSQLExtended{

	private int assemblyontid =-1;
	private int assemblytermid =-1;
	Logger logger = Logger.getRootLogger();
	ResultSet set;
	
	/******************************************************************/
	/* 
	 * NEW TABLES HERE 
	 */ 
	 /******************************************************************/
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
			Statement st = manager.getStatement();
			st.executeUpdate(info_table);
			st.executeUpdate(insert);
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create info table string ", se);
			return false;
		}
		
	}
	
	public boolean addBioEntrySynonymTable(DatabaseManager manager) {
		String table = "CREATE TABLE IF NOT EXISTS bioentry_synonym (" +
				"bioentry_synonym_id INT(10) UNSIGNED NOT NULL auto_increment," +
				"bioentry_id INT(10) UNSIGNED NOT NULL," +
			  	"identifier   	VARCHAR(40) BINARY, " +
				"PRIMARY KEY (bioentry_synonym_id)," +
			 	"UNIQUE (identifier)," +
			 	"PRIMARY KEY(bioentry_synonym_id)" +
			") TYPE=INNODB;";
		try{
			Statement st = manager.getStatement();
			st.executeUpdate(table);
			String key1 = "ALTER TABLE bioentry_synonym ADD CONSTRAINT FKbioentry_synonym"+
			"FOREIGN KEY (bioentry_id) REFERENCES bioentry(bioentry_id)";
			st.executeUpdate(key1);

			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create bioentry_synonym table", se);
			return false;
		}
	}
	
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
		logger.debug("Creating run table...");
		String table = "CREATE TABLE IF NOT EXISTS run (" +
				"run_id INT(10) UNSIGNED NOT NULL auto_increment, " +
				"bioentry_id INT(10) UNSIGNED NOT NULL, " +
				"run_date date NOT NULL, " +
			  	"program VARCHAR(40) BINARY, " +
			  	"dbname VARCHAR(40) BINARY, " +
			  	"params TEXT, " +
				"PRIMARY KEY (bioentry_id), " +
			 	"UNIQUE (run_id)" +
			") TYPE=INNODB;";
		try{
			Statement st = manager.getStatement();
			st.executeUpdate(table);
			logger.debug("Adding keys to database...");
			String key1 = "ALTER TABLE run ADD CONSTRAINT FKbioentry_run "+
			"FOREIGN KEY (bioentry_id) REFERENCES bioentry(bioentry_id) ON DELETE CASCADE;";
			st.executeUpdate(key1);
			//Note, this will fail if addBioentryDbxrefsCols() has not been called previously
			key1 = "ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKbioentry_dbxref_run "+
			"FOREIGN KEY (run_id) REFERENCES run(run_id) ON DELETE CASCADE;";
			st.executeUpdate(key1);
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to create run table", se);
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
	 * so it can be accessed from multiple locations by both eddie and any websites.
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
				"ALTER TABLE bioentry_dbxref ADD COLUMN (run_id INT);",
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
			Statement st = manager.getStatement();
			for(String s: alters)st.executeUpdate(s);
			return true;
		}
		catch(SQLException se){
			logger.error("Failed to alter bioentry_dbxref table", se);
			return false;
		}
	}
	
	
	/******************************************************************/
	/* 
	 * METHOD EXTENSIONS HERE 
	 */ 
	 /******************************************************************/
	public double getDatabaseVersion(DatabaseManager manager) {
		String g = "SELECT DatabaseVersion FROM info WHERE NUMB=1";
		try{
			Statement st = manager.getStatement();
			set = st.executeQuery(g);
			String r ="";
			while(set.next()){
				r = set.getString("DatabaseVersion");
			}
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
			Statement st = manager.getStatement();
			st.executeUpdate(insert);
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
			Statement st = manager.getStatement();
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				db = set.getInt("biodatabase_id");
			}
		}
		catch(SQLException se){
			logger.error("Failed to create biodatabase table", se);
		}
		return db;
	}
	

	
	public boolean setupAssembly(DatabaseManager manager){		
		try {
			Statement st = manager.getStatement();
			st.execute("CREATE INDEX bioentry_division ON bioentry(division);");
		} 
		catch (SQLException e) {
			logger.error("Error adding index to bioentry_division",e);
			return false;
		}
		if(addDefaultAssemblyOntology(manager)){
			return addDefaultAssemblyTerm(manager);
		}
		else{
			return false;
		}
	}
	
	public boolean addDefaultAssemblyOntology(DatabaseManager manager){
		this.assemblyontid=manager.getBioSQL().getOntology(manager.getCon(), ontology_name);
		if(this.assemblyontid<0){
			return manager.getBioSQL().addOntology(manager.getCon(), ontology_name, description) ?
					((this.assemblyontid=manager.getBioSQL().getOntology(manager.getCon(), ontology_name))> -1) : false;
		}
		else{
			return true;
		}
	}

	public boolean addDefaultAssemblyTerm(DatabaseManager manager){
		int ontology = getDefaultAssemblyOntology(manager);
		this.assemblytermid = manager.getBioSQL().getTerm(manager.getCon(), term_name_id, term_name_id);
		if(this.assemblytermid < 0){
			return manager.getBioSQL().addTerm(manager.getCon(), term_name_id, term_description, term_name_id, null,ontology) ?
					((this.assemblytermid=manager.getBioSQL().getTerm(manager.getCon(), term_name_id, term_name_id)) > -1) : false;
		}
		else{
			return true;
		}
	}
	
	public boolean addAssemblerTerm(DatabaseManager manager, String name, String division){
		int ontology = getDefaultAssemblyOntology(manager);
		int termid = manager.getBioSQL().getTerm(manager.getCon(),name, name);
		if(termid < 0){
			return manager.getBioSQL().addTerm(manager.getCon(), name, assemblerdescription, division, null,ontology) ?
					((termid=manager.getBioSQL().getTerm(manager.getCon(), name, name)) > -1) : false;
		}
		else{
			return true;
		}
	}
	
	public int getDefaultAssemblyOntology(DatabaseManager manager){
		if(this.assemblyontid == -1){
			if(!addDefaultAssemblyOntology(manager)){
				logger.error("Adding the Default Ontology term has failed");
			}
		}
		return this.assemblyontid;
	}

	public int getDefaultAssemblyTerm(DatabaseManager manager){
		if(this.assemblytermid == -1){
			if(!addDefaultAssemblyTerm(manager)){
				logger.error("Adding the Default Assembly Term has failed");
			}
		}
		return this.assemblytermid;
	}
	
	/* Returns the local name (ie from the ACE record, like Contig_1)
	 * and database identifier (ie CLCBio_Contig_0)
	 */
	
	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager manager, String division){
		HashMap<String, String> names = new HashMap<String, String>();
		try{
			Statement st = manager.getStatement();
			set = st.executeQuery("SELECT identifier, name FROM bioentry WHERE division='"+division+"'");
			while(set.next()){
				names.put(set.getString("name"),set.getString("identifier"));
			}
		}
		catch(SQLException sq){
			logger.error("Failure to retrieve contigname and identifier data");
		}
		return names;
	}
	
	public int[] getReads(DatabaseManager manager, int bioentry_id){
		LinkedList<Integer> ints = new LinkedList<Integer>();
		try{
			Statement st =manager.getStatement();
			set = st.executeQuery("SELECT subject_bioentry_id FROM bioentry_relationship WHERE object_bioentry_id="+bioentry_id);
			while(set.next()){
				ints.add(set.getInt("subject_bioentry_id"));
			}
			return Tools_Array.ListInt2int(ints);
		}
		catch(SQLException sq){
			logger.error("Failed to get Reads" , sq);
			return null;
		}
	}
	
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, String division){
		int l = -1;
		String r = "SELECT object_bioentry_id FROM bioentry_relationship INNER JOIN bioentry ON " +
		"bioentry_relationship.object_bioentry_id=bioentry.bioentry_id WHERE bioentry_relationship.subject_bioentry_id="+bioentry_id+
		" AND bioentry.division='"+division+"'";
		try{
			Statement st = manager.getStatement();
			set = st.executeQuery(r);
			while(set.next()){
				l = set.getInt(1);
			}
			return l;
		}
		catch(SQLException sq){
			logger.error("Failed to retrieve contig attached, SQL: " + r);
			return -2;
		}
	}
	
	public String[] getNamesFromTerm(DatabaseManager manager, String identifier){
		try{
			String[] info = new String[2];
			Statement st = manager.getStatement();
			set = st.executeQuery("SELECT name, definition FROM term WHERE identifier='"+identifier+"'");
			while(set.next()){
				info[0] = set.getString("name");
				info[1] = set.getString("definition");
			}
			return info;
		}
		catch(SQLException sq){
			logger.error("Failed to retrieve contig attached ");
			return null;
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
	public boolean setDbxref(DatabaseManager manager, int bioentry_id, int dbxref_id, int rank, Double evalue, Integer score, Integer dbxref_startpos,
		Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame){
		String sql = "INSERT INTO bioentry_dbxref (bioentry_id, dbxref_id, rank, evalue,score, dbxref_startpos,"+
			"dbxref_endpos, dbxref_frame, bioentry_startpos, bioentry_endpos, bioentry_frame) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement ment = manager.getCon().prepareStatement(sql);
			ment.setInt(1, bioentry_id);
			ment.setInt(2, dbxref_id);
			ment.setInt(3, rank);
			if(evalue != null) ment.setDouble(4, evalue);
			else ment.setNull(4, Types.DOUBLE);
			if(score != null) ment.setInt(5, score);
			else ment.setNull(5, Types.INTEGER);
			if(dbxref_startpos != null) ment.setInt(6, dbxref_startpos);
			else ment.setNull(6, Types.INTEGER);
			if(dbxref_endpos != null) ment.setInt(7, dbxref_endpos);
			else ment.setNull(7, Types.INTEGER);
			if(dbxref_frame != null) ment.setInt(8, dbxref_frame);
			else ment.setNull(8, Types.INTEGER);
			if(bioentry_startpos != null) ment.setInt(9, bioentry_startpos);
			else ment.setNull(9, Types.INTEGER);
			if(bioentry_endpos != null) ment.setInt(10, bioentry_endpos);
			else ment.setNull(10, Types.INTEGER);
			if(bioentry_frame != null) ment.setInt(11, bioentry_frame);
			else ment.setNull(11, Types.INTEGER);
			return ment.execute();
		} 
		catch (SQLException e) {
			logger.error("Failed to add bioentry_dbxref entry", e);
			return false;
		}
	}
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int rank){
		String sql = "SELECT COUNT(1) AS bool FROM bioentry_dbxref WHERE bioentry_id="+bioentry_id+" AND dbxref_id="+dbxref_id+" AND rank="+rank;
		try {
			PreparedStatement ment = manager.getCon().prepareStatement(sql);
			set = ment.executeQuery(sql);
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
	 * Currently as Reads are likely uploaded without padding
	 * the start and end values from ACE files will be off
	 * This is currently not a major issue, as I have not in depth application 
	 * that requires the exact positions.
	 * But this may lead to downstream issues if developed upon, 
	 * my considerations are to produce a CIGAR string-like for the differences between
	 * the read padded and unpadded. Altenatively the modified sequences could be 
	 * uploaded as a different version* ??? Though this would lead to a lot of excess
	 * data in the database
	 * 
	 * @param manager database manager
	 * @param contig_id to map to read, this should be a bioentry_id previously identified
	 * @param read_id to map to contig, this should be a bioentry_id previously identified
	 * @param programid 
	 * @param start of read alignment to contig
	 * @param stop where the read stops alignment
	 * @param strand
	 * 
	 * @return succesful or not
	 */
	
	public boolean mapRead2Contig(DatabaseManager manager, int contig_id, int read_id, int programid, int start, int stop, int strand){
		int term_id = getDefaultAssemblyTerm(manager);
		if(manager.getBioSQL().getBioEntryRelationship(manager.getCon(), contig_id, read_id, term_id) <0){
			if(!manager.getBioSQL().addBioEntryRelationship(manager.getCon(), contig_id, read_id, term_id, 0)){
				return false;
			}
		}
		int seqfeature_id = manager.getBioSQL().getSeqFeature(manager.getCon(), read_id, term_id, programid, 0);
		if(seqfeature_id < 0){
			if(!manager.getBioSQL().addSeqFeature(manager.getCon(), read_id, term_id, programid, assmbledread, 0)){
				return false;
			}
			seqfeature_id = manager.getBioSQL().getSeqFeature(manager.getCon(), read_id, term_id, programid, 0);
		}
		if(seqfeature_id < 0){
			logger.error("SeqFeature Id was not retrieved");
			return false;
		}
		else{
			return manager.getBioSQL().getLocation(manager.getCon(), seqfeature_id, 0) <0 ?
					manager.getBioSQL().addLocation(manager.getCon(), seqfeature_id, null, term_id, start, stop, strand, 0) : true ; 
		}
	}



}
