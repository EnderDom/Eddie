package enderdom.eddie.bio.assembly;

import java.io.File;
import java.util.Set;

import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.Contig;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

public class SAMRecordWrapper implements Contig{

	
	
	@Override
	public int getN50() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getListOfLens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getListOfActualLens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNoOfMonomers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getQuickMonomers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNoOfSequences() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SequenceObject getSequence(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SequenceObject getSequence(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canAddSequenceObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addSequenceObject(SequenceObject object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRemoveSequenceObjects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeSequenceObject(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] saveFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int loadFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BioFileType getFileType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SequenceObject next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SequenceObject getConsensus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int trimLeftAllContig() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int trimRightAllContig() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Contig[] removeSectionAllContig(int opts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCoverageAtBp(int i, int base) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getCharAt(int sequencenumber, int position, int base) {
		// TODO Auto-generated method stub
		return 0;
	}

}
