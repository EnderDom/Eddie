package enderdom.eddie.tools.bio;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.tools.Tools_File;

public class Tools_Bio_File {
	
	public static BioFileType detectFileType(String filename){
		String[] filesuffix = new String[]{".ace",".sam",
				".fna",".fasta",".fa",".faa",
				".fastq", ".qual", ".aln", ".xml"};
		BioFileType[] filetype = new BioFileType[]{BioFileType.ACE,BioFileType.SAM,
				BioFileType.FASTA,BioFileType.FASTA,BioFileType.FASTA,BioFileType.FASTA,
				BioFileType.FASTQ,BioFileType.QUAL, BioFileType.CLUSTAL_ALN,BioFileType.XML};
		
		for(int i=0; i < filesuffix.length; i++){
			if(filename.endsWith(filesuffix[i])){
				Logger.getRootLogger().debug("Filetype detected as " + filetype[i]);
				BioFileType t = filetype[i];
				if(t == BioFileType.XML){
					//Check Blast first line
					String s = Tools_File.returnLine(filename, 1);
					if(s != null){
						if(s.contains("BlastOutput")) return BioFileType.BLAST_XML;
					}
				}
				return t;
			}
		}
		Logger.getRootLogger().warn("File is not distinguishable by its suffix");
		return BioFileType.UNKNOWN;
	}

	
	
}

