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
	
//	public static boolean isOdd(int i){
//		return ((i & 1) == 0) ? true : false;
//	}
	
	/*
	 * Calculates the overlap between two 1D lines
	 * x1, x2 is start and finish of 1 line
	 * x3, x4 is start and finish of other line
	 * Result will be percentage
	 * Negative percentage indicates no overlap
	 * 
	 */
	public static double getOverlap(int x1, int x2, int x3, int x4){
		int x5 = Math.abs(Math.min(x1,x2)-Math.min(x3,x4));
		int x6 = Math.abs(Math.max(x1,x2)-Math.max(x3,x4));
		int xMax = Math.max(Math.max(x1,x2),Math.max(x3,x4));
		int xMin = Math.min(Math.min(x1,x2), Math.min(x3,x4));
		int xSize = xMax-xMin;
		int overlap = xSize-x5-x6;
		double y1 = (double)overlap;
		double y2 = (double)xSize;
		double y3 = y1/y2;
		return y3;
	}
	

}
