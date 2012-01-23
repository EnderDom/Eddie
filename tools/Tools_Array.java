package tools;

public abstract class Tools_Array {

	public static String[] mergeStrings(String[] a, String[] b){
		String[] c = new String[a.length+b.length];
		for(int i = 0; i < a.length; i++)c[i] = a[i];			
		for(int i = 0; i < b.length; i++)c[i+a.length] = b[i];
		return c;
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
}
