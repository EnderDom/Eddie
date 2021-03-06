package enderdom.eddie.tools;

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
	
	/**
	 * 
	 * @param values[0] = array of start values on the x axis
	 * @param values[1] = array of lengths along the x axis corresponding to the start values
	 * @return Maximum extent the above data reaches on the x axis
	 */
	public static int getMaxXValue(int[][] values){
		int max = 0;
		for(int i =0; i < values[0].length; i++){
			if(values[0][i] + values[1][i] > max){
				max =values[0][i] + values[1][i]; 
			}
		}
		return max;
	}
	
	public static int getMaxValue(int data[]){
		int o = data[0];
		for(int i = 1; i < data.length; i++){
			if(data[i] > o)o=data[i];
		}
		return o;
	}
	public static int getMinValue(int data[]){
		int o = data[0];
		for(int i = 1; i < data.length; i++){
			if(data[i] < o)o=data[i];
		}
		return o;
	}
	
	
}
