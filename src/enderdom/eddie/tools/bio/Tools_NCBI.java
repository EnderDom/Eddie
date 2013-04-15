package enderdom.eddie.tools.bio;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import enderdom.eddie.tools.Tools_XML;


public class Tools_NCBI {

	public static String eutils = "eutils.ncbi.nlm.nih.gov";
	public static String esearch = "/entrez/eutils/esearch.fcgi";
	public static String esummary = "/entrez/eutils/esummary.fcgi";
	public static String efetch = "/entrez/eutils/efetch.fcgi";
	public static int ncbi_root_taxon = 1;
	
	//Database can be such as
	public static String getGIFromAccession(NCBI_DATABASE database, String accession)
			throws MalformedURLException, URISyntaxException, XMLStreamException {
		URI uri = new URI("http", eutils, esearch, "db="+database.toString()+"&term="+accession, null);
		URL site = uri.toURL();
		return Tools_XML.getSingleTagFromURL(site, "Id");
	}
	
	public static String getTaxIDfromGI(NCBI_DATABASE database, String gi)
			throws MalformedURLException, URISyntaxException, XMLStreamException {
		URI uri = new URI("http", eutils, esummary, "db="+database.toString()+"&id="+gi, null);
		URL site = uri.toURL();
		return Tools_XML.getSingleTagFromURL(site, "Item", "TaxId");
	}
	
	public static String getTaxIDFromAccession(NCBI_DATABASE database, String accession)
			throws MalformedURLException, URISyntaxException, XMLStreamException{
		return getTaxIDfromGI(database,getGIFromAccession(database,accession));
	}
	
	
	
}
