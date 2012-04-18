package databases.bioSQL.pgsql;

import java.sql.Statement;

import databases.bioSQL.interfaces.GenericBioSQL;

public class PgSQL_GenericBioSQL implements GenericBioSQL{

	public boolean addBiosequence(Statement st, int version, int length,
			String alphabet, String seq, int bioentry_id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addBioEntry(Statement st, int biodatabase, int taxon_id,
			String name, String accession, String identifier, String division,
			String description, int version) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addSequence(Statement st, int biodatabase, int taxon_id,
			String name, String accession, String identifier, String division,
			String description, int version, String seq, String alphabet) {
		// TODO Auto-generated method stub
		return false;
	}

	//STUB
}
