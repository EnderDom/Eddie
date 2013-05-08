package enderdom.eddie.junit.bio.sequence;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import enderdom.eddie.bio.homology.ClustalAlign;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

public class ClustalTest {
	
	@Test
	public void testClustalParse(){
	InputStream in = getClass().getResourceAsStream("data/test.aln");
		
		try {
			ClustalAlign img = new ClustalAlign(in, BioFileType.CLUSTAL_ALN);
			assertEquals(11, img.getNoOfSequences());
			assertEquals("Aplysia", img.getSequence(0).getIdentifier());
			
		} catch (UnsupportedTypeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
