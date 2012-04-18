package databases.bioSQL.mysql;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.GenericBioSQL;

/**
 * 
 * @author Dominic Wood
 *
 */

public class MySQL_GenericBioSQL implements GenericBioSQL{

	Logger logger = Logger.getRootLogger();
	
	public boolean addBiosequence(Statement st, int version,int length, String alphabet, String seq,  int bioentry_id) {
		String insert = new String("INSERT INTO biosequence (bio_entry, version, length, alphabet, seq) VALUES ('"+version+"', '"+length+"', '"+alphabet+"', '"+seq+"')");
		try{
			st.executeUpdate(insert);
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);
			return false;
		}
		
	}

	public boolean addBioEntry(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version) {
		String insert = new String("INSERT INTO bioentry (biodatabase, taxon_id, name, accession, identifier, division, description, version) " +
				"VALUES ('"+biodatabase+"','"+taxon_id+"','"+name+"','"+accession+"','"+identifier+"','"+division+"','"+version+"')");
		try{
			st.executeUpdate(insert);
			return true;
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);
			return false;
		}
	}
	
	public boolean addSequence(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet){
		addBioEntry(st, biodatabase, taxon_id, name, accession, identifier, division, description, version);
		int bio_entry = getBioEntry(st, identifier, biodatabase);
		if(bio_entry != -1){
			return addBiosequence(st, version, seq.length(), alphabet, seq, bio_entry);
		}
		else{
			return false;
		}
	}

	public int getBioEntry(Statement st, String identifier, int biodatabase){
		String insert = new String("SELECT bioentry_id FROM bioentry WHERE identifier=''"+identifier+" AND biodatabase='"+biodatabase+"'");
		int entry =-1;
		try{
			ResultSet set = st.executeQuery(insert);
			while(set.next()){
				entry = set.getInt("bioentry_id");
			}
		}
		catch(SQLException sq){
			logger.error("Failed to add Biosequence", sq);			
		}
		return entry;
	}
	
}

