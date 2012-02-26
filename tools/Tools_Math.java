package tools;

public class Tools_Math {

	public static double round(double d, int c) {
		d = d*Math.pow(10,c);
		d = Math.round(d);
		return d/Math.pow(10,c);
	}
	
}
