package enderdom.eddie.bio.factories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.bio.Tools_Bio_File;

public class SequenceListFactory {

	public SequenceListFactory(){
		
	}
	
	/**
	 * Task in general is supposed to remove some
	 * of the cludge in Tasks for creating bio objects
	 * by moving the cludge here and adding extra layers
	 * of abstraction so you have no idea whats going on.
	 * Enjoy :) 
	 * 
	 * Dominic
	 * 
	 * @param input String path of a file to load
	 * @return A SequenceList object which you can manipulate
	 * @throws FileNotFoundException
	 * @throws UnsupportedTypeException
	 * @throws IOException
	 */
	public static SequenceList getSequenceList(String input) throws FileNotFoundException, UnsupportedTypeException, IOException{
		File i = new File(input);
		if(!i.exists()){
			throw new FileNotFoundException("File: " + input + " does not exist");
		}
		else if(i.isDirectory()){
			throw new FileNotFoundException("File: " + input + " is a directory");//TODO support directory
		}
		else{
			BioFileType filetype = Tools_Bio_File.detectFileType(i.getName());
			Fasta f;
			switch(filetype){
				case FASTA:
					f = new Fasta();
					f.loadFile(i, filetype);
					return f;
				case FASTQ:
					f = new Fasta();
					f.loadFile(i, filetype);
					return f;
				case QUAL:
					f = new Fasta();
					f.loadFile(i, filetype);
					return f;
				case ACE:
					f = new Fasta();
					ACEFileParser parser = new ACEFileParser(new File(input));
					while(parser.hasNext()){
						f.addSequenceObject(parser.next().getConsensus());
					}
					return f;
				//case SAM://TODO this method	
				default:
				throw new UnsupportedTypeException("You are trying to get a sequence list from " 
						+ filetype.toString() + " which is not yet supported");	
			}
		}
	}
	
	public static SequenceList getSequenceList(String input, String input2) throws FileNotFoundException, UnsupportedTypeException, IOException{
		File i = new File(input);
		File i2 = new File(input2);
		if(!i.exists() || !i2.exists()){
			throw new FileNotFoundException("File: " + input + " does not exist");
		}
		else if(i.isDirectory() || i2.isDirectory()){
			throw new FileNotFoundException("File: " + input + " is a directory");
		}
		else{
			BioFileType f1 = Tools_Bio_File.detectFileType(i.getName());
			BioFileType f2 = Tools_Bio_File.detectFileType(i2.getName());
			if((f1 == BioFileType.FASTA && f2 == BioFileType.QUAL) || (f1 == BioFileType.QUAL && f2 == BioFileType.FASTA)){
				Fasta f = new Fasta();
				f.loadFile(i, f1);
				f.loadFile(i2, f2);
				return f;
			}
//			else if((f1 == BioFileType.ACE && f2 == BioFileType.FASTA)){
//				//TODO
//			}
			else{
				throw new UnsupportedTypeException("Cannot generate Sequence list from " 
			+ f1.toString() + " and " + f2.toString() + " filetypes");
			}
		}
	}
	
	public static SequenceList buildSequenceList(BioFileType type) throws UnsupportedTypeException{
		switch(type){
			case FASTQ:
				Fasta f = new Fasta();
				f.setFastq(true);
				return f;
			case FASTA:
				return new Fasta();
			case FAST_QUAL:
				return new Fasta();
			default:
				throw new UnsupportedTypeException("Builder for this filetype not yet implemented");
		}
	}

}
