package enderdom.eddie.tools.bio;

import enderdom.eddie.tools.Tools_String;

public class Tools_Contig {

	public static String[] stripContig(String name){
		int i =-1;
		String r = name;
		if((i=r.indexOf(" "))!=-1)r=r.substring(0, i);
		
		if((i=name.toLowerCase().indexOf("contig")) != -1 && i < name.length()){
			String sub = name.substring(i, name.length());
			if((i=Tools_String.getLongestInt(sub)) != -1){
				return new String[]{r,sub, "Contig_"+i, "Contig"+i, "ConsensusfromContig"+i, "contig"+i, "contig_"+i, "isotig"+i};
			}
		}
		else{
			i=Tools_String.getLongestInt(name);
			if(i != -1){
				return new String[]{r,"contig_"+i, "contig"+i, "consensusfromcontig"+i, "isotig"+i, "sequence"+i, "seq"+i, i+"","sequence_"+i, "seq_"+i, "gi:"+i ,"gi"+i ,i+""};
			}
		}
		return null;
	}
	
}
