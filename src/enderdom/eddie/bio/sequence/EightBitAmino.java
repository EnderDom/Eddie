package enderdom.eddie.bio.sequence;

import java.util.Iterator;

import org.biojava.bio.symbol.Symbol;

import enderdom.eddie.bio.interfaces.SequenceObject;


//TODO 
public class EightBitAmino implements SequenceObject, Iterator<Symbol>{

	String amino;
	String name;
	int iteration =0;
	
	public EightBitAmino(String name, String amino){
		this.name = name;
		this.amino = amino;
		//TODO use eight bits :)
	}

	public boolean hasNext() {
		return false;
	}

	public Symbol next() {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQuality() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSequenceType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getActualLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getLength(){
		return 0;
	}

	public int leftTrim(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int rightTrim(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public SequenceObject[] removeSection(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

}
