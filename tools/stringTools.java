package tools;

public abstract class stringTools {
	
	public static int parseString2Int(String sun){
		try{
			return Integer.parseInt(sun.trim());
		}
		catch (NumberFormatException nfe){
			return -1; //Not a great solution
		}
	}
	
	public static double parseString2Double(String sun){
		try{
			return Double.parseDouble(sun.trim());
		}
		catch (NumberFormatException nfe){
			return -1.0;//Not a great solution
		}
	}
}
