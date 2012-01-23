package tools.bio;

import tools.Tools_Array;
import tools.Tools_String;

public class Tools_Fasta {
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
			arr[i] = Tools_String.parseString2Int(qul[i]);
		}
		return Tools_Array.IntArrayTrimAll(arr,-1);
	}
}
