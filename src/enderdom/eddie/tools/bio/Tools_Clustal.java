package enderdom.eddie.tools.bio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.lists.ClustalAlign;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.Tools_Task;
import enderdom.eddie.ui.EddieProperty;
import enderdom.eddie.ui.UI;

public class Tools_Clustal {

	public static void saveAsClustal(File f, SequenceList list) throws IOException{
		FileWriter fstream = new FileWriter(f, false);
		BufferedWriter out = new BufferedWriter(fstream);
		
		int max = Tools_Math.getMaxValue(list.getListOfLens());
		
		
		for(int total =0; total < max;total+=60){
			System.out.println();
			for(int i =0;i < list.getNoOfSequences(); i++){
				StringBuffer s = new StringBuffer();
				if(list.getSequence(i).getIdentifier().length() > 9){
					s.append(list.getSequence(i).getIdentifier().substring(0, 10));
				}
				else {
					s.append(list.getSequence(i).getIdentifier());
					while(s.length() <10)s.append(" ");
				}
				s.append("      ");
				out.write(s.toString());
				out.flush();
				for(int j=0; j < 60; j++){
					if(j+total < list.getSequence(i).getLength()){
					}
				}
				System.out.println();
			}
			System.out.println();
		}
		
		out.close();
	}
	
	public static ClustalAlign getClustalAlign(UI ui, SequenceList list) throws IOException, UnsupportedTypeException{
		File input = File.createTempFile("eddie1", ".fasta");
		list.saveFile(input, BioFileType.FASTA);
		File output = File.createTempFile("eddie2", ".aln");
		String value = ui.getPropertyLoader().getValue(EddieProperty.CLUSTALW2BIN.toString());
		value += " -ALIGN -PWMATRIX=BLOSUM -INFILE="+input.getPath()+" -OUTPUT=CLUSTAL -OUTFILE="+output.getPath();
		Logger.getRootLogger().debug("About to execute cmd "+value+
				" (if this fails check that "+EddieProperty.CLUSTALW2BIN.toString() + " is set to the correct location)");
		StringBuffer[] buf = Tools_Task.runProcess(value, true);
		Logger.getRootLogger().debug("Clustal CLI output: "+buf[0].toString());
		
		if(!output.exists()){
			Logger.getRootLogger().error("Command: "+value+" failed to produce output file");
			return null;
		}
		else{
			return new ClustalAlign(output, BioFileType.CLUSTAL_ALN);
		}
	}
	
}
