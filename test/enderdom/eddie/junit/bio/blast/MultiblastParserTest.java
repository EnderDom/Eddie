package enderdom.eddie.junit.bio.blast;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import enderdom.eddie.bio.blast.MultiblastParser;
import enderdom.eddie.bio.objects.BlastObject;
import enderdom.eddie.ui.BasicPropertyLoader;

public class MultiblastParserTest {

	private InputStream in = null;
	
	@Before
    public void setup(){
		BasicPropertyLoader.configureProps("test/log4j.properties", "test/log4j.properties");
		BasicPropertyLoader.logger.setLevel(Level.DEBUG);
		in = getClass().getResourceAsStream("data/vecscreen.xml");
    }

    @After
    public void teardown(){
    	if (in != null){
            try {
				in.close();
			} 
            catch (IOException e) {
				e.printStackTrace();
			}
        }
        in = null;
    }
	
	@Test
	public void test() throws IOException {
		in.mark(128);
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = r.readLine();
		assertNotNull(line);
		in.reset();
		try {
			MultiblastParser parser = new MultiblastParser(in);
			int i =-1;
			while(parser.hasNext()){
				i++;
				BlastObject obj = parser.next();
				if(obj.getIterationNumber() == 6){
					System.out.println("Comparing: CAAGGCACACAGGGGATAGG with " + obj.getHspTagContents("Hsp_qseq", 1, 1) );
					assertEquals("CAAGGCACACAGGGGATAGG", obj.getHspTagContents("Hsp_qseq", 1, 1));
					System.out.println("Comparing: 40.527741988586 with " + obj.getHspTagContents("Hsp_bit-score", 1, 1) );
					assertEquals("40.527741988586", obj.getHspTagContents("Hsp_bit-score", 1, 1));
					System.out.println("Comparing: 1.10400757585036 with " + obj.getLowestEValue());
					assertEquals(1.10400757585036,obj.getLowestEValue(), 0.00001);
				}
				if(i > 10){
					fail("Shouldn't parse this far");
				}
			}
			assertEquals(9, i);		
			System.out.println("End position:" + parser.getCurrentPosition());
			parser.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
