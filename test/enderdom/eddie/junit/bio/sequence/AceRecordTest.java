package enderdom.eddie.junit.bio.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACERecord;

public class AceRecordTest {

	private static String contig1 = "TAAATATTTATTTATTTGACGGTTCCAGCATCACAG*CAAAAATGTTGCC"+
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
					assertEquals(5, record.getNoOfReads());
					for(int total =0; total < record.getConsensus().getLength();total+=60){
						System.out.print(record.getConsensus().getIdentifier() + "       ");
						for(int j=0; j < 60; j++){
							if(j+total < record.getConsensus().getLength()){
								System.out.print(record.getConsensus().getSequence().charAt(j+total));
							}
						}
						System.out.println();
						for(int i =0;i < record.getNoOfReads(); i++){
							System.out.print(record.getRead(i).getIdentifier() + "    ");
							for(int j=0; j < 60; j++){
								if(j+total < record.getConsensus().getLength()){
									System.out.print(record.getCharAt(i, j+total, 0));
								}
							}
							System.out.println();
						}
						System.out.println();
					}
					System.out.println(record.getSequence(0).getIdentifier() +" : " + record.getCharAt(0, 0, 0));
					System.out.println(record.getSequence(0).getIdentifier() +" : " + record.getCharAt(0, 1, 1));
					assertEquals(record.getCharAt(0, 1, 1), 'T');
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
