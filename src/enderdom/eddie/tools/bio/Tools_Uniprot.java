package enderdom.eddie.tools.bio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_XML;

public class Tools_Uniprot {

	public static String uniprot = "http://www.uniprot.org/uniprot/";
	
	/**
	 * 
	 * @param uid uniprot name which should be accessible through http eg: http://www.uniprot.org/uniprot/O16980
	 * @return uniprot as a sequenceobejct 
	 * @throws EddieException
	 * @throws UnsupportedTypeException
	 * @throws IOException
	 */
	public static SequenceObject getUniprot(String uid) throws EddieException, UnsupportedTypeException, IOException{
		URL site = new URL(uniprot+uid+".fasta");
		Fasta fasta = new Fasta();
		fasta.loadFile(site.openStream(), BioFileType.FASTA);
		if(fasta.getNoOfSequences() == 0)throw new EddieException("Failed to retrieve fasta for uniprot " + uid);
		else return fasta.getSequence(0);
	}
	
	//Quick method, would be better not to call uniprot twice
	public static UNIPROT_EXIST getExistLvl(String uid) throws XMLStreamException, MalformedURLException{
		URL site = new URL(uniprot+uid+".xml");
		String uni = Tools_XML.getSingleAttrValuefromURL(site, "{http://uniprot.org/uniprot}proteinExistence", 0);
		if(uni == null) return UNIPROT_EXIST.uncertain;
		else if(uni.contains("protein"))return UNIPROT_EXIST.protein;
		else if(uni.contains("transcript"))return UNIPROT_EXIST.transcript;		
		else if (uni.contains("homology"))return UNIPROT_EXIST.homology;
		else if (uni.contains("Predicted"))return UNIPROT_EXIST.predicted;
		else return UNIPROT_EXIST.uncertain;
	}
}
