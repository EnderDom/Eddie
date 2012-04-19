package databases.bioSQL.interfaces;

import java.sql.Connection;

public interface BioSQLExtended {

	public static String programname = "EddieBiologySoftwareSuite";
	public static String authority = "https://github.com/EnderDom";
	public static String description = "Eddie Biology Software was created by Dominic Matthew Wood." +
	" This software manipulates biological data and frequently holds data within databases." +
	" In an attempt to maintain some level of inter-operability the main database framework used is BioSQL.";
	
	public static String assembly = "EDDIE_ASSEMBLY_LINKER";
	public static String ont_description = "Eddie Linker Reference, used to to link Assembly terms";
	

	public boolean addEddie2Database(Connection con);

	public boolean addLegacyVersionTable(Connection con, String version, String dbversion);
	
	public double getDatabaseVersion(Connection con);
	
	public int getEddieFromDatabase(Connection con);
	
	public boolean addBioEntrySynonymTable(Connection con);
	
	public boolean setupAssembly(BioSQL boss, Connection con);
	
	public boolean addAssemblyOntology(BioSQL boss, Connection con);
	
	public boolean addAssemblyTerm(BioSQL boss, Connection con, int ontology_id);
	
	public int getDefaultAssemblyTerm(BioSQL boss, Connection con);
}
