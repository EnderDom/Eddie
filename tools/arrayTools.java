package tools;

public abstract class arrayTools {

	public static String[] mergeStrings(String[] a, String[] b){
		String[] c = new String[a.length+b.length];
		for(int i = 0; i < a.length; i++)c[i] = a[i];			
		for(int i = 0; i < b.length; i++)c[i+a.length] = b[i];
		return c;
	}
	
}
