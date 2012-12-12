package enderdom.eddie.junit.bio.blast;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import enderdom.eddie.bio.blast.MultiblastParser;
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
				parser.next();
				if(i > 10){
					fail("Shouldn't parse this far");
				}
			}
			assertEquals(9, i);		
			System.out.println("End position:" + parser.getCurrentPosition());
			parser.close();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
}
