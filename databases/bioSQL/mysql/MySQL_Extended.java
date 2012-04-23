package databases.bioSQL.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tools.Tools_String;
import tools.Tools_System;
import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;

public class MySQL_Extended implements BioSQLExtended{

	private int assemblyontid =-1;
	private int assemblytermid =-1;
	
	public double getDatabaseVersion(Connection con) {
		String g = "SELECT DatabaseVersion FROM info WHERE NUMB=1";
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(g);
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
			Logger.getRootLogger().error("Failed to retrieve database index", sq);
			return -1;
		}
	}
	
	public boolean addEddie2Database(Connection con) {
		String insert = new String("INSERT INTO biodatabase (name, authority, description) VALUES ('"+programname+"', '"+authority+"', '"+description+"')");
		try{
			Statement st = con.createStatement();
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create biodatabase table", se);
			return false;
		}
	}
	
	public int getEddieFromDatabase(Connection con){
		String insert = new String("SELECT biodatabase_id FROM biodatabase WHERE name='"+programname+"';");
		int db =-1;
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				db = set.getInt("biodatabase_id");
			}
			st.close();
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create biodatabase table", se);
		}
		return db;
	}
	
	public boolean addLegacyVersionTable(Connection con, String version, String dbversion) {
		String info_table = "CREATE TABLE IF NOT EXISTS info ("+
		  "Numb INT(11) NOT NULL auto_increment,"+
		  "DerocerasVersion VARCHAR(30),"+
		  "DatabaseVersion VARCHAR(30),"+
		  "LastRevision DATE,"+
		  "PRIMARY KEY (Numb)) TYPE=INNODB;";
		String insert = new String("INSERT INTO info (Numb, DerocerasVersion, DatabaseVersion, LastRevision) VALUES (1, 'v"+version+"', 'v"+dbversion+"', '"+Tools_System.getDateNow("yyyy-MM-dd")+"')"+
				" ON DUPLICATE KEY UPDATE DerocerasVersion='v"+version+"', DatabaseVersion='v"+dbversion+"', LastRevision='"+Tools_System.getDateNow("yyyy-MM-dd")+"' ;");
		try{
			Statement st = con.createStatement();
			st.executeUpdate(info_table);
			st.executeUpdate(insert);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create info table string ", se);
			return false;
		}
		
	}

	public boolean addBioEntrySynonymTable(Connection con) {
		String table = "CREATE TABLE bioentry_synonym (" +
				"bioentry_synonym_id INT(10) UNSIGNED NOT NULL auto_increment," +
				"bioentry_id	    INT(10) UNSIGNED NOT NULL," +
			  	"identifier   	VARCHAR(40) BINARY, " +
				"PRIMARY KEY (bioentry_synonym_id)," +
			 	"UNIQUE (identifier)" +
			") TYPE=INNODB;";
		try{
			Statement st = con.createStatement();
			st.executeUpdate(table);
			st.close();
			return true;
		}
		catch(SQLException se){
			Logger.getRootLogger().error("Failed to create bioentry_synonym table", se);
			return false;
		}
	}
	
	public boolean setupAssembly(BioSQL boss, Connection con){
		if(addAssemblyOntology(boss, con)){
			return addAssemblyTerm(boss, con);
		}
		else{
			return false;
		}
	}
	
	public boolean addAssemblyOntology(BioSQL boss, Connection con){
		this.assemblyontid=boss.getOntology(con, ontology_name);
		if(this.assemblyontid<0){
			return boss.addOntology(con, ontology_name, description) ?
					((this.assemblyontid=boss.getOntology(con, ontology_name))> -1) : false;
		}
		else{
			return true;
		}
	}

	public boolean addAssemblyTerm(BioSQL boss, Connection con){
		int ontology = this.getDefaultAssemblyOntology(boss, con);
		this.assemblytermid = boss.getTerm(con, term_name, term_name);
		if(this.assemblytermid < 0){
			return boss.addTerm(con, term_name, ontology_name, term_name, null,ontology) ?
					((this.assemblytermid=boss.getTerm(con, term_name, term_name)) > -1) : false;
		}
		else{
			return true;
		}
	}
	
	public boolean addAssemblerTerm(BioSQL boss, Connection con, String name){
		int ontology = this.getDefaultAssemblyOntology(boss, con);
		int termid = boss.getTerm(con, name, name);
		if(termid < 0){
			return boss.addTerm(con, name, assemblerdescription, name, null,ontology) ?
					((this.assemblytermid=boss.getTerm(con, name, name)) > -1) : false;
		}
		else{
			return true;
		}
	}
	
	public int getDefaultAssemblyOntology(BioSQL boss, Connection con){
		if(this.assemblyontid == -1){
			this.assemblyontid = boss.getOntology(con, ontology_name);
		}
		return this.assemblyontid;
	}

	public int getDefaultAssemblyTerm(BioSQL boss, Connection con){
		if(this.assemblytermid == -1){
			this.assemblytermid = boss.getTerm(con, term_name, term_name);
		}
		return this.assemblytermid;
	}
	
	
	/* INDEV function
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
	 */
	public boolean mapRead2Contig(Connection con, BioSQL boss, int contig_id, int read_id, int programid, int start, int stop, int strand){
		int term_id = this.getDefaultAssemblyTerm(boss, con);
		boss.addBioEntryRelationship(con, contig_id, read_id, term_id, 0);
		boss.addSeqFeature(con, read_id, term_id, programid, assmbledread, 0);
		int seqfeature_id = boss.getSeqFeature(con, read_id, term_id, programid, 0);
		if(seqfeature_id < 0)return false;
		else return boss.addLocation(con, seqfeature_id, null, term_id, start, stop, strand, 0);
	}

}
