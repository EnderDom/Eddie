package enderdom.eddie.tools;

import java.util.ArrayList;
import java.util.List;

public abstract class Tools_Array {

	public static String[] mergeStrings(String[] a, String[] b){
		String[] c = new String[a.length+b.length];
		for(int i = 0; i < a.length; i++)c[i] = a[i];			
		for(int i = 0; i < b.length; i++)c[i+a.length] = b[i];
		return c;
	}
	
	public static int[] getUniqueValues(int[] values){
		ArrayList<Integer> ins = new ArrayList<Integer>();
		boolean found =false;
		ins.add(values[values.length-1]);
		for(int i =0;i < values.length-1; i++){
			found = false;
			for(int j = i+1;j < values.length; j++){
				found = (values[i]==values[j]);
				if(found)break;
			}
			if(!found)ins.add(values[i]);
		}
		return ListInt2int(ins);
	}
	
	/*
	 * Eddie 3: some basic testing, 
	 * as yets no bugs found
	 */
	/* replaceAll but for int[] arrays
	 * examples where i == 0 :
	 * int[]{0,0,0,10,0,1} --> {10,1}
	 * int[]{0,0,0}        --> {}
	 * ...You get the idea.
	 */
	public static int[] IntArrayTrimAll(int[] arr, int i) {
		int k =0;
		//Count ints == i
		for(int j =arr.length-1 ; j >-1 ; j--){
			if(arr[j] == i){
				k++;
			}
		}
		int[] new_arr = new int[arr.length -k];
		int c = 0;
		for(int j =0 ; j < arr.length ; j++){
			if(arr[j] != i){
				new_arr[c] = arr[j];c++;
			}
		}
		return new_arr;
	}
	
	public static int[] ListInt2int(List<Integer> list){
		Integer[] ints = list.toArray(new Integer[0]);
		int[] ints2 = new int[ints.length];
		for(int i =0; i < ints.length; i++){
			ints2[i] = ints[i].intValue();
		}
		return ints2;
	}
	
	/* Hax warning!
	 * 
	 * Sort lifted from the openJDK
	 * 
	 * All logic remains with a, but all
	 * values set to a are mirrored with b
	 * I theory, this could be pushed to a 
	 * matrix column sort.
	 */
	public static void sortBothByFirst(int[]a, int[]b){
		int left =0;
		int right = a.length-1;
		sortBothByFirst(a,b,left,right);
	}
	
	public static void sortBothByFirst(int[] a, int[] b, int left, int right){
		for (int i = left, j = i; i < right; j = ++i) {
			int ai = a[i + 1];
			int bi = b[i + 1];
			while (ai < a[j]) {
				a[j + 1] = a[j];
				b[j + 1] = b[j];
				if (j-- == left) {
					break;
				}
			}
			a[j + 1] = ai;
			b[j + 1] = bi;
		}
	}

	//A poor algorhythm, to be improved i'm sure
	public static int[][] convert2DepthArray(int[][] blasts) {
		int[] ends = new int[blasts[1].length];
		for(int i =0; i < blasts[1].length; i++)ends[i]=blasts[0][i]+blasts[1][i];
		int[] starts = blasts[0];
		ArrayList<Integer> pos = new ArrayList<Integer>();
		ArrayList<Integer> dep = new ArrayList<Integer>();
		for(int i = 0 ; i < starts.length; i++){
			pos.add(starts[i]);
			dep.add(1);
			pos.add(ends[i]);
			dep.add(-1);
		}
		int[] poss = ListInt2int(pos);
		int[] deps  = ListInt2int(dep);
		Tools_Array.sortBothByFirst(poss, deps);
		int[][] returned = new int[2][poss.length];
		int depss = 0;
		for(int i =0 ; i < poss.length; i++){
			returned[0][i] = poss[i];
			depss += deps[i];
			returned[1][i] = depss;
		}
		return returned;
	}
}
