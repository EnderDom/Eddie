package enderdom.eddie.databases.bioSQL.psuedoORM;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;


public class Taxonomy {

	private int biosql_taxid;
	private Integer ncbi_taxid;
	private String node_rank;
	private String sciencename;
	private String commonname;
	private Integer genetic_code;
	private Integer mitogenetic_code;
	private Integer parent_taxid;
	private Integer left_value;
	private Integer right_value;
	private LinkedList<Taxonomy> othertaxes;
	
	private Logger logger = Logger.getRootLogger();
	
	public Taxonomy(){
		othertaxes = new LinkedList<Taxonomy>();
	}
	
	public Taxonomy(String taxid){
		othertaxes = new LinkedList<Taxonomy>();
		this.ncbi_taxid = Tools_String.parseString2Int(taxid);
		if(this.ncbi_taxid == null || this.ncbi_taxid <1 )logger.warn(taxid + " is not a valid ncbi taxid ");
		else{
			init();
		}
	}
	
	public Taxonomy(int taxid){
		othertaxes = new LinkedList<Taxonomy>();
		this.ncbi_taxid = taxid;
		if(this.ncbi_taxid <1 )logger.warn(taxid + " is not a valid ncbi taxid ");
		else{
			init();
		}
	}
	
	public void init(){
		try{
			URI uri = new URI("http", Tools_NCBI.eutils, Tools_NCBI.efetch, "db="+NCBI_DATABASE.taxonomy+"&id="+this.getNcbi_taxid(), null);
			URL site = uri.toURL();
			XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
		    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		    XMLStreamReader2 stream = (XMLStreamReader2) f.createXMLStreamReader(site);
		    boolean othertax=false;
		    Taxonomy tax = new Taxonomy();
			while(stream.hasNext()){
			    stream.next();
			    if(stream.isStartElement()){
			    	String xmtag = stream.getName().toString();
			    	//Could deal with more names but can't be bothered
			    	if(!othertax){
			    		if(xmtag.equals("ScientificName")){
							this.setSciencename(stream.getElementText());
						}
						else if(xmtag.equals("GenbankCommonName")){
							this.setCommonname(stream.getElementText());
						}
						else if(xmtag.equals("CommonName") && this.getCommonname() == null){
							this.setCommonname(stream.getElementText());
						}
						else if(xmtag.equals("ParentTaxId")){
							this.setParent_taxid(Tools_String.parseString2Int(stream.getElementText()));
						}
						else if(xmtag.equals("Rank")){
							this.setNode_rank(stream.getElementText());
						}
						else if(xmtag.equals("GCId")){
							this.setGenetic_code(Tools_String.parseString2Int(stream.getElementText()));
						}
						else if(xmtag.equals("MGCId")){
							this.setMitogenetic_code(Tools_String.parseString2Int(stream.getElementText()));
						}
						else if(xmtag.equals("LineageEx")){
							othertax=true;
						}
			    	}
			    	else{
						if(xmtag.equals("TaxId")){
							tax.setNcbi_taxid(Tools_String.parseString2Int(stream.getElementText()));
						}
						else if(xmtag.equals("ScientificName")){
							tax.setSciencename(stream.getElementText());
						}
						else if(xmtag.equals("Rank")){
							tax.setNode_rank(stream.getElementText());
						}
			    	}
			    }
			    else if(stream.isEndElement()){
			    	String xmtag = stream.getName().toString();
			    	if(xmtag.equals("Taxon") && othertax){
			    		if(tax.getNcbi_taxid() != null && tax.getNcbi_taxid() > 0){
			    			othertaxes.add(tax);
			    		}
			    		tax = new Taxonomy();
			    	}
			    }
			}
		}
		catch(Exception e){
			logger.error("Failed to parse NCBI taxid ", e);
		}
	}
	
	/**
	 * Uploads information to database, only uploads
	 * if update set to true else just get taxon_id
	 * and moves on
	 * 
	 * @param manager
	 * @param update 
	 * @return true if Biosql_taxid is set
	 */
	public boolean upload2DB(DatabaseManager manager, boolean update){
		if((this.biosql_taxid < 1) && (this.ncbi_taxid == null  || this.ncbi_taxid < 1)){
			logger.warn("Will not upload a taxonomy without either a biosql_taxid or ncbi_taxid");
			return false;
		}
		this.setBiosql_taxid(manager.getBioSQL().getTaxonIdwNCBI(manager.getCon(), this.getNcbi_taxid()));
		if(this.getBiosql_taxid() < 1){
			this.setBiosql_taxid(manager.getBioSQL().addTaxon(manager.getCon(), this.getNcbi_taxid(), this.getParent_taxid(),
					this.getNode_rank(), this.getGenetic_code(), this.getMitogenetic_code(), this.getLeft_value(), 
					this.getRight_value(), -1));
			if(this.getBiosql_taxid() > 0){
				if(this.getSciencename() != null){
					manager.getBioSQL().addTaxonName(manager.getCon(), this.getBiosql_taxid(), this.getSciencename(), "ScientificName");
				}
				if(this.getCommonname() != null){
					manager.getBioSQL().addTaxonName(manager.getCon(), this.getBiosql_taxid(), this.getCommonname(), "CommonName");
				}
				if(othertaxes.size() > 0){
					for(Taxonomy T : othertaxes){
						T.upload2DB(manager, false);
					}
				}
				return true;
			}
			return false;
		}
		else if(update){
			this.setBiosql_taxid(manager.getBioSQL().addTaxon(manager.getCon(), this.getNcbi_taxid(), this.getParent_taxid(),
					this.getNode_rank(), this.getGenetic_code(), this.getMitogenetic_code(), this.getLeft_value(), 
					this.getRight_value(), this.getBiosql_taxid()));
			return true;
		}
		else return true;
	}
	
	public int getBiosql_taxid() {
		return biosql_taxid;
	}

	public void setBiosql_taxid(int biosql_taxid) {
		this.biosql_taxid = biosql_taxid;
	}

	public Integer getNcbi_taxid() {
		return ncbi_taxid;
	}

	public void setNcbi_taxid(Integer ncbi_taxid) {
		this.ncbi_taxid = ncbi_taxid;
	}

	public String getNode_rank() {
		return node_rank;
	}

	public void setNode_rank(String node_rank) {
		this.node_rank = node_rank;
	}

	public Integer getMitogenetic_code() {
		return mitogenetic_code;
	}

	public void setMitogenetic_code(Integer mitogenetic_code) {
		this.mitogenetic_code = mitogenetic_code;
	}

	public Integer getParent_taxid() {
		return parent_taxid;
	}

	public void setParent_taxid(Integer parent_taxid) {
		this.parent_taxid = parent_taxid;
	}

	public Integer getLeft_value() {
		return left_value;
	}

	public void setLeft_value(Integer left_value) {
		this.left_value = left_value;
	}

	public Integer getRight_value() {
		return right_value;
	}

	public void setRight_value(Integer right_value) {
		this.right_value = right_value;
	}

	public Integer getGenetic_code() {
		return genetic_code;
	}

	public void setGenetic_code(Integer genetic_code) {
		this.genetic_code = genetic_code;
	}

	public String getSciencename() {
		return sciencename;
	}

	public void setSciencename(String sciencename) {
		this.sciencename = sciencename;
	}

	public String getCommonname() {
		return commonname;
	}

	public void setCommonname(String commonname) {
		this.commonname = commonname;
	}

	
}
