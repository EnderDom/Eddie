package tools.bio;

import tools.Tools_String;

public class Tools_Contig {

	public static String[] stripContig(String name){
		int i =-1;
		if((i=name.toLowerCase().indexOf("contig")) != -1 && i < name.length()){
			String sub = name.substring(i, name.length());
			if((i=Tools_String.getLongestInt(sub)) != -1){
				return new String[]{sub, "Contig_"+i, "Contig"+i, "ConsensusfromContig"+i, "contig"+i, "contig_"+i, "isotig"+i};
			}
		}
		return null;
	}
	
}
