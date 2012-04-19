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
	//Gets
	private PreparedStatement BioEntryGET1;
	private PreparedStatement BioEntryGET2;	
	
	public void clearStatements(){
		BioEntrySET = null;
		BioSequenceSET = null;
		BioEntryRelationshipSET = null;
		BioEntryGET1 = null;
		BioEntryGET2 = null;
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
			logger.error("Failed to add Entry_Relationship with ", sq);
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
			ResultSet set = stmt.executeQuery();
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
	
	public int getOntology(Connection con, String name){
		try{
			int id=-1;
			PreparedStatement stmt = con.prepareStatement("SELECT ontology_id FROM ontology WHERE name=?");
			stmt.setString(1, name);
			ResultSet set = stmt.executeQuery();
			while(set.next()){
				id = set.getInt("ontology_id");
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve ontology id for " + name);
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
		try{
			int id=-1;
			PreparedStatement stmt = con.prepareStatement("SELECT term_id FROM term WHERE name=?");
			if(name == null){
				name = identifier;
				stmt = con.prepareStatement("SELECT term_id FROM term WHERE identifier=?");
			}
			stmt.setString(1, name);
			ResultSet set = stmt.executeQuery();
			while(set.next()){
				id = set.getInt("term_id");
			}
			return id;
		}
		catch(SQLException se){
			logger.error("Failed to retrieve ontology id for " + name);
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

