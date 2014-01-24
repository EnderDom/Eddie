package enderdom.eddie.databases.bioSQL.mysql;

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

import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.psuedoORM.Ontology;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.bioSQL.psuedoORM.TermRelationship;
import enderdom.eddie.tools.Tools_String;

/**
 * @author Dominic Wood
 * 
 *         Using PreparedStatements which are initialised once and used many
 *         times in the vain & unfounded hope that that will speed up mysql
 *         database interaction. (It does in my mind, possibly nowhere else)
 * 
 *         Also strings are created for every single method, these would do
 *         better as a static array
 * 
 */

public class MySQL_BioSQL implements BioSQL {

	private Logger logger = Logger.getRootLogger();
	private static String mysqldb = "biosqldb-mysql.sql";
	private ResultSet set;

	public static String innodb = "ENGINE";

	/**
	 * Check to see if mysql version is 4 or greater because they change syntax
	 * from Type to Engine >3.3 and completely removed at 5.5 so this checks to
	 * see if its 4 or higher for tables
	 * 
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static boolean useEngine(Connection con) throws SQLException {
		Statement st = con.createStatement();
		ResultSet set = st.executeQuery("SHOW VARIABLES LIKE \"%version%\";");
		String v = null;
		while (set.next()) {
			v = set.getString(1);
		}
		set.close();
		st.close();
		if (v.indexOf(".") != -1) {
			Integer i = Tools_String.parseString2Int(v.substring(0,
					v.indexOf(".")));
			if (i != null) {
				System.out.println(v + " of " + i);
				if (i > 3)
					return true;
				else {
					innodb = "TYPE";
					return false;
				}
			} else
				return true;
		}
		return true;
	}

	public boolean addBiosequence(Connection con, Integer version,
			Integer length, String alphabet, String seq, int bioentry_id) {
		try {
			PreparedStatement BioSequenceSET = con
					.prepareStatement("INSERT INTO biosequence (bioentry_id, version, length, alphabet, seq) VALUES (?,?,?,?,?)");
			BioSequenceSET.setInt(1, bioentry_id);
			if (version == null)
				BioSequenceSET.setNull(2, Types.INTEGER);
			else
				BioSequenceSET.setInt(2, version);
			if (length == null)
				BioSequenceSET.setNull(3, Types.INTEGER);
			else
				BioSequenceSET.setInt(3, length);
			if (alphabet == null)
				BioSequenceSET.setNull(4, Types.VARCHAR);
			else
				BioSequenceSET.setString(4, alphabet);
			if (seq == null)
				BioSequenceSET.setNull(5, Types.VARCHAR);
			else
				BioSequenceSET.setString(5, seq);
			BioSequenceSET.execute();
			BioSequenceSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add BioSequence", sq);
			return false;
		}

	}

	public boolean addBioEntry(Connection con, int biodatabase,
			Integer taxon_id, String name, String accession, String identifier,
			String division, String description, int version) {
		try {
			PreparedStatement BioEntrySET = con
					.prepareStatement("INSERT INTO bioentry "
							+ "(biodatabase_id, taxon_id, name, accession, identifier, division, "
							+ "description, version) VALUES (?,?,?,?,?,?,?,?)");

			// Values that cannot be null
			BioEntrySET.setInt(1, biodatabase); // <- Huh? PreparedStatement
												// uses 1-based indexing???
			BioEntrySET.setString(3, name);
			BioEntrySET.setString(4, accession);
			BioEntrySET.setInt(8, version);

			// Values that can be null:
			if (taxon_id == null)
				BioEntrySET.setNull(2, Types.INTEGER);
			else
				BioEntrySET.setInt(2, taxon_id);
			if (identifier == null)
				BioEntrySET.setNull(5, Types.VARCHAR);
			else
				BioEntrySET.setString(5, identifier);
			if (division == null)
				BioEntrySET.setNull(6, Types.VARCHAR);
			else
				BioEntrySET.setString(6, division);
			if (description == null)
				BioEntrySET.setNull(7, Types.VARCHAR);
			else
				BioEntrySET.setString(7, description);
			BioEntrySET.execute();
			BioEntrySET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Bioentry ", sq);
			return false;
		}
	}

	public boolean addSequence(Connection con, int biodatabase,
			Integer taxon_id, String name, String accession, String identifier,
			String division, String description, int version, String seq,
			String alphabet) {

		boolean added = addBioEntry(con, biodatabase, taxon_id, name,
				accession, identifier, division, description, version);
		if (added) {
			int bio_entry = getBioEntry(con, identifier, accession,
					biodatabase, -1);
			if (bio_entry != -1) {
				return addBiosequence(con, version, seq.length(), alphabet,
						seq, bio_entry);
			} else {
				logger.error("Failed to return sequence added");
				return false;
			}
		} else
			return false;
	}

	public boolean addOntology(Connection con, String name, String definition) {
		try {
			PreparedStatement st = con
					.prepareStatement("INSERT INTO ontology (name, definition) VALUES (?,?)");
			st.setString(1, name);
			st.setString(2, definition);
			st.execute();
			st.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Ontology with name " + name, sq);
			return false;
		}
	}

	public boolean addTerm(Connection con, String name, String definition,
			String identifier, String is_obsolete, int ontology_id) {
		try {
			PreparedStatement st = con
					.prepareStatement("INSERT INTO term (name, definition, identifier, is_obsolete, ontology_id) VALUES (?,?,?,?,?)");
			st.setString(1, name);
			if (definition == null)
				st.setNull(2, Types.VARCHAR);
			else
				st.setString(2, definition);
			if (identifier == null)
				st.setNull(3, Types.VARCHAR);
			else
				st.setString(3, identifier);
			if (is_obsolete == null)
				st.setNull(4, Types.CHAR);
			else
				st.setString(4, is_obsolete);
			st.setInt(5, ontology_id);
			st.execute();
			st.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Term with name " + name, sq);
			return false;
		}
	}

	public boolean addBioEntryRelationship(Connection con,
			int object_bioentry_id, int subject_bioentry_id, int term_id,
			Integer rank) {
		try {
			PreparedStatement BioEntryRelationshipSET = con
					.prepareStatement("INSERT INTO bioentry_relationship (object_bioentry_id, "
							+ "subject_bioentry_id, term_id, rank) VALUES (?,?,?,?)");
			BioEntryRelationshipSET.setInt(1, object_bioentry_id);
			BioEntryRelationshipSET.setInt(2, subject_bioentry_id);
			BioEntryRelationshipSET.setInt(3, term_id);
			if (rank == null)
				BioEntryRelationshipSET.setNull(4, Types.INTEGER);
			else
				BioEntryRelationshipSET.setInt(4, rank);
			BioEntryRelationshipSET.execute();
			BioEntryRelationshipSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Entry_Relationship", sq);
			return false;
		}
	}

	public boolean addSeqFeature(Connection con, int bioentry_id,
			int type_term_id, int source_term_id, String display_name, int rank) {
		try {
			PreparedStatement SeqFeatureSET = con
					.prepareStatement("INSERT INTO seqfeature (bioentry_id, type_term_id, source_term_id, display_name, rank) VALUES (?,?,?,?,?)");
			SeqFeatureSET.setInt(1, bioentry_id);
			SeqFeatureSET.setInt(2, type_term_id);
			SeqFeatureSET.setInt(3, source_term_id);
			if (display_name == null)
				SeqFeatureSET.setNull(4, Types.VARCHAR);
			else
				SeqFeatureSET.setString(4, display_name);
			SeqFeatureSET.setInt(5, rank);
			SeqFeatureSET.execute();
			SeqFeatureSET.close();
			return true;

		} catch (SQLException sq) {
			logger.error("Failed to add seqfeature", sq);
			return false;
		}
	}

	public boolean addLocation(Connection con, int seqfeature_id,
			Integer dbxref_id, Integer term_id, Integer start_pos,
			Integer stop_pos, int strand, int rank) {
		try {
			PreparedStatement LocationSET = con
					.prepareStatement("INSERT INTO location (seqfeature_id,"
							+ "dbxref_id,term_id,start_pos,end_pos,"
							+ "strand,rank) VALUES (?,?,?,?,?,?,?)");
			LocationSET.setInt(1, seqfeature_id);
			if (dbxref_id == null)
				LocationSET.setNull(2, Types.INTEGER);
			else
				LocationSET.setInt(2, dbxref_id);
			if (term_id == null)
				LocationSET.setNull(3, Types.INTEGER);
			else
				LocationSET.setInt(3, term_id);
			if (start_pos == null)
				LocationSET.setNull(4, Types.INTEGER);
			else
				LocationSET.setInt(4, start_pos);
			if (stop_pos == null)
				LocationSET.setNull(5, Types.INTEGER);
			else
				LocationSET.setInt(5, stop_pos);
			LocationSET.setInt(6, strand);
			LocationSET.setInt(7, rank);
			LocationSET.execute();
			LocationSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add location ", sq);
			return false;
		}
	}

	public boolean addDBxref(Connection con, String dbname, String accession,
			int version, String description) {
		if(description==null)description="";
		try {
			PreparedStatement DBxrefSET = con
					.prepareStatement("INSERT INTO dbxref (dbname, accession, "
							+ "version, description) VALUES (?,?,?,?)");
			DBxrefSET.setString(1, dbname);
			DBxrefSET.setString(2, accession);
			DBxrefSET.setInt(3, version);
			DBxrefSET.setString(4, description);
			DBxrefSET.execute();
			DBxrefSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add location ", sq);
			return false;
		}

	}

	/*******************************************************************/
	/*																   */
	/*																   */
	/* Get Methods */
	/*																   */
	/*																   */
	/*******************************************************************/


	public String[] getBioEntryNames(Connection con, int bioentry_id) {
		try {
			String[] names = new String[3];
			Statement st = con.createStatement();
			ResultSet set = st
					.executeQuery("SELECT name, accession, identifier FROM bioentry WHERE bioentry_id="
							+ bioentry_id);
			while (set.next()) {
				names[0] = set.getString("name");
				names[1] = set.getString("accession");
				names[2] = set.getString("identifier");
			}
			set.close();
			st.close();
			return names;
		} catch (SQLException sq) {
			logger.error("Failed to get BioEntry with bioentry id "
					+ bioentry_id, sq);
			return null;
		}
	}

	public int getBioEntry(Connection con, String identifier, String accession,
			int biodatabase) {
		return getBioEntry(con, identifier, accession, biodatabase, -1);
	}

	public int getBioEntry(Connection con, String identifier, String accession,
			int biodatabase, int runid) {
		String where1 = "WHERE identifier=? AND biodatabase_id=?";
		String where2 = "WHERE accession=? AND biodatabase_id=?";
		String init = "SELECT bioentry_id FROM bioentry ";
		init += runid > 0 ? "INNER JOIN bioentry_run USING(bioentry_id) " : "";
		String run = runid > 0 ? " AND run_id=" + runid : "";
		try {
			if (runid > 0) {
				PreparedStatement BioEntryGET1wR = con.prepareStatement(init
						+ where1 + run);
				PreparedStatement BioEntryGET2wR = con.prepareStatement(init
						+ where2 + run);
				return getBioEntry(BioEntryGET1wR, BioEntryGET2wR, identifier,
						accession, biodatabase);
			} else {
				PreparedStatement BioEntryGET1 = con.prepareStatement(init
						+ where1);
				PreparedStatement BioEntryGET2 = con.prepareStatement(init
						+ where2);
				return getBioEntry(BioEntryGET1, BioEntryGET2, identifier,
						accession, biodatabase);
			}

		} catch (SQLException sq) {
			logger.error("Failed to add Bioentry identifier", sq);
			return -1;
		}
	}

	private int getBioEntry(PreparedStatement stat1, PreparedStatement stat2,
			String identifier, String accession, int biodatabase)
			throws SQLException {
		int entry = -1;
		PreparedStatement stmt = null;

		if (identifier == null) {
			stmt = stat2;
			identifier = accession;
		} else {
			stmt = stat1;
		}
		stmt.setString(1, identifier);
		stmt.setInt(2, biodatabase);
		set = stmt.executeQuery();
		while (set.next()) {
			entry = set.getInt("bioentry_id");
		}
		if (entry == -1 && accession != null) {
			stmt = stat2;
			stmt.setString(1, accession);
			stmt.setInt(2, biodatabase);
			set = stmt.executeQuery();
			while (set.next()) {
				entry = set.getInt("bioentry_id");
			}
		}
		stat1.close();
		stat2.close();
		return entry;
	}

	public int getBioEntrywName(Connection con, String name, int runid) {
		int entry = -1;
		String init = "SELECT bioentry_id FROM bioentry ";
		init += runid > 0 ? "INNER JOIN bioentry_run USING(bioentry_id) " : "";
		String where = "WHERE name='" + name + "'";
		where += runid > 0 ? " AND run_id=" + runid : "";

		try {
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(init + where);
			while (set.next()) {
				entry = set.getInt("bioentry_id");
				if (set.next()) {
					logger.warn("Other ids are available with this name "
							+ name);
					logger.warn("SQL was " + init + where);
				}
				break;
			}
			set.close();
			st.close();

		} catch (SQLException sq) {
			logger.error("Failed to get bioentry id with name: " + name
					+ " using SQL " + init + where, sq);
		}
		return entry;
	}

	public int getBioEntrywName(Connection con, String name) {
		return getBioEntrywName(con, name, -1);
	}

	public int getBioEntryRelationship(Connection con, int bioentry_object_id,
			int bioentry_subject_id, int term_id) {

		int entry = -1;
		try {
			PreparedStatement BioEntryRelationshipGET = con
					.prepareStatement("SELECT bioentry_relationship_id "
							+ "FROM bioentry_relationship WHERE "
							+ "object_bioentry_id=? AND subject_bioentry_id=? "
							+ "AND term_id=?");

			BioEntryRelationshipGET.setInt(1, bioentry_object_id);
			BioEntryRelationshipGET.setInt(2, bioentry_subject_id);
			BioEntryRelationshipGET.setInt(3, term_id);

			set = BioEntryRelationshipGET.executeQuery();
			while (set.next()) {
				entry = set.getInt("bioentry_relationship_id");
			}
			set.close();
			BioEntryRelationshipGET.close();

		} catch (SQLException sq) {
			logger.error("Failed to add Biosequence", sq);
		}
		return entry;
	}

	public Ontology getOntology(Connection con, String name) {
		try {
			PreparedStatement stmt = con
					.prepareStatement("SELECT ontology.* FROM ontology WHERE name=?");
			stmt.setString(1, name);
			set = stmt.executeQuery();
			Ontology o = null;
			while (set.next()) {
				o = new Ontology();
				o.setOntology_id(set.getInt("ontology_id"));
				o.setName(set.getString("name"));
				o.setDefinition(set.getString("definition"));
			}
			set.close();
			stmt.close();
			return o;
		} catch (SQLException se) {
			logger.error("Failed to retrieve ontology id for " + name, se);
			return null;
		}
	}

	public Term getTerm(Connection con, String name, String identifier,
			int ontology_id) {
		PreparedStatement stmt = null;
		try {
			Term t = null;
			stmt = con
					.prepareStatement("SELECT term.* FROM term WHERE name=? AND ontology_id=?");
			if (name == null) {
				name = identifier;
				stmt = con
						.prepareStatement("SELECT term.* FROM term WHERE identifier=? AND ontology_id=?");
			}
			stmt.setString(1, name);
			stmt.setInt(2, ontology_id);
			set = stmt.executeQuery();
			while (set.next()) {
				t = new Term(set.getInt("ontology_id"), set.getInt("term_id"),
						set.getString("name"), set.getString("definition"),
						set.getString("identifier"),
						set.getString("is_obsolete"));
			}
			if (t == null && identifier != null) {
				stmt = con
						.prepareStatement("SELECT term.* FROM term WHERE identifier=? AND ontology_id=?");
				stmt.setString(1, identifier);
				stmt.setInt(2, ontology_id);
				set = stmt.executeQuery();
				while (set.next()) {
					t = new Term(set.getInt("ontology_id"),
							set.getInt("term_id"), set.getString("name"),
							set.getString("definition"),
							set.getString("identifier"),
							set.getString("is_obsolete"));
				}
				set.close();
				stmt.close();
			}
			return t;
		} catch (SQLException se) {
			logger.error("Failed to retrieve term id for " + name, se);
			return null;
		}
	}

	public TermRelationship getTermRelationship(Connection con, int subject_id,
			int object_id, int predicate_id, int ontology_id) {
		try {
			PreparedStatement TermRelationshipGET = con
					.prepareStatement("SELECT term_relationship.* FROM term_relationship "
							+ "WHERE subject_term_id=? AND predicate_term_id=? AND object_term_id=? AND ontology_id=?");
			TermRelationshipGET.setInt(1, subject_id);
			TermRelationshipGET.setInt(2, predicate_id);
			TermRelationshipGET.setInt(3, object_id);
			TermRelationshipGET.setInt(4, ontology_id);
			set = TermRelationshipGET.executeQuery();
			TermRelationship t = null;
			while (set.next()) {
				t = new TermRelationship();
				t.setSubject_id(set.getInt("subject_term_id"));
				t.setPredicate_id(set.getInt("predicate_term_id"));
				t.setObject_id(set.getInt("object_term_id"));
				t.setOntology_id(set.getInt("ontology_id"));
				t.setTermRel_id(set.getInt("term_relationship_id"));
			}
			set.close();
			TermRelationshipGET.close();
			return t;
		} catch (SQLException sq) {
			logger.error("Failed to get Term relationship", sq);
			return null;
		}
	}

	public int getSeqFeature(Connection con, int bioentry_id, int type_term_id,
			int source_term_id, int rank) {
		try {
			int id = -1;
			PreparedStatement SeqfeatureGET = con
					.prepareStatement("SELECT seqfeature_id FROM seqfeature WHERE bioentry_id=? "
							+ "AND type_term_id=? AND source_term_id=? AND rank=?");
			SeqfeatureGET.setInt(1, bioentry_id);
			SeqfeatureGET.setInt(2, type_term_id);
			SeqfeatureGET.setInt(3, source_term_id);
			SeqfeatureGET.setInt(4, rank);
			set = SeqfeatureGET.executeQuery();
			while (set.next()) {
				id = set.getInt("seqfeature_id");
			}
			set.close();
			SeqfeatureGET.close();
			return id;
		} catch (SQLException se) {
			logger.error("Failed to retrieve seqfeature id with bioentry id "
					+ bioentry_id, se);
			return -2;
		}
	}

	public int getLocation(Connection con, int seqfeature_id, int rank) {
		try {
			int id = -1;
			PreparedStatement LocationGET = con
					.prepareStatement("SELECT location_id FROM location WHERE seqfeature_id=? AND rank=?");
			LocationGET.setInt(1, seqfeature_id);
			LocationGET.setInt(2, rank);
			set = LocationGET.executeQuery();
			while (set.next()) {
				id = set.getInt("location_id");
			}
			set.close();
			LocationGET.close();
			return id;
		} catch (SQLException se) {
			logger.error("Failed to retrieve location id " + seqfeature_id, se);
			return -2;
		}
	}

	public String getSequence(Connection con, int bioentry_id) {
		String str = null;
		try {
			Statement st = con.createStatement();
			set = st.executeQuery("SELECT seq FROM biosequence WHERE bioentry_id="
					+ bioentry_id);
			while (set.next()) {
				str = set.getString("seq");
			}
			set.close();
			st.close();
		} catch (SQLException e) {
			logger.error("Failed to get sequence for contig id " + bioentry_id,
					e);
		}
		return str;
	}

	public int getDBxRef(Connection con, String dbname, String accession) {
		int i = -1;
		try {
			PreparedStatement DBxrefGET = con
					.prepareStatement("SELECT dbxref_id FROM dbxref WHERE dbname=? AND accession=?");
			DBxrefGET.setString(1, dbname);
			DBxrefGET.setString(2, accession);
			set = DBxrefGET.executeQuery();
			while (set.next()) {
				i = set.getInt("dbxref_id");
			}
			set.close();
			DBxrefGET.close();
			return i;
		} catch (SQLException sq) {
			logger.error("Failed to get dbxref", sq);
			return -1;
		}
	}

	/**
	 * Builds the Default bioSQL 1.0.1 schema
	 * 
	 * @return returns true if no errors
	 */
	public boolean buildDatabase(Connection con) {
		try {
			boolean engine = useEngine(con);
			if (engine) {
				logger.debug("mysql version is 4 or greater switching INNODB from TYPE to ENGINE");
			}
			String[] sarr = getStatements(mysqldb);
			Statement st = con.createStatement();
			int i = 0;
			System.out.print("Building Tables...");
			for (String s : sarr) {
				if (s.trim().length() > 0) {
					if (engine)
						s = s.replaceAll("TYPE=INNODB", "ENGINE=INNODB");
					i += st.executeUpdate(s);
					System.out.print(".");
				}
			}
			st.close();
			System.out.println();
			logger.debug(sarr.length + " queries run, " + i + " issues ");
			return true;
		} catch (SQLException sexe) {
			logger.error("Failed to build bioSQL Table", sexe);
			return false;
		} catch (Exception e) {
			logger.error("Failed to build bioSQL Table", e);
			return false;
		}
	}

	// Assumes the file is within this package
	private String[] getStatements(String fiel) throws IOException {
		InputStream str = this.getClass().getResourceAsStream(fiel);
		logger.debug("Loading File: " + this.getClass().getPackage() + "/"
				+ fiel);
		BufferedReader reader = new BufferedReader(new InputStreamReader(str));
		String line = "";
		StringBuilder currentstate = new StringBuilder("");
		while ((line = reader.readLine()) != null) {
			if (!line.trim().startsWith("--")) {
				currentstate.append(line + " ");
			}
		}
		return currentstate.toString().split(";");
	}

	public int addTaxon(Connection con, Integer ncbi_id, Integer parent_id,
			String node_rank, Integer gencode, Integer mitocode,
			Integer leftvalue, Integer rightvalue, int taxon_id) {
		try {
			PreparedStatement TaxonSET;
			if (taxon_id < 1) {
				TaxonSET = con
						.prepareStatement("INSERT INTO taxon (ncbi_taxon_id, parent_taxon_id, node_rank, "
								+ "genetic_code, mito_genetic_code, left_value, right_value) VALUES (?,?,?,?,?,?,?)");
			} else {
				TaxonSET = con
						.prepareStatement("UPDATE taxon SET ncbi_taxon_id=?, parent_taxon_id=?,"
								+ " node_rank=?, genetic_code=?, mito_genetic_code=?, left_value=?, right_value=? "
								+ "WHERE taxon_id=?");
				TaxonSET.setInt(8, taxon_id);
			}
			// Values that cannot be null
			if (ncbi_id == null || ncbi_id == 0)
				TaxonSET.setNull(1, Types.INTEGER);
			else
				TaxonSET.setInt(1, ncbi_id);
			if (parent_id == null || parent_id == 0)
				TaxonSET.setNull(2, Types.INTEGER);
			else
				TaxonSET.setInt(2, parent_id);
			if (node_rank == null)
				TaxonSET.setNull(3, Types.VARCHAR);
			else
				TaxonSET.setString(3, node_rank);
			if (gencode == null)
				TaxonSET.setNull(4, Types.INTEGER);
			else
				TaxonSET.setInt(4, gencode);
			if (mitocode == null)
				TaxonSET.setNull(5, Types.INTEGER);
			else
				TaxonSET.setInt(5, mitocode);
			if (leftvalue == null)
				TaxonSET.setNull(6, Types.INTEGER);
			else
				TaxonSET.setInt(6, leftvalue);
			if (rightvalue == null)
				TaxonSET.setNull(7, Types.INTEGER);
			else
				TaxonSET.setInt(7, rightvalue);
			TaxonSET.execute();
			TaxonSET.close();
			if (ncbi_id == null) {
				logger.warn("Should upload a taxid without something unique");
				return -1;
			} else {
				if (taxon_id < 1)
					return getTaxonIdwNCBI(con, ncbi_id);
				else
					return taxon_id;
			}
		} catch (SQLException sq) {
			logger.error("Failed to add Taxon id ", sq);
			return -1;
		}
	}

	public int getTaxonIdwNCBI(Connection con, int ncbi_id) {
		try {
			PreparedStatement TaxonGET = con
					.prepareStatement("SELECT taxon_id FROM taxon WHERE ncbi_taxon_id=?");
			TaxonGET.setInt(1, ncbi_id);
			set = TaxonGET.executeQuery();
			int ret = -1;
			while (set.next()) {
				ret = set.getInt("taxon_id");
			}
			set.close();
			TaxonGET.close();
			return ret;
		} catch (SQLException sq) {
			logger.error("Failed to add Taxon id ", sq);
			return -1;
		}
	}

	public boolean addTaxonName(Connection con, int taxon_id, String name,
			String name_class) {
		try {
			PreparedStatement TaxonNameSET = con
					.prepareStatement("INSERT INTO taxon_name (taxon_id, name, name_class)"
							+ "VALUES (?,?,?)");

			TaxonNameSET.setInt(1, taxon_id);
			TaxonNameSET.setString(2, name);
			TaxonNameSET.setString(3, name_class);
			TaxonNameSET.execute();
			TaxonNameSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Taxon name ", sq);
			return false;
		}
	}

	public boolean addTermRelationship(Connection con, int subject_id,
			int object_id, int predicate_id, int ontology_id) {
		try {
			PreparedStatement TermRelationshipSET = con
					.prepareStatement("INSERT INTO term_relationship (subject_term_id, predicate_term_id, "
							+ "object_term_id, ontology_id) VALUES (?,?,?,?)");

			TermRelationshipSET.setInt(1, subject_id);
			TermRelationshipSET.setInt(2, predicate_id);
			TermRelationshipSET.setInt(3, object_id);
			TermRelationshipSET.setInt(4, ontology_id);
			TermRelationshipSET.execute();
			TermRelationshipSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Term relationship with subject "
					+ subject_id + " and object " + object_id, sq);
			return false;
		}
	}

	public boolean addDbxrefTermPath(Connection con, int dbxref_id,
			int term_id, int rank, String value) {
		try {
			PreparedStatement DbxrefTermValueSET = con
					.prepareStatement("INSERT IGNORE INTO dbxref_qualifier_value (dbxref_id, term_id, "
							+ "rank, value) VALUES (?,?,?,?)");

			DbxrefTermValueSET.setInt(1, dbxref_id);
			DbxrefTermValueSET.setInt(2, term_id);
			DbxrefTermValueSET.setInt(3, rank);
			if (value == null)
				DbxrefTermValueSET.setNull(4, Types.VARCHAR);
			else
				DbxrefTermValueSET.setString(4, value);
			DbxrefTermValueSET.execute();
			DbxrefTermValueSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Term dbxref path ", sq);
			return false;
		}
	}

	public boolean addDbxrefTerm(Connection con, int dbxref_id, int term_id,
			Integer rank) {
		try {
			PreparedStatement DbxrefTermSET = con
					.prepareStatement("INSERT IGNORE INTO term_dbxref (dbxref_id, term_id, rank) VALUES (?,?,?)");
			DbxrefTermSET.setInt(1, dbxref_id);
			DbxrefTermSET.setInt(2, term_id);
			if (rank == null)
				DbxrefTermSET.setNull(3, Types.INTEGER);
			else
				DbxrefTermSET.setInt(3, rank);
			DbxrefTermSET.execute();
			DbxrefTermSET.close();
			return true;
		} catch (SQLException sq) {
			logger.error("Failed to add Term dbxref path ", sq);
			return false;
		}
	}

	public void largeInsert(Connection con, boolean start) {
		try {
			Statement st = con.createStatement();
			if (start) {
				logger.debug("Starting mysql transaction");
				st.execute("START TRANSACTION;");
			} else {
				st.execute("COMMIT;");
				logger.debug("Commiting cached transactions");
			}
			st.close();
		} catch (SQLException sq) {
			logger.error("Failed in method large insert", sq);
		}
	}

}
