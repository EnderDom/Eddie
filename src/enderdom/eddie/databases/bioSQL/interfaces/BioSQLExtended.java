package enderdom.eddie.databases.bioSQL.interfaces;

import java.sql.Date;
import java.util.HashMap;

import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.databases.bioSQL.psuedoORM.BioSequence;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;

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
	
	public boolean addBioentryDbxrefCols(DatabaseManager dbman);
	
	//public boolean addBiosequenceRunID(DatabaseManager dbman);
	
	public boolean addRunTable(DatabaseManager dbman);
	
	public boolean addAssemblyTable(DatabaseManager manager);
	
	public boolean addLegacyVersionTable(DatabaseManager dbman, String version, String dbversion);
	
	public HashMap<String, String>getContigNameNIdentifier(DatabaseManager dbman, int run_id);
	
	public boolean mapRead2Contig(DatabaseManager manager, int contig_id, int read_id, int read_version, int runid, int start, int stop, boolean trimmed);
	
	public String[][] getUniqueStringFields(DatabaseManager manager, String[] fields, String table);
	
	public Run getRun(DatabaseManager manager, int run_id);
	
	public int[] getReads(DatabaseManager manager, int bioentry_id);
	
	public int getContigFromRead(DatabaseManager manager, int bioentry_id, int run_id);

	public int getBioEntryId(DatabaseManager manager, String name, boolean fuzzy, int biodatabase_id);
	
	public boolean setDbxref(DatabaseManager manager, int bioentry_id, int run_id, int dbxref_id, int rank, Double evalue, Integer score, Integer dbxref_startpos,
			Integer dbxref_endpos,Integer dbxref_frame, Integer bioentry_startpos,Integer bioentry_endpos,Integer bioentry_frame);
	
	public boolean existsDbxRefId(DatabaseManager manager, int bioentry_id, int dbxref_id, int run_id, int rank);
	
	public int[] getRunId(DatabaseManager manager, String programname, String runtype);
	
	public BioSequence[] getBioSequences(DatabaseManager manager, int bioentry_id);
	
	public boolean setRun(DatabaseManager manager, Date date, String runtype, String program, String version, String dbname, String params, String comment);

	/**
	 * Retrieve some contig names attached to the run of id 'r'
	 * returns 'i' amount of names as a String[]
	 * @param r
	 * @param i
	 * @return String[] of length i
	 */
	public String[] getContigNames(DatabaseManager manager, int r, int i);

	public SequenceList getContigsAsFasta(DatabaseManager manager, SequenceList l, int i);
	
}
