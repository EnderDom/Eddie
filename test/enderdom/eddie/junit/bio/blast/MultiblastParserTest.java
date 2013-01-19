package enderdom.eddie.junit.bio.blast;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import enderdom.eddie.bio.blast.MultiblastParser;
import enderdom.eddie.bio.blast.UniVecBlastObject;
import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.SequenceList;
import enderdom.eddie.tasks.bio.Task_UniVec;
import enderdom.eddie.ui.BasicPropertyLoader;

public class MultiblastParserTest {

	private InputStream in = null;
	private String in2 = null;
	private String in3 = null;
	
	@Before
    public void setup(){
		BasicPropertyLoader.configureProps("test/log4j.properties", "test/log4j.properties");
		BasicPropertyLoader.logger.setLevel(Level.TRACE);
		in = getClass().getResourceAsStream("data/vecscreen.xml");
		in2 = getClass().getResource("data/vecsreen_fasta.fasta").getPath();
		in3 = getClass().getResource("data/vecsreen_fasta.qual").getPath();
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
    public void testDataFileCheck() throws IOException {
		in.mark(128);
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = r.readLine();
		assertNotNull(line);
		in.reset();
    }
    
	@Test
	public void testUniVecRegions(){
		
		try {
			MultiblastParser parser = new MultiblastParser(MultiblastParser.UNIVEC, in);
			int i =-1;
			while(parser.hasNext()){
				i++;
				UniVecBlastObject obj = (UniVecBlastObject)parser.next();
				System.out.println(obj.get("Iteration_query-def")+": Region Count:"+obj.regionCount());
				if(obj.getIterationNumber() == 6){
					System.out.println("Comparing: CAAGGCACACAGGGGATAGG with " + obj.getHspTagContents("Hsp_qseq", 1, 1) );
					assertEquals("CAAGGCACACAGGGGATAGG", obj.getHspTagContents("Hsp_qseq", 1, 1));
					System.out.println("Comparing: 40.527741988586 with " + obj.getHspTagContents("Hsp_bit-score", 1, 1) );
					assertEquals("40.527741988586", obj.getHspTagContents("Hsp_bit-score", 1, 1));
					System.out.println("Comparing: 1.10400757585036 with " + obj.getLowestEValue());
					assertEquals(1.10400757585036,obj.getLowestEValue(), 0.00001);
					assertEquals(1, obj.regionCount());
					assertEquals(110,obj.getRegion(0).getStart(1));
				}
				if(obj.getIterationNumber() == 7){
					System.out.println("Comparing: CAAGGCACACAGGGGATAGG with " + obj.getHspTagContents("Hsp_qseq", 1, 1) );
					assertEquals("CAAGGCACACAGGGGATAGG", obj.getHspTagContents("Hsp_qseq", 1, 1));
					assertEquals(1, obj.regionCount());
					assertEquals(56,obj.getRegion(0).getStart(1));
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
			fail("Failed due to exception");
		}
	}
	
	@Test
	public void testTrimming(){
		try{
			File outfolder = new File(in2).getParentFile();
			if(!outfolder.isDirectory())fail("Outfolder should be a directory");
			SequenceList seq = SequenceListFactory.getSequenceList(in2, in3);
			String comp1 = seq.getSequence("GQYW8I402CYV16").getSequence();
			String comp2 = seq.getSequence("GQYW8I402ET26S").getSequence();
			int[] l1 = seq.getListOfLens();
			String[] files = Task_UniVec.parseBlastAndTrim(new MultiblastParser(MultiblastParser.UNIVEC, in), seq, outfolder.getPath(), BioFileType.FAST_QUAL, 50);
			assertNotNull(files);
			assertEquals(2,files.length);
			if(files != null && files.length == 2) {
				SequenceList list = SequenceListFactory.getSequenceList(files[0], files[1]);
				System.out.println(files[0] +", " + files[1]);
				System.out.println("LIST OF MODDED LENGTHS");
				int[] l2 = list.getListOfLens();
				System.out.println("---");
				System.out.println(list.getSequence("GQYW8I402ET26S").getSequence());
				System.out.println(comp2);
				System.out.println("---");
				System.out.println(list.getSequence("GQYW8I402CYV16").getSequence());
				System.out.println(comp1);
				System.out.println("---");
				for(int i =0; i < l1.length; i++){
					System.out.println(l1[i] + " -> " +l2[i]);
				}
			}
			else{
				fail("Did not create files");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			fail("Failed due to exception");
		}
	}

	
}

