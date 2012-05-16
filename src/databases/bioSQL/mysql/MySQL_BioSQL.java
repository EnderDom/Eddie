package databases.bioSQL.mysql;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.BioSQL;

/**
 * @author Dominic Wood
 */

public class MySQL_BioSQL implements BioSQL{

	private Logger logger = Logger.getRootLogger();
	private static String mysqldb = "biosqldb-mysql.sql";
	
	//Sets
	private PreparedStatement BioEntrySET;
	private PreparedStatement BioSequenceSET;
	private PreparedStatement BioEntryRelationshipSET;
	private PreparedStatement SeqFeatureSET;
	private PreparedStatement LocationSET;
	//Gets
	private PreparedStatement BioEntryGET1;
	private PreparedStatement BioEntryGET2;
	private PreparedStatement SeqfeatureGET;
	private PreparedStatement BioEntryRelationshipGET;
	private PreparedStatement LocationGET;
	
	private ResultSet set; 
	
	
	public void clearStatements(){
		BioEntrySET = null;
		BioSequenceSET = null;
		SeqFeatureSET = null;
		BioEntryRelationshipSET = null;
		BioEntryGET1 = null;
		BioEntryGET2 = null;
		SeqfeatureGET = null;
		BioEntryRelationshipGET = null;
		LocationGET = null;
	}
	
	public PreparedStatement init(Connection con, PreparedStatement ment, String sql) throws SQLException{
		if(ment == null || ment.isClosed()){
			ment = con.prepareStatement(sql);
		}
		return ment;
	}
	
	public boolean addBiosequence(Connection con, Integer version, Integer length, String alphabet, String seq,  int bioentry_id) {
		try{
			BioSequenceSET = init(con, BioSequenceSET, "INSERT INTO biosequence (bioentry_id, version, length, alphabet, seq) VALUES (?,?,?,?,?)");
			BioSequenceSET.setInt(1, bioentry_id);
			if(version == null)BioSequenceSET.setNull(2, Types.INTEGER) ;
			else BioSequenceSET.setInt(2, version);
			if(length == null)BioSequenceSET.setNull(3, Types.INTEGER) ;
			else BioSequenceSET.setInt(3, length);
			if(alphabet == null)BioSequenceSET.setNull(4, Types.VARCHAR) ;
			else BioSequenceSET.setString(4, alphabet);
			if(seq == null)BioSequenceSET.setNull(5, Types.VARCHAR) ;
			else BioSequenceSET.setString(5, seq);
			BioSequenceSET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add BioSequence", sq);
			return false;
		}
		
	}
	
	public  boolean addBioEntry(Connection con, int biodatabase, Integer taxon_id, String name, String accession, String identifier, String division, String description, int version) {
		try{
			BioEntrySET = init(con, BioEntrySET, "INSERT INTO bioentry (biodatabase_id, taxon_id, name, accession, identifier, division, description, version) VALUES (?,?,?,?,?,?,?,?)");
			
			//Values that cannot be null
			BioEntrySET.setInt(1, biodatabase); //<- Huh? PreparedStatement uses 1-based indexing???
			BioEntrySET.setString(3, name);
			BioEntrySET.setString(4, accession);
			BioEntrySET.setInt(8, version);
			
			//Values that can be null:
			if(taxon_id == null)BioEntrySET.setNull(2, Types.INTEGER);
			else BioEntrySET.setInt(2, taxon_id);
			if(identifier == null)BioEntrySET.setNull(5, Types.VARCHAR);
			else BioEntrySET.setString(5, identifier); 
			if(division == null)BioEntrySET.setNull(6, Types.VARCHAR);
			else BioEntrySET.setString(6, division);
			if(description == null) BioEntrySET.setNull(7, Types.VARCHAR);
			else BioEntrySET.setString(7, description);
			BioEntrySET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence ", sq);
			return false;
		}
	}
	
	public boolean addSequence(Connection con, int biodatabase, Integer taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet){
		boolean added = addBioEntry(con, biodatabase, taxon_id, name, accession, identifier, division, description, version);
		if(added){
			int bio_entry = getBioEntry(con, identifier, accession, biodatabase);
			if(bio_entry != -1){
				return addBiosequence(con, version, seq.length(), alphabet, seq, bio_entry);
			}
			else{
				logger.error("Failed to return sequence added");
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public boolean addOntology(Connection con, String name, String definition){
		try{
			PreparedStatement st = con.prepareStatement("INSERT INTO ontology (name, definition) VALUES (?,?)");
			st.setString(1, name);
			st.setString(2, definition);
			st.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Ontology with name " + name, sq);
			return false;
		}
	}

	public boolean addTerm(Connection con, String name, String definition, String identifier, Character is_obsolete, int ontology_id){
		try{
			PreparedStatement st = con.prepareStatement("INSERT INTO term (name, definition, identifier, is_obsolete, ontology_id) VALUES (?,?,?,?,?)");
			st.setString(1, name);
			if(definition == null)st.setNull(2, Types.VARCHAR);
			else st.setString(2, definition);
			if(identifier == null)st.setNull(3, Types.VARCHAR);
			else st.setString(3, identifier);
			if(is_obsolete == null)st.setNull(4, Types.CHAR);
			else st.setString(4, new String(is_obsolete.toString()));
			st.setInt(5, ontology_id);
			st.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Ontology with name " + name, sq);
			return false;
		}
	}
	
	public boolean addBioEntryRelationship(Connection con, int object_bioentry_id, int subject_bioentry_id, int term_id, Integer rank){
		try{
			BioEntryRelationshipSET = init(con, BioEntryRelationshipSET, "INSERT INTO bioentry_relationship (object_bioentry_id, subject_bioentry_id, term_id, rank) VALUES (?,?,?,?)");
			BioEntryRelationshipSET.setInt(1, object_bioentry_id);
			BioEntryRelationshipSET.setInt(2, subject_bioentry_id);
			BioEntryRelationshipSET.setInt(3, term_id);
			if(rank == null) BioEntryRelationshipSET.setNull(4, Types.INTEGER);
			else BioEntryRelationshipSET.setInt(4, rank);
			BioEntryRelationshipSET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Entry_Relationship " + BioEntryRelationshipSET.toString(), sq);
			return false;
		}
	}
	
	public boolean addSeqFeature(Connection con, int bioentry_id, int type_term_id, int source_term_id, String display_name, int rank){
		try{
			SeqFeatureSET = init(con, SeqFeatureSET, "INSERT INTO seqfeature (bioentry_id, type_term_id, source_term_id, display_name, rank) VALUES (?,?,?,?,?)");
			SeqFeatureSET.setInt(1, bioentry_id);
			SeqFeatureSET.setInt(2, type_term_id);
			SeqFeatureSET.setInt(3, source_term_id);
			if(display_name == null)SeqFeatureSET.setNull(4, Types.VARCHAR);
			else SeqFeatureSET.setString(4, display_name);
			SeqFeatureSET.setInt(5, rank);
			SeqFeatureSET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add seqfeature "+SeqFeatureSET.toString(), sq);
			return false;
		}
	}
	
	/*
	
	*/
	public boolean addLocation(Connection con, int seqfeature_id, Integer dbxref_id, Integer term_id, Integer start_pos, Integer stop_pos, int strand, int rank){
		try{
			LocationSET = init(con, LocationSET, "INSERT INTO location (seqfeature_id,dbxref_id,term_id,start_pos,end_pos,strand,rank) VALUES (?,?,?,?,?,?,?)");
			LocationSET.setInt(1, seqfeature_id);
			if(dbxref_id == null)LocationSET.setNull(2, Types.INTEGER);
			else LocationSET.setInt(2, dbxref_id);
			if(term_id == null)LocationSET.setNull(3, Types.INTEGER);
			else LocationSET.setInt(3, term_id);
			if(start_pos == null)LocationSET.setNull(4, Types.INTEGER);
			else LocationSET.setInt(4, start_pos);
			if(stop_pos == null)LocationSET.setNull(5, Types.INTEGER);
			else LocationSET.setInt(5, stop_pos);
			LocationSET.setInt(6, strand);
			LocationSET.setInt(7, rank);
			LocationSET.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add location ", sq);
			return false;
		}
	}
	
	/*******************************************************************/
	/*																   */
	/*																   */
	/*							Get Methods					   */
	/*																   */
	/*																   */
	/*******************************************************************/

	public String[] getBioEntryNames(Connection con, int bioentry_id){
		try{
			String[] names = new String[3];
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery("SELECT name, accession, identifier FROM bioentry WHERE bioentry_id="+bioentry_id);
			while(set.next()){
				names[0]=set.getString("name");
				names[1]=set.getString("accession");
				names[2]=set.getString("identifier");
			}
			st.close();
			return names;
		}
		catch(SQLException sq){
			logger.error("Failed to get BioEntry with bioentry id " + bioentry_id, sq);
			return null;
		}
	}
	
	public int getBioEntry(Connection con, String identifier, String accession, int biodatabase){
		PreparedStatement stmt = null;
				
		int entry =-1;
		try{
			BioEntryGET1 = init(con, BioEntryGET1, "SELECT bioentry_id FROM bioentry WHERE identifier=? AND biodatabase_id=?");
			BioEntryGET2 = init(con, BioEntryGET2, "SELECT bioentry_id FROM bioentry WHERE accession=? AND biodatabase_id=?");
			
			if(identifier == null){
				stmt = BioEntryGET2;
				identifier = accession;
			}
			else{
				stmt = BioEntryGET1;
			}
			stmt.setString(1, identifier);
			stmt.setInt(2, biodatabase);
			set = stmt.executeQuery();
			while(set.next()){
				entry = set.getInt("bioentry_id");
			}
			if(entry == -1 && accession != null){
				stmt = BioEntryGET2;
				stmt.setString(1, accession);			
				stmt.setInt(2, biodatabase);
				set = stmt.executeQuery();
				while(set.next()){
					entry = set.getInt("bioentry_id");
				}
			}
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence " +stmt.toString(), sq);
		}
		return entry;
	}
	
	public int getBioEntrywName(Connection con, String name){
		int entry =-1;
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery("SELECT id FROM bioentry WHERE name='"+name+"'");
			while(set.next()){
				entry = set.getInt("id");
				if(set.next()) logger.warn("Other ids are available with this name " + name);
				break;
			}
		}
		catch(SQLException sq){
			logger.error("Failed to get bioentry id with name: " +name, sq);
		}
		return entry;
	}
	
	public int getBioEntryRelationship(Connection con, int bioentry_object_id, int bioentry_subject_id, int term_id){

		int entry =-1;
		try{
			BioEntryRelationshipGET = init(con, BioEntryRelationshipGET, "SELECT bioentry_relationship_id FROM bioentry_relationship WHERE object_bioentry_id=? AND subject_bioentry_id=? AND term_id=?");		
			
			BioEntryRelationshipGET.setInt(1, bioentry_object_id);
			BioEntryRelationshipGET.setInt(2, bioentry_subject_id);
			BioEntryRelationshipGET.setInt(3, term_id);
			
			set = BioEntryRelationshipGET.executeQuery();
			while(set.next()){
				entry = set.getInt("bioentry_relationship_id");
			}
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence " +BioEntryRelationshipGET.toString(), sq);			
		}
		return entry;
	}
	
	public int getOntology(Connection con, String name){
		try{
			int id=-1;
			PreparedStatement stmt = con.prepareStatement("SELECT ontology_id FROM ontology WHERE name=?");
			stmt.setString(1, name);
			set = stmt.executeQuery();
			while(set.next()){
				id = set.getInt("ontology_id");
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve ontology id for " + name, se);
			return -2;
		}
	}
	/**
	 * 
	 * @param con
	 * @param name
	 * @param identifier
	 * @return id if either name or identifier exists,
	 * @return -1 if term_id doesn't exists
	 * @return -2 if error
	 */
	
	public int getTerm(Connection con, String name, String identifier){
		PreparedStatement stmt = null;
		try{
			int id=-1;
			stmt = con.prepareStatement("SELECT term_id FROM term WHERE name=?");
			if(name == null){
				name = identifier;
				stmt = con.prepareStatement("SELECT term_id FROM term WHERE identifier=?");
			}
			stmt.setString(1, name);
			set = stmt.executeQuery();
			while(set.next()){
				id = set.getInt("term_id");
			}
			if(id < 0 && identifier != null){
				stmt = con.prepareStatement("SELECT term_id FROM term WHERE identifier=?");
				stmt.setString(1, identifier);
				set = stmt.executeQuery();
				while(set.next()){
					id = set.getInt("term_id");
				}
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve term id for "+ name, se);
			return -2;
		}
	}
	
	public int getSeqFeature(Connection con, int bioentry_id, int type_term_id, int source_term_id, int rank){
		try{
			int id=-1;
			SeqfeatureGET = init(con, SeqfeatureGET, "SELECT seqfeature_id FROM seqfeature WHERE bioentry_id=? AND type_term_id=? AND source_term_id=? AND rank=?");
			SeqfeatureGET.setInt(1, bioentry_id);
			SeqfeatureGET.setInt(2, type_term_id);
			SeqfeatureGET.setInt(3, source_term_id);
			SeqfeatureGET.setInt(4, rank);
			set = SeqfeatureGET.executeQuery();
			while(set.next()){
				id = set.getInt("seqfeature_id");
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve seqfeature id "+SeqfeatureGET.toString(), se);
			return -2;
		}
	}
	
	public int getLocation(Connection con, int seqfeature_id, int rank){
		try{
			int id=-1;
			LocationGET = init(con, LocationGET, "SELECT location_id FROM location WHERE seqfeature_id=? AND rank=?");
			LocationGET.setInt(1, seqfeature_id);
			LocationGET.setInt(2, rank);
			set = LocationGET.executeQuery();
			while(set.next()){
				id = set.getInt("location_id");
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve seqfeature id "+LocationGET.toString(), se);
			return -2;
		}
	}
	
	
	/**
	 * Builds the Default bioSQL 1.0.1 schema
	 * 
	 * @return returns true if no errors
	 */
	public boolean buildDatabase(Connection con){
		try{
			String[] sarr = getStatements(mysqldb);
			Statement st = con.createStatement();
			int i =0;
			System.out.print("Building Tables...");
			for(String s : sarr){
				if(s.trim().length() > 0){
					i +=st.executeUpdate(s);
					System.out.print(".");
				}
			}
			System.out.println();
			logger.debug("Returned " + i + " value for " + sarr.length + " queries");
			return true;
		}
		catch(SQLException sexe){
			logger.error("Failed to build bioSQL Table", sexe);
			return false;
		}
		catch(Exception e){
			logger.error("Failed to build bioSQL Table", e);
			return false;
		}
	}
	
	//Assumes the file is within this package
	private String[] getStatements(String fiel) throws IOException{
		InputStream str = this.getClass().getResourceAsStream(fiel);
		logger.debug("Loading File: " + this.getClass().getPackage()+"/"+fiel);
		BufferedReader reader = new BufferedReader(new InputStreamReader(str));
		String line = "";
		StringBuilder currentstate = new StringBuilder("");
		while((line = reader.readLine()) != null){
			if(!line.trim().startsWith("--")){
				currentstate.append(line + " ");
			}
		}
		return currentstate.toString().split(";");
	}
	
}


