package enderdom.eddie.tools.bio;

import java.io.IOException;
import java.net.URL;

import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

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
}
