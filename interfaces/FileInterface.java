package interfaces;

import bioobjects.BioFile;

public interface FileInterface {
	
	public BioFile[] getAllFiles();

	public BioFile[] getFilesofType(String filetypes);
	
	
	
}
