package databases.legacy;

public interface DBBuilder {
	
	public String[] ListTables2Build();
	
	public void builddb(String name);
	
	public void buildDBFull();

	public void buildBioDesktopInfoTable(double version);
	
	public void buildAssembledSequenceTable();
	
	public void buildBlastSequenceTable();
	
	public void buildHitTable();
	
	public void buildHspTable();
	
	public void buildNCBIGenesTable();
	
	public void buildGoTermTable();
	
	public void buildGoTermAltTable();
	
	public void buildGoMatchTable();
	
	public void buildGOLinkTable();
	
	public void buildESTTable();
	
	public void buildInterproTermsTable();
	
	public void buildInterproMatchesTable();
	
	public void buildInterproMatchesLocTable();
	
	public void buildKeywordsTable();
	
	public void buildKeywordsLinkTable();
	
	public void buildSpeciesTable();
	
	public void buildPhylaTable();
	
	public void buildImageTable();
}
