package databases.bioSQL.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class BioSQLSequences {

	/*
	 * 
	 * Use of BioSQL for 'private lab'
	 * See: "Bioentry with Taxon and Namespace" Section @ http://biosql.org/wiki/Schema_Overview
	 * 
	 * Alphabets available for biosequence,
	 * 
	 * Note there case is not defined;
	 * 		"The Bio* toolkits turn out to use slight variations of the set of valid alphabet names. Consistency could be enforced through a check constraint.
			 BioPerl uses protein, dna, and rna. Biojava reportedly uses all-uppercase for all three. "
		from http://biosql.org/wiki/Enhancement_Requests (Accessed [18/04/12])
	 * 
	 */
	public static String alphabet_dna = "DNA";
	public static String alphabet_rna = "RNA";
	public static String alphabet_protein = "PROTEIN";
	Logger logger = Logger.getRootLogger();
	public static int MySQL = 1;
	public static int PostGresSQL = 1;
	
	
	public boolean addBiosequence(Statement st, int version,int length, String alphabet, String seq,  int bioentry_id, int dbtype){
		if(dbtype ==MySQL){
			return addBiosequenceMySQL(st,version,length,alphabet, seq, bioentry_id);
		}
		else if(dbtype ==PostGresSQL){
			//TODO
			return false;
		}
		else{
			logger.warn("Unsupported Database type set");
			return false;
		}
	}

	public boolean addBioEntry(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version, int dbtype){
		if(dbtype ==MySQL){
			return addBioentryMySQL(st, biodatabase,taxon_id, name, accession, identifier, division, description, version);
		}
		else if(dbtype ==PostGresSQL){
			//TODO
			return false;
		}
		else{
			logger.warn("Unsupported Database type set");
			return false;
		}
	}
	
	public boolean addSequence(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet, int dbtype){
		if(dbtype ==MySQL){
			return addBiosequenceMySQL(st,version,length,alphabet, seq, bioentry_id);
		}
		else if(dbtype ==PostGresSQL){
			//TODO
			return false;
		}
		else{
			logger.warn("Unsupported Database type set");
			return false;
		}
	}
	

	
	public boolean addBiosequenceMySQL(Statement st, int version,int length, String alphabet, String seq,  int bioentry_id) {
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

	public boolean addBioEntryMySQL(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version) {
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
	
	public boolean addSequenceMySQL(Statement st, int biodatabase, int taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet){
		addBioEntryMySQL(st, biodatabase, taxon_id, name, accession, identifier, division, description, version);
		int bio_entry = getBioEntryMySQL(st, identifier, biodatabase);
		if(bio_entry != -1){
			return addBiosequenceMySQL(st, version, seq.length(), alphabet, seq, bio_entry);
		}
		else{
			return false;
		}
	}

	public int getBioEntryMySQL(Statement st, String identifier, int biodatabase){
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
