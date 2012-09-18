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
				phrap.append(ascii + "  ");
			}
			else{
				phrap.append(ascii + " ");
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
}
