package enderdom.eddie.tools.bio;

import java.io.BufferedWriter;
import java.io.IOException;

import enderdom.eddie.tools.Tools_Array;
import enderdom.eddie.tools.Tools_String;

public class Tools_Fasta {
	
	public static String newline = System.getProperty("line.separator");
	/*
	 * Convert from fastq ascii quality into
	 * qual file integer quality, separated by
	 * whitespace 
	 */
	public static String Fastq2Qual(String qual){
		StringBuilder phrap = new StringBuilder();
		for(int i =0; i < qual.length(); i++){
			int ascii = (int)qual.charAt(i)-33;
			if(ascii < 10){
				phrap.append(ascii);
				phrap.append("  ");
			}
			else{
				phrap.append(ascii);
				phrap.append(" ");
			}
		}
		return phrap.toString();
	}
	
	public static String Fastq2QualwNewline4ACE(String qual, int linesize){
		StringBuilder phrap = new StringBuilder();
		int line = 0;
		phrap.append(" ");
		for(int i =0; i < qual.length(); i++){
			int ascii = (int)qual.charAt(i)-33;
			if(ascii < 10){
				phrap.append(ascii);
				phrap.append("  ");
			}
			else{
				phrap.append(ascii);
				phrap.append(" ");
			}
			if(phrap.length() > linesize+line){
				line = phrap.length();
				phrap.append(newline);
				phrap.append(" ");
			}
		}
		return phrap.toString();
	}
	

	public static String QualwNewline(String qual, int linelength) {
		StringBuilder build = new StringBuilder();
		int start=0;
		int end=linelength;
		while(end < qual.length()){
			while(qual.charAt(end)!=' ' &&  end < qual.length()){
				end++;
			}
			build.append(qual.substring(start,end));
			start = end;
			end+=linelength;
		}
		build.append(qual.substring(start));
		return build.toString();
	}
	
	public static String Fastq2QualwNewline(String qual, int linesize){
		StringBuilder phrap = new StringBuilder();
		int line = 0;
		for(int i =0; i < qual.length(); i++){
			int ascii = (int)qual.charAt(i)-33;
			if(ascii < 10){
				phrap.append(ascii + "  ");
			}
			else{
				phrap.append(ascii + " ");
			}
			if(phrap.length() > linesize+line){
				phrap.append(newline);
				line = phrap.length();
			}
		}
		return phrap.toString();
	}
	
	
	/*
	 * Converts quality string into int array then into
	 *  sanger fastq style ascii quality string
	 */
	
	public static String Qual2Fastq(String quality){
		int[] arr = getSeqQualAsIntArray(quality);
		return converArray2Phrap(arr);
	}
	
	public static String converArray2Phrap(int[] arr){
		StringBuilder q = new StringBuilder();
		for(int i =0; i < arr.length; i++)q.append((char)(arr[i]+33));	
		return q.toString();
	}
	
	/*
	 * Converts the integer qualities from a .qual file 
	 * into an actual integer array
	 */
	
	public static int[] getSeqQualAsIntArray(String quality){
		quality = quality.trim().replaceAll("\\s+", " ");
		quality.replaceAll(" +", " ");
		String[] qul = quality.split(" ");
		int[] arr = new int[qul.length];
		for(int i =0; i < qul.length; i++){
			Integer j = Tools_String.parseString2Int(qul[i]);
			if(j != null)arr[i] = j;
			
		}
		return Tools_Array.IntArrayTrimAll(arr,-1);
	}
	
	public static void saveFastq(String description, String sequence, String quality, BufferedWriter out) throws IOException{
		out.write("@"+description+newline);
		out.flush();
		writeFasta(out, sequence, false);
		out.write("+"+description+newline);
		out.flush();
		writeFasta(out, quality, false);
	}
	
	public static void saveFasta(String description, String seqOrQual, BufferedWriter out) throws IOException{
		out.write(">"+description+newline);
		out.flush();
		writeFasta(out, seqOrQual, true);
	}
	
	public static void writeFasta(BufferedWriter out, String towrite, boolean split) throws IOException{
		if(split)out.write(Tools_String.splitintolines(Tools_String.fastadefaultlength, towrite));
		else out.write(towrite + newline);
		out.flush();
	}
	
	public static boolean checkFastq(String description, String sequence, String quality){
		if(sequence == null || quality == null || description == null){
			return false;
		}
		else if(sequence.length() != quality.length()){
			return false;
		}
		else return true;
	}
	
	
	public static String getEmptyQualasPhred(int l){
		StringBuilder b = new StringBuilder();
		while(l>0){
			b.append('!');
			l--;
		}
		return b.toString();
	}
	
	public static String getEmptyQual(int l){
		StringBuilder b = new StringBuilder();
		while(l>0){
			b.append("0 ");
			l--;
		}
		return b.toString();
	}

}
