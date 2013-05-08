package enderdom.eddie.bio.homology;

public class PantherGene {

	private String shortSpecies;
	private String dbLink;
	private String geneAcc;
	private String geneName;
	private String geneSymbol;
	
	
	public PantherGene(){
		
	}

	public String getShortSpecies() {
		return shortSpecies;
	}

	public void setShortSpecies(String shortSpecies) {
		this.shortSpecies = shortSpecies;
	}

	public String getGeneAcc() {
		return geneAcc;
	}

	public void setGeneAcc(String geneAcc) {
		this.geneAcc = geneAcc;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	
	
	
	public String getDbLink() {
		return dbLink;
	}

	public void setDbLink(String dbLink) {
		this.dbLink = dbLink;
	}

	public String getGeneSymbol() {
		return geneSymbol;
	}

	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}

	public String getFlybase(){
		return getWithDBCode("FB=");
	}
	
	public String getWormbase(){
		return getWithDBCode("WB=");
	}
	
	public String getEnsemble(){
		return getWithDBCode("ENSEMBL=");
	}
	
	public String getWithDBCode(String dbcode){
		int i=0;
		if((i=dbLink.indexOf(dbcode)) != -1)return dbLink.substring(dbLink.indexOf(i+3),dbLink.length());
		else return null;
	}
	
	public String getUniprot(){
		int i = geneAcc.indexOf("UniProtKB=");
		if(i == -1)return null;
		i+=new String("UniProtKB=").length();
		return geneAcc.substring(i, geneAcc.length());
	}

	public String getNCBI() {
		int i = geneAcc.indexOf("NCBI=");
		if(i == -1)return null;
		i+=new String("NCBI=").length();
		return geneAcc.substring(i, geneAcc.length());
	}
	
	
	
}
