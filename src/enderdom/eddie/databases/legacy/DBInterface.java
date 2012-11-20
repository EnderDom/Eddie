package enderdom.eddie.databases.legacy;

import enderdom.eddie.bio.blast.BlastxDocumentParser;
import enderdom.eddie.bio.objects.GOTermData;
import enderdom.eddie.bio.xml.XML_Iprscan;

//Still To Finish

public interface DBInterface {
	
	public String[] getAvailableDB();

	public void uploadAssemblySequence(String id_name, String sequence, String blast_pred, String blast2go_pred, int coverage, int query_len, double blast_evalue, int blast_hits, int blast2go_terms, int interpro_terms, int peptide_len);
	
	public void uploadBlast(BlastxDocumentParser file);
	
	public void uploadInterPro(XML_Iprscan file);
	
	public void uploadGOTermData(GOTermData file);
	
}
