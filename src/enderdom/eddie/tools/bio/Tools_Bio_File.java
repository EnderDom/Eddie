package enderdom.eddie.tools.bio;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.BioFileType;

public class Tools_Bio_File {
	
	public static BioFileType detectFileType(String filename){
		String[] filesuffix = new String[]{".ace",".sam",".fna",".fasta",".fastq", ".qual", ".aln"};
		String[] filetype = new String[]{"ACE","SAM","FASTA","FASTA","FASTQ","QUAL", "CLUSTAL"};
		
		for(int i=0; i < filesuffix.length; i++){
			if(filename.endsWith(filesuffix[i])){
				Logger.getRootLogger().debug("Filetype detected as " + filetype[i]);
				return BioFileType.valueOf(filetype[i]);
			}
		}
		Logger.getRootLogger().warn("File is not distinguishable by its suffix");
		return BioFileType.UNKNOWN;
	}
	
}

