package databases.bioSQL.interfaces;

import java.sql.Connection;
import java.util.HashMap;

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

	public boolean addEddie2Database(Connection con);

	public boolean addLegacyVersionTable(Connection con, String version, String dbversion);
	
	public double getDatabaseVersion(Connection con);
	
	public int getEddieFromDatabase(Connection con);
	
	public boolean addBioEntrySynonymTable(Connection con);
	
	public boolean addBioentryDbxrefCols(Connection con);
	
	public boolean setupAssembly(BioSQL boss, Connection con);
	
	public boolean addDefaultAssemblyOntology(BioSQL boss, Connection con);
	
	public boolean addDefaultAssemblyTerm(BioSQL boss, Connection con);
	
	public boolean addAssemblerTerm(BioSQL boss, Connection con, String name, String division);
	
	public int getDefaultAssemblyTerm(BioSQL boss, Connection con);
	
	public int getDefaultAssemblyOntology(BioSQL boss, Connection con);
	
	public HashMap<String, String>getContigNameNIdentifier(Connection con, String division);
	
	public boolean mapRead2Contig(Connection con, BioSQL boss, int contig_id, int read_id, int programid, int start, int stop, int strand);
	
	/**
	 * 
	 * @param con
	 * @param bioentry_id
	 * @return int array, will return array of length 0 if no contigs attached
	 * to the contig id or a null object if there was an SQLException
	 */
	public int[] getReads(Connection con, int bioentry_id);
	
	public int getContigFromRead(Connection con, int bioentry_id, String division);
	
	public String[] getNamesFromTerm(Connection con, String identifier);

	public int getBioEntryId(BioSQL boss, Connection con, String name, boolean fuzzy, int biodatabase_id);
	
	
}
