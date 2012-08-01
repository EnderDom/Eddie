package tools.bio;

import org.apache.log4j.Logger;

public class Tools_Bio_File {
	
	public static String detectFileType(String filename){
		String[] filesuffix = new String[]{".ace",".sam",".fna",".fasta","fastq"};
		String[] filetype = new String[]{"ACE","SAM","FASTA","FASTA","FASTQ"};
		String filetype_val = "UNKNOWN";
		for(int i=0; i < filesuffix.length; i++){
			if(filename.endsWith(filesuffix[i])){
				filetype_val = filetype[i];
				break;
			}
		}
		Logger.getRootLogger().debug("Filetype detected as " + filetype_val);
		return filetype_val;
	}
	
}
