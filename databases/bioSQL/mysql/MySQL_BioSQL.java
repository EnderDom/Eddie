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
	
	public boolean addBiosequence(Connection con, int version,int length, String alphabet, String seq,  int bioentry_id) {
		String insert = new String("INSERT INTO biosequence (bioentry_id, version, length, alphabet, seq) VALUES ('"+bioentry_id+"','"+version+"', '"+length+"', '"+alphabet+"', '"+seq+"')");
		try{
			Statement st = con.createStatement();
			st.executeUpdate(insert);
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);
			return false;
		}
		
	}

	public  boolean addBioEntry(Connection con, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version) {
		try{
			PreparedStatement stm = con.prepareStatement("INSERT INTO bioentry (biodatabase_id, taxon_id, name, accession, identifier, division, description, version) " +
			"VALUES (?,?,?,?,?,?,?,?)");
			//Values that cannot be null
			stm.setInt(1, biodatabase); //<- Huh? PreparedStatement uses 1-based indexing???
			stm.setString(3, name);
			stm.setString(4, accession);
			stm.setInt(8, version);
			
			//Values that can be null:
			if(taxon_id < 0)stm.setNull(2, Types.INTEGER);
			else stm.setInt(2, taxon_id);
			if(identifier == null)stm.setNull(5, Types.VARCHAR);
			else stm.setString(5, identifier); 
			if(division == null)stm.setNull(6, Types.VARCHAR);
			else stm.setString(6, division);
			if(description == null) stm.setNull(7, Types.VARCHAR);
			else stm.setString(7, description);
			
			logger.trace("(Prepared SQL): "+stm.toString());
			stm.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);
			return false;
		}
	}
	
	public boolean addSequence(Connection con, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet){
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
	
	public boolean addOntology(Connection con, String name, String description){
		try{
			PreparedStatement st = con.prepareStatement("INSERT INTO (name, description) VALUES (?,?)");
			st.setString(1, name);
			st.setString(2, description);
			st.execute();
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Ontology with name " + name, sq);
			return false;
		}
	}
	
	/*******************************************************************/
	/*																   */
	/*																   */
	/*							Get Methods							   */
	/*																   */
	/*																   */
	/*******************************************************************/

	public int getBioEntry(Connection con, String identifier, String accession, int biodatabase){
		String insert = null;
		if(identifier != null && accession != null){
			insert = new String("SELECT bioentry_id FROM bioentry WHERE indentifier='"+identifier+"' AND accession='"+accession+"' AND biodatabase_id='"+biodatabase+"'");
		}
		else if(identifier != null){
			insert = new String("SELECT bioentry_id FROM bioentry WHERE indentifier='"+identifier+"' AND biodatabase_id='"+biodatabase+"'");
		}
		else{
			insert = new String("SELECT bioentry_id FROM bioentry WHERE accession='"+accession+"' AND biodatabase_id='"+biodatabase+"'");
		}
		int entry =-1;
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				entry = set.getInt("bioentry_id");
			}
			if(entry == -1 && accession != null){
				insert = new String("SELECT bioentry_id FROM bioentry WHERE identifier='"+identifier+"' AND biodatabase_id='"+biodatabase+"'");	
				set = st.executeQuery(insert);
				int c=0;
				while(set.next()){
					entry = set.getInt("bioentry_id");
					c++;
				}
				if(c>1){
					entry=-2;
					logger.warn("Warning multiple results returned with accession "+accession);
				}
			}
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);			
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

