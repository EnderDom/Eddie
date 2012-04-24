package gui;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

public class FileOptions extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3166708271020340360L;
	private String[] filetypes = new String[]{"FASTA", "FASTQ","QUAL", "SAM", "BAM"};
	int filetype =-1;

	public FileOptions(String filetypea){
		super();
		for(int i =0; i < filetypes.length; i++){
			if(filetypes[i].equals(filetypea)){
				filetype =i;
				Logger.getRootLogger().debug("Filetype "+filetypea+" is recognised");
				break;
			}
		}
		buildDefault();
	}
	
	private void buildDefault(){
		//Delete
		JMenuItem item = new JMenuItem("Delete");
		this.add(item);
	}
	
	
	public boolean isValid(){
		if(filetype ==-1)return false;
		else return true;
	}
	
}
