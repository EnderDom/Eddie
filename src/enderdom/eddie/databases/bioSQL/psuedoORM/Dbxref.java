package enderdom.eddie.databases.bioSQL.psuedoORM;

//TODO
public class Dbxref {

	private int dbxref_id;
	private String dbname;
	private String accession;
	private int version;
	private int taxon_id;
	
	public Dbxref(){
		
	}

	public int getDbxref_id() {
		return dbxref_id;
	}

	public void setDbxref_id(int dbxref_id) {
		this.dbxref_id = dbxref_id;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getTaxon_id() {
		return taxon_id;
	}

	public void setTaxon_id(int taxon_id) {
		this.taxon_id = taxon_id;
	}
	
	
}