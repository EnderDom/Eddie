package databases.bioSQL.interfaces;

import java.sql.Connection;

public interface BioSQLExtended {

	public static String programname = "EddieBiologySoftwareSuite";
	public static String authority = "https://github.com/EnderDom";
	public static String description = "Eddie Biology Software was created by Dominic Matthew Wood." +
	" This software manipulates biological data and frequently holds data within databases." +
	" In an attempt to maintain some level of inter-operability the main database framework used is BioSQL.";
	
	public static String ontology_name = "EDDIE_ASSEMBLY_LINKER";
	public static String term_name_id = "Read Alignment";
	public static String term_name = "Generic Term identifies the alignment of read (subject) to contig (object)";
	
	public static String assmbledread = "Assembled Read";
	
	public static String assemblerdescription = "This Term is a unique for this Assembler";

	public boolean addEddie2Database(Connection con);

	public boolean addLegacyVersionTable(Connection con, String version, String dbversion);
	
	public double getDatabaseVersion(Connection con);
	
	public int getEddieFromDatabase(Connection con);
	
	public boolean addBioEntrySynonymTable(Connection con);
	
	public boolean setupAssembly(BioSQL boss, Connection con);
	
	public boolean addAssemblyOntology(BioSQL boss, Connection con);
	
	public boolean addAssemblyTerm(BioSQL boss, Connection con);
	
	public boolean addAssemblerTerm(BioSQL boss, Connection con, String name);
	
	public int getDefaultAssemblyTerm(BioSQL boss, Connection con);
	
	public int getDefaultAssemblyOntology(BioSQL boss, Connection con);
	
	public boolean mapRead2Contig(Connection con, BioSQL boss, int contig_id, int read_id, int programid, int start, int stop, int strand);
}
