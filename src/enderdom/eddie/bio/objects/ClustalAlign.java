package enderdom.eddie.bio.objects;

import java.io.File;

import enderdom.eddie.bio.interfaces.BioFileType;
import enderdom.eddie.bio.interfaces.SequenceList;
import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.bio.interfaces.UnsupportedTypeException;

//TODO implement
//@stub
public class ClustalAlign implements SequenceList{

	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public SequenceObject next() {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}

	public int getN50() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int[] getListOfLens() {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getListOfActualLens() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNoOfMolecules() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNoOfSequences() {
		// TODO Auto-generated method stub
		return 0;
	}

	public SequenceObject getSequence(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public SequenceObject getSequence(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean saveFile(File file, BioFileType filetype) throws Exception,
			UnsupportedTypeException {
		// TODO Auto-generated method stub
		return false;
	}

	public int loadFile(File file, BioFileType filetype) throws Exception, UnsupportedTypeException {
		// TODO Auto-generated method stub
		return 0;
	}

}
