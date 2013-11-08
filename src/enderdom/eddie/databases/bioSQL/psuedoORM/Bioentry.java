package enderdom.eddie.databases.bioSQL.psuedoORM;

public class Bioentry {
	private int bioentry_id;
	private int biodatabase_id;
	private int taxon_id;
	private String name;
	private String accession;
	private String identifier;
	private String division;
	private String description;
	private int version;
	
	public Bioentry(int bioentry_id, int biodatabase_id, int taxon_id, 
			String name, String accession, String identifier, 
			String division, String description, int version){
		this.bioentry_id = bioentry_id;
		this.biodatabase_id = biodatabase_id;
		this.taxon_id = taxon_id; 	
		this.name =name; 	
		this.accession = accession; 	
		this.identifier = identifier;
		this.division = division; 	
		this.description = description;
		this.version = version;
	}

	public int getBioentry_id() {
		return bioentry_id;
	}

	public void setBioentry_id(int bioentry_id) {
		this.bioentry_id = bioentry_id;
	}

	public int getBiodatabase_id() {
		return biodatabase_id;
	}

	public void setBiodatabase_id(int biodatabase_id) {
		this.biodatabase_id = biodatabase_id;
	}

	public int getTaxon_id() {
		return taxon_id;
	}

	public void setTaxon_id(int taxon_id) {
		this.taxon_id = taxon_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	
	
}
