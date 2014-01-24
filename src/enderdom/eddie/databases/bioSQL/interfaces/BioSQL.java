package enderdom.eddie.databases.bioSQL.interfaces;

import java.sql.Connection;

import enderdom.eddie.databases.bioSQL.psuedoORM.Ontology;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.bioSQL.psuedoORM.TermRelationship;

/**
 * This class should include only the bare essentials for inputting data  
 * into the bioSQL database, more complex, program specific methods
 * should be added to the Extended class using these base methods 
 * @author Dominic Wood
 *
 * In general this is some sort of hybrid bastardisation of bioSQL, 
 * meant to help improve the portability of my data, but undoubtably
 * will further mire me into a sea of bioinformatic oblivion.
 *
 */

public interface BioSQL {
	
	public static String alphabet_DNA = "DNA";
	public static String alphabet_RNA = "RNA";
	public static String alphabet_PROTEIN = "PROTEIN";
	
	public boolean buildDatabase(Connection con);
	
	/*
	 * 
	 * Alphabet can be either DNA, RNA or PROTEIN
	 * 
	 * 
	 * From http://www.biosql.org/wiki/Enhancement_Requests [Accessed 18/04/12]:
	   
	    "Check constraint on biosequence.alphabet
		The Bio* toolkits turn out to use slight variations of the set of valid alphabet names. 
		Consistency could be enforced through a check constraint.
		BioPerl uses protein, dna, and rna.
		Biojava reportedly uses all-uppercase for all three."
		 
	 * 
	 * We can confirm biojava 1.8 uses upper case with: 
	 * Alphabet s = DNATools.createDNA("ATAG").getAlphabet();
			System.out.println(s.getName());
	 */
	public boolean addBiosequence(Connection con, Integer version, Integer length, String alphabet, String seq,  int bioentry_id);
	
	public boolean addBioEntry(Connection con, int biodatabase, Integer taxon_id, String name, String accession, String identifier, String division, String description, int version);
	
	/*
	 * Adds both the bioentry and biosequence in one method, should be preferable
	 * as the BioSequence require bioentry id anyway. 
	 */
	public boolean addSequence(Connection con, int biodatabase, Integer taxon_id, String name, String accession, String identifier, String division, String description, int version, String seq, String alphabet);
	
	public boolean addOntology(Connection con, String name, String definition);
	
	public boolean addTerm(Connection con, String name, String definition, String identifier, String is_obsolete, int ontology_id);
	
	public boolean addBioEntryRelationship(Connection con, int object_bioentry_id, int subject_bioentry_id, int term_id, Integer rank);
	
	public boolean addSeqFeature(Connection con, int bioentry_id, int type_term_id, int source_term_id, String display_name, int rank);
	
	public boolean addLocation(Connection con, int seqfeature_id, Integer dbxref_id, Integer term_id, Integer start_pos, Integer stop_pos, int strand, int rank);
	
	/**
	 * 
	 * @param con
	 * @param dbname
	 * @param accession
	 * @param version
	 * @return if insert was successful
	 */
	public boolean addDBxref(Connection con, String dbname, String accession, int version, String description);
	
	/**
	 * 
	 * @param con
	 * @param ncbi_id
	 * @param parent_id
	 * @param node_rank
	 * @param gencode
	 * @param mitocode
	 * @param leftvalue
	 * @param rightvalue
	 * @param taxon_id set this to -1 to INSERT, or use id for UPDATE
	 * @return
	 */
	public int addTaxon(Connection con, Integer ncbi_id, Integer parent_id, String node_rank, Integer gencode,
			Integer mitocode, Integer leftvalue, Integer rightvalue, int taxon_id);
	
	public boolean addTaxonName(Connection con, int taxon_id, String name, String name_class);
	
	public int getTaxonIdwNCBI(Connection con, int ncbi_id);
	
	/**
	 * 
	 * @param con
	 * @param bioentry_id
	 * @return string array with [0] = "name", [1] = "accession", [2] = "identifier"
	 * 
	 */
	public String[] getBioEntryNames(Connection con, int bioentry_id);
	
	/**
	 * 
	 * Input is the identifier and/or accession,  
	 * the method will check identifier first, if it is not null
	 * then if accession is null or no result from identifier
	 * it will the check using accession, however accession is not 
	 * assumed to be null, so in the presence of multiple values with the same
	 * accession -1 will be returned
	 * 
	 * Both identifier and accession is used as, whilst identifier is unique, 
	 * it is also non-null.
	 * 
	 * @return -1 where entry does not exists, 
	 * -2 if identifier doesn't exist and accession does
	 * 
	 * 
	 */
	public int getBioEntry(Connection con, String identifier, String accession, int biodatabase, int runid);
	
	public int getBioEntry(Connection con, String identifier, String accession, int biodatabase);
	
	public Ontology getOntology(Connection con, String name);
	
	public Term getTerm(Connection con, String name, String identifier, int ontology_id);
	
	public int getSeqFeature(Connection con, int bioentry_id, int type_term_id, int source_term_id, int rank);
	
	public int getBioEntryRelationship(Connection con, int bioentry_object_id, int bioentry_subject_id, int term_id);
	
	public int getLocation(Connection con, int seqfeature_id, int rank);
	
	/**
	 * Retrieve bioentry_id with the name field only, this 
	 * will return the first instance with this name only, 
	 * whilst a warning is logged, no exception is thrown
	 * 
	 * @param con
	 * @param name
	 * @return
	 */
	public int getBioEntrywName(Connection con, String name);
	
	public int getBioEntrywName(Connection con, String name, int runid);
	
	/**
	 * 
	 * @param con
	 * @param bioentry_id
	 * @return the 'seq' field from the biosequence table which
	 * is attached to the bioentry_id
	 */
	public String getSequence(Connection con, int bioentry_id);
	
	public int getDBxRef(Connection con, String dbname, String accession);
	
	public boolean addTermRelationship(Connection con, int subject_id,
			int object_id, int predicate_id, int ontology_id);

	public boolean addDbxrefTermPath(Connection con, int dbxref_id, int term_id, int rank,
			String value);

	public TermRelationship getTermRelationship(Connection con, int subject_id,
			int object_id, int predicate_id, int ontology_id);

	public boolean addDbxrefTerm(Connection con, int dbxref_id, int term_id, Integer rank);

	//This is really just mysql specific, but it allows faster mysql for innodb
	public void largeInsert(Connection con, boolean start);
}
