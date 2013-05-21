package enderdom.eddie.tools.bio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
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
	
	public static SequenceObject getSequencewGI(NCBI_DATABASE database, String gi)
			throws URISyntaxException, UnsupportedTypeException, IOException, EddieException{
		URI uri = new URI("http", eutils, efetch, "db="+database.toString()+"&id="+gi+"&rettype=fasta", null);
		URL site = uri.toURL();
		Fasta fasta = new Fasta();
		fasta.loadFile(site.openStream(), BioFileType.FASTA);
		if(fasta.getNoOfSequences() == 0)throw new EddieException("Failed to retrieve fasta for uniprot " + gi);
		else return fasta.getSequence(0);
	}
	
	public static SequenceObject getSequencewAcc(NCBI_DATABASE database, String acc)
			throws URISyntaxException, UnsupportedTypeException, IOException, EddieException, XMLStreamException{
		String gi = getGIFromAccession(database, acc);
		return getSequencewGI(database, gi);
	}
}
