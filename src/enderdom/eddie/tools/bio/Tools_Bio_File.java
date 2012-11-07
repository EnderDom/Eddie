package enderdom.eddie.tools.bio;

import org.apache.log4j.Logger;

public class Tools_Bio_File {
	
	public static String SAM = "SAM";
	public static String ACE = "ACE";
	public static String FASTA = "FASTA";
	public static String FASTQ = "FASTQ";
	public static String QUAL = "QUAL";
	public static String CLUSTAL ="CLUSTAL";
	public static String UNKNOWN = "UNKNOWN";
	
	
	public static String detectFileType(String filename){
		String[] filesuffix = new String[]{".ace",".sam",".fna",".fasta",".fastq", ".qual", ".aln"};
		String[] filetype = new String[]{ACE,SAM,FASTA,FASTA,FASTQ,QUAL, CLUSTAL};
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

