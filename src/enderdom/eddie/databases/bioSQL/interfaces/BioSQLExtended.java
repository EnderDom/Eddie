package enderdom.eddie.databases.bioSQL.interfaces;

import java.io.File;
import java.sql.Date;
import java.util.HashMap;

import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.databases.bioSQL.psuedoORM.BioSequence;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.bioSQL.psuedoORM.Taxonomy;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tools.bio.NCBI_DATABASE;

public interface BioSQLExtended {

	public static String programname = "EddieBiologySoftwareSuite";
	public static String authority = "https://github.com/EnderDom";
	public static String description = "Eddie Biology Software was created by Dominic Matthew Wood." +
	" This software manipulates biological data and frequently holds data within databases." +
	" In an attempt to maintain some level of inter-operability the main database framework used is BioSQL.";
	
	//Runtypes of run table
	public static String assembly = "assembly";
	public static String runtable = "run";

	public boolean addEddie2Database(DatabaseManager dbman);
	
	/**
	 * Returns the database version from the 'info' tool
	 * 
	 * @param con
	 * @return double value of the database
	 * returns -1 if version could not be retrieved
	 */
	public double getDatabaseVersion(DatabaseManager dbman);
	
	public int getEddieFromDatabase(DatabaseManager dbman);
	
	//public boolean addBioEntrySynonymTable(DatabaseManager dbman);
	
	
	/**
	 * This table alteration was added to hold more detailed information
	 * about the relationship between the external database reference and the
	 * bioentry. Without this there is now way of ascertaining how strong or weak the
	 * link between these two is. I have read several blogs lambasting the wholesale uploading of
	 * xml into databases, so I realise that recreating the flat file data in the database
	 * may be a bad mistake. However I see no alternative method or storing this data
	 * so it can be accessed from multiple locations by both eddie and any websites easily.
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
	public boolean addBioentryDbxrefCols(DatabaseManager dbman);
	
	//public boolean addBiosequenceRunID(DatabaseManager dbman);
	
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
	public boolean addRunTable(DatabaseManager dbman);
	
	public boolean addRunBioentryTable(DatabaseManager manager);
	
	
	/**
	 * After several different attempts at jamming assembly data 
	 * into the biosql database, this is my latest attempt.
	 * 
	 * Originally used a convoluted method of using pseudo terms & ontologies
	 * to style an assembly, but gave up and just created a new table, 
	 * this may break the database more, but its just a lot simpler.
	 * 
	 * So contig is contig_bioentry_id and read is read_bioentry_id
	 * version is the version of biosequence to use, so basically
	 * you should upload the aligned trimmed read as another 'version'
	 * of the read. This will mean you will have to check available version
	 * numbers, or alternative link a version to an assembly. 
	 * 
	 * As it uses run_id, the run id table should be created first
	 * 
	 * @param manager
	 * @return boolean succesfully created table
	 */
	public boolean addAssemblyTable(DatabaseManager manager);
	
	public boolean addDbxTaxons(DatabaseManager manager);
	
	public boolean addLegacyVersionTable(DatabaseManager dbman, String version, String dbversion) throws Exception;
	
	
	/**
	 * Returns the local name (ie from the ACE record, like Contig_1)
	 * and database identifier (ie CLCBio_Contig_0)
	 */
	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager dbman, int run_id);
	
	
	/**
	 * Supports uploading same mapping again if
	 * contig, read and run ids are the same, will 
	 * just update the values of start, stop, trimmmed 
	 * 
	 * @param manager database manager
	 * @param contig_id to map to read, this should be a bioentry_id previously identified
	 * @param read_id to map to contig, this should be a bioentry_id previously identified
	 * @param runid
	 * @param offset, where the read  
	 * @param start of read alignment to contig relative to read, ie 4 = offset+4 for alignment
	 * @param stop where the read stops alignment
	 * @param trimmed whether or not the read was trimmed
	 * 
	 * @return successful or not
	 */
	public boolean mapRead2Contig(DatabaseManager manager, int contig_id, int read_id, int read_version, int runid, int start, int stop, boolean trimmed);
	
	public String[][] getUniqueStringFields(DatabaseManager manager, String[] fields, String table);
	
	public Run getRun(DatabaseManager manager, int run_id);
	
	public int getRunIdFromInfo(DatabaseManager manager, Run r);

	/**
	 * Returns a 4xn matrix
	 * containing data on reads associated with 
	 * the contig stapiluated by bioentry arg
	 * where x is column and y is row int[x][y]
	 * int[0][y] = bioentry_id
	 * int[1][y] = run_id
	 * int[2][y] = range_start
	 * int[3][y] = range_end
	 * 
	 * Probably better as a ORM but this will suffice
	 * 
	 * @param manager
	 * @param bioentry_id contig bioentry_id
	 * @return 4xn integer matrix
	 */
	public int[][] getReads(DatabaseManager manager, int bioentry_id);
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, int run_id);

	public int getBioEntryId(DatabaseManager manager, String name, boolean fuzzy, int biodatabase_id, int runid);
	
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
	public boolean setBioentry2Dbxref(DatabaseManager manager, int bioentry_id, int run_id, int dbxref_id, Double evalue, Integer score, Integer dbxref_startpos,
			Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame, int hit, int hsp);
	
	public boolean updateDbxref(DatabaseManager manager, int bioentry_id,  int dbxref_id, int run_id, Double evalue, Integer score, Integer dbxref_startpos,
				Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame, int hit, int hsp);
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int run_id, int rank, int hit_no);
	
	/**
	 * Returns run ids for all runs with this runtype and
	 * optionally with the programname, but programname can be
	 * set to null. Runtype cannot be set to null
	 * 
	 * @param manager
	 * @param programname
	 * @param runtype
	 * @return list of Run IDs
	 */
	public int[] getRunId(DatabaseManager manager, String programname, String runtype);
	
	public BioSequence[] getBioSequences(DatabaseManager manager, int bioentry_id);
	
	public boolean setRun(DatabaseManager manager, Date date, String runtype, Integer parent_id,  String program, String version, String dbname, String source, String params, String comment);

	/**
	 * Retrieve some contig names attached to the run of id 'r'
	 * returns 'i' amount of names as a String[]
	 * @param r
	 * @param i
	 * @return String[] of length i
	 */
	public String[] getContigNames(DatabaseManager manager, int r, int i);

	public SequenceList getContigsAsList(DatabaseManager manager, SequenceList l, int i);
	
	public Taxonomy getTaxonomyFromSQL(DatabaseManager manager, Integer biosql_id, Integer ncbi_id);
	
	public boolean updateTaxonParents(DatabaseManager manager);
	
	/**
	 * Retrieves taxon ids and attaches them to dbxref
	 * accessions. Also uploads missing taxons, hierarchical
	 * records are added, but with only the data returned by
	 * the initial taxid ID, namely taxid, name, rank. Run
	 * updateTaxonParents() to fully populate the dataset
	 */
	public boolean updateAccs(DatabaseManager manager, String localdb, NCBI_DATABASE ncbidb);
	
	/**
	 * See biosql notes on taxon table, 
	 * they refer to here
	 * http://www.oreillynet.com/pub/a/network/2002/11/27/bioconf.html
	 * This builds a depth map, so taxons can be block selected based
	 * See Block Select
	 * 
	 * This seems a lot more elegant in perl :( maybe I've
	 * implemented this poorly.
	 * 
	 * @param manager
	 * @param root_id
	 * @return
	 */
	public boolean depthTraversalTaxon(DatabaseManager manager, int root_id);
	
	/**
	 * Use for instance to get a list of the 
	 * taxon_id and string name of all phyla 
	 * by setting node_rank = "phylum"
	 * 
	 * 
	 * @param manager
	 * @return returns a hashmap 
	 * of ncbi_taxon_id is key and name
	 * of node_rank as value
	 */
	public HashMap<Integer, String> getNodeRank(DatabaseManager manager, String node_rank);
	
	/**
	 * Method returns a matrix of 2 by n
	 * with n being the original lenght of 
	 * param listoftaxids. The value in int[1][i]
	 * where 0 > i < n should be the number
	 * of dbxrefs the match database named db
	 * which have the taxid stated in int[0][i]
	 * 
	 * For instance setting listoftaxids to a list of taxids for
	 * all the phylum in the database. Setting assembly_run_id to the run_id for
	 * a 454 assembly and setting blast_run_id to the blast run. Will return for each phylum 
	 * the number of hits against a species within the phylum from a blast run
	 * against the 'nr' database
	 * 
	 * For adding further filters, evalue will not count hits above
	 * the evalue, and hit_no will only count the top hit_no. Set either to -1 to not include
	 * 
	 * @param manager
	 * @param listoftaxids
	 * @param assembly_run_id
	 * @param blast_run_id
	 * @param evalue
	 * @param hit_no
	 * @return
	 */
	public int[][] getTaxonPerAssembly(DatabaseManager manager, int[] listoftaxids, int blast_run_id, double evalue, int hit_no);

	
	public boolean addRunBioentry(DatabaseManager manager, int bioentry, int runid);
	
	/**
	 * Use unique to count the number of genes matched
	 * 
	 * @param manager
	 * @param run_id
	 * @param evalue
	 * @param hit_no
	 * @param hsp_no
	 * @param score
	 * @param unique
	 * @return count representing number of hits for 
	 * that blast run. Assumes the blast run is assembly specific
	 * 
	 */
	public int getHitCount(DatabaseManager manager, int run_id,double evalue, int hit_no, int hsp_no, int score, boolean unique);
	
	/**
	 * @see getHitCount(..)
	 * as with getHitCount but totals reads rather than contig
	 * 
	 * @param manager
	 * @param run_id
	 * @param evalue
	 * @param hit_no
	 * @param hsp_no
	 * @param score
	 * @return
	 */
	public int getHitCountwReadCount(DatabaseManager manager, int run_id,double evalue, int hit_no, int hsp_no, int score);
	
	/**
	 * A relatively specific query, takes blast run and assembly run 
	 * parameters and prints out a cumulative total for the number of
	 * records below the evalue. So the output will be something like
	 * 
	 * 0 2
	 * 1e-180 3
	 * 1e-179 4
	 * ...
	 * 1e-2 1212
	 * 1 1213
	 * 1.1 1214
	 * etc...
	 * 
	 * This is printed straight to file (param output) to avoid messing about
	 * 
	 * @param manager
	 * @param output
	 * @param blastRun
	 * @param hit_no
	 * @param header
	 * @return
	 */
	public boolean cumulativeCountQuery(DatabaseManager manager, File output, int blastRun, int hit_no, boolean header);
	
	/**
	 * Outputs the top species for a blast run, limiting to hits less than evalue and
	 * hits less than hit number. File format is a .csv with the following format
	 * "Homo sapiens",121
	 * "Lymnaea stagnalis",14
	 * "Xenopus (Silurana) tropicalis", 13
     * "Mixia osmundae IAM 14324", 13
     * ....
     * 
     * The name returned here is the ScientificName defined by
     * NCBI, However if taxids is set to true then the output will
     * be the taxid in place of the species name
     * 1212,121
	 * 6447,14
	 * 12, 13
     * ...
     *
	 * @param manager
	 * @param output
	 * @param blastRun
	 * @param evalue
	 * @param taxid
	 * @return
	 */
	public boolean runSpeciesQuery(DatabaseManager manager, File output, int blastRun, double evalue, int hit_no, boolean taxids);

	/**
	 * Resets left and right values
	 *  in the taxonomy table so you 
	 *  can rerun the depth traversel after
	 *  adding more taxonomy data
	 * @param manager
	 * @return
	 */
	public boolean resetDepth(DatabaseManager manager);
	
}
