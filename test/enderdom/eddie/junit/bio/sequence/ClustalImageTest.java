package enderdom.eddie.junit.bio.sequence;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;


import org.junit.Test;

import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.graphics.bio.ClustalImage;

public class ClustalImageTest {

	@Test
	public void testImage(){
		InputStream in = getClass().getResourceAsStream("data/test.aln");
		
		try {
			ClustalImage img = new ClustalImage(in, BioFileType.CLUSTAL_ALN);
			File f = File.createTempFile("image", ".png");
			img.drawImage(f);
			if(!f.exists()){
				fail("Failed to create image");
			}
			else{
				System.out.println("Image @" + f.getPath());
			}
		} catch (UnsupportedTypeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
