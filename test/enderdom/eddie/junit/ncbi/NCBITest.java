package enderdom.eddie.junit.ncbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import enderdom.eddie.databases.bioSQL.psuedoORM.Taxonomy;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;

public class NCBITest {

	@Test
	public void checkNCBIGetGI(){
		String accession = "0507192A";
		String expectedGI = "223101";
		try{
			assertEquals(expectedGI,Tools_NCBI.getGIFromAccession(NCBI_DATABASE.protein, accession));
		}
		catch(Exception e){
			e.printStackTrace();
			fail("Failed to get gi");
		}		
	}
	
	@Test
	public void checkNCBITaxid(){
		String GI = "223101";
		String expect = "9940";
		try{
			assertEquals(expect,Tools_NCBI.getTaxIDfromGI(NCBI_DATABASE.protein, GI));
		}
		catch(Exception e){
			e.printStackTrace();
			fail("Failed to get gi");
		}		
	}
	
	@Test
	public void testTaxonomyLoad(){
		Taxonomy t =  new Taxonomy("9940");
		assertEquals(t.getCommonname(), "sheep");
		assertEquals(t.getSciencename(), "Ovis aries");
	}
}
