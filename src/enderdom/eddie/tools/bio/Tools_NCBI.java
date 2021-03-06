package enderdom.eddie.tools.bio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_XML;

/**
 * 
 * @author Dominic Matthew Wood
 * 
 * At the moment I don't haven't written tools for getting 
 * multiple values, as I just don't have the time.
 * So at the moment it is very expensive.
 * Sorry NCBI :(
 * 
 *
 */
public class Tools_NCBI {

	public static String eutils = "eutils.ncbi.nlm.nih.gov";
	public static String esearch = "/entrez/eutils/esearch.fcgi";
	public static String esummary = "/entrez/eutils/esummary.fcgi";
	public static String efetch = "/entrez/eutils/efetch.fcgi";
	public static int ncbi_root_taxon = 1;
	
	/**
	 * 
	 * @param database
	 * @param accession
	 * @return string containg the GI number
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws XMLStreamException
	 */
	public static String getGIFromAccession(NCBI_DATABASE database, String accession)
			throws MalformedURLException, URISyntaxException, XMLStreamException {
		URI uri = new URI("http", eutils, esearch, "db="+database.toString()+"&term="+accession, null);
		URL site = uri.toURL();
		return Tools_XML.getSingleTagFromURL(site, "Id");
	}
	
	/**
	 * 
	 * @param database
	 * @param gi
	 * @return NCBI taxid of that gi
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws XMLStreamException
	 */
	public static String getTaxIDfromGI(NCBI_DATABASE database, String gi)
			throws MalformedURLException, URISyntaxException, XMLStreamException {
		URI uri = new URI("http", eutils, esummary, "db="+database.toString()+"&id="+gi, null);
		URL site = uri.toURL();
		return Tools_XML.getSingleTagFromURLwValue(site, "Item", "TaxId");
	}
	
	/**
	 * 
	 * @param database
	 * @param accession
	 * @return Taxid from the ncbi accession
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws XMLStreamException
	 */
	public static String getTaxIDFromAccession(NCBI_DATABASE database, String accession)
			throws MalformedURLException, URISyntaxException, XMLStreamException{
		return getTaxIDfromGI(database,getGIFromAccession(database,accession));
	}
	
	/**
	 * 
	 * @param database
	 * @param gi
	 * @return SequenceObject containing the sequence and GI as the name
	 * @throws URISyntaxException
	 * @throws UnsupportedTypeException
	 * @throws IOException
	 * @throws EddieException
	 */
	public static SequenceObject getSequencewGI(NCBI_DATABASE database, String gi)
			throws URISyntaxException, UnsupportedTypeException, IOException, EddieException{
		URI uri = new URI("http", eutils, efetch, "db="+database.toString()+"&id="+gi+"&rettype=fasta", null);
		URL site = uri.toURL();
		//Can be done better, this is real haxy
		Fasta fasta = new Fasta();
		fasta.loadFile(site.openStream(), BioFileType.FASTA);
		if(fasta.getNoOfSequences() == 0)throw new EddieException("Failed to retrieve fasta for uniprot " + gi);
		else return fasta.getSequence(0);
	}
	
	/**
	 * 
	 * @param database
	 * @param acc
	 * @return SequenceOBject with the *GI* as the name not accession
	 * @throws URISyntaxException
	 * @throws UnsupportedTypeException
	 * @throws IOException
	 * @throws EddieException
	 * @throws XMLStreamException
	 */
	public static SequenceObject getSequencewAcc(NCBI_DATABASE database, String acc)
			throws URISyntaxException, UnsupportedTypeException, IOException, EddieException, XMLStreamException{
		String gi = getGIFromAccession(database, acc);
		return getSequencewGI(database, gi);
	}
	
	public static String getTagInfoFromGI(NCBI_DATABASE database, String gi, String tag) throws URISyntaxException, MalformedURLException, XMLStreamException{
		URI uri = new URI("http", eutils, efetch, "db="+database.toString()+"&id="+gi+"&rettype=xml", null);
		URL site = uri.toURL();
		return Tools_XML.getSingleTagFromURL(site, tag);
	}
	
	public static String[] getLineageFromGI(NCBI_DATABASE database, String gi) throws MalformedURLException, URISyntaxException, XMLStreamException{
		String s = getTagInfoFromGI(database,gi,"OrgName_lineage");
		return s != null ? s.split(";") : null;
	}
	
	/**
	 * 
	 * @param db enter a blast database and returns a search database
	 * why these are not the same is beyond me
	 * @return searchable database for other NCBI tools
	 */
	public static NCBI_DATABASE getDBfromDB(String db){
		if(db.endsWith("nr")) return NCBI_DATABASE.protein;
		else if (db.endsWith("nt")) return NCBI_DATABASE.nuccore;
		else if (db.toLowerCase().contains("est")) return NCBI_DATABASE.nucest;
		else return NCBI_DATABASE.unknown;
	}
	
	/**
	 * Simple method, tries to get 
	 * @param name
	 */
	public static String getNCBIGi(String name){
		Pattern r = Pattern.compile("gi\\|\\d+\\|");
		Matcher m = r.matcher(name);
		return m.find() ?name.substring(m.start()+3, m.end()-1): null; 
	}
	
	/**
	 * Input blast name string with species in brackets 
	 * ie Sequenc1 [Homo sapeins]
	 * and it will return 
	 * 
	 * H.sapiens
	 * 
	 * Should work on multi species
	 * ie
	 * [Homo sapiens sapiens] -> H.sapien
	 *
	 * 
	 * @param name
	 * @return Specis name abbreviated
	 */
	public static String getSpecies(String name){
		Pattern r = Pattern.compile("\\[\\S+(\\s\\S+)|(\\s\\S+\\s\\S+)\\]");
		Matcher m = r.matcher(name);
		return m.find() ? name.substring(m.start()+2, name.charAt(m.end()-1) == ']'? m.end()-1:m.end()): null;
		
	}
}
