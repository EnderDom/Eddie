package databases.bioSQL.interfaces;

import java.util.HashMap;

import databases.manager.DatabaseManager;

public interface BioSQLExtended {

	public static String programname = "EddieBiologySoftwareSuite";
	public static String authority = "https://github.com/EnderDom";
	public static String description = "Eddie Biology Software was created by Dominic Matthew Wood." +
	" This software manipulates biological data and frequently holds data within databases." +
	" In an attempt to maintain some level of inter-operability the main database framework used is BioSQL.";
	
	public static String ontology_name = "EDDIE_ASSEMBLY_LINKER";
	public static String term_name_id = "Read Alignment";
	public static String term_description = "Generic Term identifies the alignment of read (subject) to contig (object)";
	
	public static String assmbledread = "Assembled Read";
	
	public static String assemblerdescription = "This Term is a unique for this Assembler";

	public boolean addEddie2Database(DatabaseManager dbman);

	public boolean addLegacyVersionTable(DatabaseManager dbman, String version, String dbversion);
	
	/**
	 * Returns the database version from the 'info' tool
	 * 
	 * @param con
	 * @return double value of the database
	 * returns -1 if version could not be retrieved
	 */
	public double getDatabaseVersion(DatabaseManager dbman);
	
	public int getEddieFromDatabase(DatabaseManager dbman);
	
	public boolean addBioEntrySynonymTable(DatabaseManager dbman);
	
	public boolean addBioentryDbxrefCols(DatabaseManager dbman);
	
	public boolean addRunTable(DatabaseManager dbman);
	
	public boolean setupAssembly(DatabaseManager dbman);
	
	public boolean addDefaultAssemblyOntology(DatabaseManager dbman);
	
	public boolean addDefaultAssemblyTerm(DatabaseManager dbman);
	
	public boolean addAssemblerTerm(DatabaseManager dbman, String name, String division);
	
	public int getDefaultAssemblyTerm(DatabaseManager dbman);
	
	public int getDefaultAssemblyOntology(DatabaseManager dbman);
	
	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager dbman, String division);
	
	public boolean mapRead2Contig(DatabaseManager dbman, int contig_id, int read_id, int programid, int start, int stop, int strand);
	
	/**
	 * 
	 * @param con
	 * @param bioentry_id
	 * @return int array, will return array of length 0 if no contigs attached
	 * to the contig id or a null object if there was an SQLException
	 */
	public int[] getReads(DatabaseManager manager, int bioentry_id);
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, String division);
	
	public String[] getNamesFromTerm(DatabaseManager manager, String identifier);

	public int getBioEntryId(DatabaseManager manager, String name, boolean fuzzy, int biodatabase_id);
	
	public boolean setDbxref(DatabaseManager manager, int bioentry_id, int dbxref_id, int rank, Double evalue, Integer score, Integer dbxref_startpos,
			Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame);
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int rank);
}
