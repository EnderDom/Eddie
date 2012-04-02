package tools.bio;

import java.util.Arrays;

import tools.Tools_Math;

public class Tools_Sequences {
	
	/*
	 * Source code taken from 
	 * 
	 * http://asap.ahabs.wisc.edu/mauve/mauve-developer-guide/mauve-development-overview.html
	 * 
	 * which appears to be under the GPL (v3) licence 
	 * 
	 * Returns stats for an array of lengths
	 * presumably from a fasta or assembly file
	 * [0] Sum of Lengths (no of bp)
	 * [1] min length
	 * [2] max length
	 * [3] n50
	 * [4] n90
	 */
	public static long[] SequenceStats(int[] arr_of_lengths){
		Arrays.sort(arr_of_lengths);
		long[] ret = new long[5];
		for(int k : arr_of_lengths)ret[0]+=k;
		int i=0;
		ret[1] = arr_of_lengths[0];
		ret[2] = arr_of_lengths[arr_of_lengths.length-1];
		long cur = 0;
		for(i=arr_of_lengths.length-1; i>=0 && cur*2 < ret[0]; i--){
			cur += arr_of_lengths[i];
		}
		ret[3]= arr_of_lengths[i];
		for(; i>0 && cur < ret[0]*0.9d;){
			cur += arr_of_lengths[--i];
		}
		ret[4] = arr_of_lengths[i];
		return ret;
	}
	
	public static int n50(int[] arr_of_lengths){
		Arrays.sort(arr_of_lengths);
		long sum = Tools_Math.sum(arr_of_lengths);
		long cur = 0;
		int i=0;
		for(i=arr_of_lengths.length-1; i>=0 && cur*2 < sum; i--){
			cur += arr_of_lengths[i];
		}
		return arr_of_lengths[i];
	}
}
