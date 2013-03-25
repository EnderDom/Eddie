package enderdom.eddie.tools.bio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.tools.Tools_Math;

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
}
