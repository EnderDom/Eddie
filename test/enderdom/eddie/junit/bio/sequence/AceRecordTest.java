package enderdom.eddie.junit.bio.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACERecord;

public class AceRecordTest {

	private String contig1 = "TAAATATTTATTTATTTGACGGTTCCAGCATCACAG*CAAAAATGTTGCC"+
						"CCATAATTGCTCCGCTCCTGTGCCAGTGTAa*ATCAAGCAAAGTGTTCAT"+
						"CAAGGAAGCAGTAATCa*CGTTAAAACCAGCAGCCACACAAGCATGTACA"+
						"TGCATAAGTTTGGCCGAGCCATAGAGTAGTCTTATaaTATCTTTGGGCCG"+
						"A*GCAAAGaCTGCTt*ACAATTTATTGAa*tGGTTt*ACAGCAGCAATTC"+
						"ATGGACa*CACAttcaacaatttctaaataaaatgcacagctcacaagaa"+
						"aatttaaattaaatataaggaaattaaacattttatttcaaataaattaa"+
						"tggactagacaattttagagaaattctataattttaaaattaatcttcag"+
						"acaaatcaaaaatggcagaCATCAATTATACAGTCGACTCCGCctaactc"+
						"gaac";
	
	@Test
	public void testACERecord() {
		try {
			InputStream in = getClass().getResourceAsStream("data/small.ace");
			ACEFileParser parser = new ACEFileParser(in);
			int count =0;
			while(parser.hasNext()){
				ACERecord record = parser.next();
				String seq = record.getConsensus().getSequence();
				if(count == 0){
					assertEquals(contig1.length(), seq.length());
					assertEquals(contig1, seq);
				}
				count++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Failed to parse file");
		}
	}
	
}
