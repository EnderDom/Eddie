package tools;

public class Tools_Math {

	public static double round(double d, int c) {
		d = d*Math.pow(10,c);
		d = Math.round(d);
		return d/Math.pow(10,c);
	}
	
	public static double average(int[] i){
		double l = 0;
		l=(double)sum(i);
		return l/(double)i.length;
	}
	
	public static int sum(int[] i){
		int l = 0;
		for(int j =0;j < i.length; j++)l=l+i[j];
		return l;
	}
	
}
