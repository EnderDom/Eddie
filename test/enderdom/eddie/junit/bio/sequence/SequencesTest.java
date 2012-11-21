package enderdom.eddie.junit.bio.sequence;

import static org.junit.Assert.*;

import org.junit.Test;

import enderdom.eddie.bio.sequence.GenericSequence;

public class SequencesTest {

	@Test
	public void testGetActualLength() {
		String s = "CGATCGATAGC--ATCGATCGA";
		GenericSequence seq = new GenericSequence("Test", s);
		assertEquals("Result", s.length()-2, seq.getActualLength());
	}

	@Test
	public void testGetLength() {
		String s = "CGATCGATAGC--ATCGATCGA";
		GenericSequence seq = new GenericSequence("Test", s);
		assertEquals("Result", s.length(), seq.getLength());
	}

	@Test
	public void testLeftTrim() {
		String s =  "CGATCGATAGC--ATCGATCGA";
		GenericSequence seq = new GenericSequence("Test", s);
		seq.rightTrim(3);
		String s2 = "CGATCGATAGC--ATCGAT";
		assertEquals("Result", s2, seq.getSequence());
	}

	@Test
	public void testRightTrim() {
		String s =  "CGATCGATAGC--ATCGATCGA";
		GenericSequence seq = new GenericSequence("Test", s);
		seq.leftTrim(3);
		String s2 = "TCGATAGC--ATCGATCGA";
		assertEquals("Result", s2, seq.getSequence());
	}

	@Test
	public void testRemoveSection() {
		//          00000000001111111111222
		//          01234567890123456789012
		String s = "CGATCGATAGC--ATCGATCGA";
		GenericSequence seq = new GenericSequence("Test", s);
		;
		String[] s2 = new String[]{"CGATCGATAGC","ATCGATCGA"};
		GenericSequence[] s3 = (GenericSequence[]) seq.removeSection(11, 13);
		assertEquals("Result", s2[0], s3[0].getSequence());
		assertEquals("Result", s2[1], s3[1].getSequence());
	}

	@Test
	public void testInsert() {
		String s = "CGATCGAT";
		GenericSequence seq = new GenericSequence("Test", s);
		GenericSequence seq2 = new GenericSequence("Test2", s);
		seq.insert(0, seq2);
		s= "CGATCGATCGATCGAT";
		assertEquals("Result", s, seq.getSequence());
	}

	@Test
	public void testAppend() {
		String s = "CGATCGAT";
		GenericSequence seq = new GenericSequence("Test", s);
		GenericSequence seq2 = new GenericSequence("Test2", s);
		seq.append(seq2);
		s= "CGATCGATCGATCGAT";
		assertEquals("Result", s, seq.getSequence());
	}

	@Test
	public void testExtendLeft() {
		String s = "CGATCGAT";
		GenericSequence seq = new GenericSequence("Test", s);
		seq.extendLeft(2);
		String s2 = "--CGATCGAT";
		assertEquals("Result", s2, seq.getSequence());
	}

	@Test
	public void testExtendRight() {
		String s = "CGATCGAT";
		GenericSequence seq = new GenericSequence("Test", s);
		seq.extendRight(2);
		String s2 = "CGATCGAT--";
		assertEquals("Result", s2, seq.getSequence());
	}

}
