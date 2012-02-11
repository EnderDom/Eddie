package tools;


public abstract class Tools_String {
	
	public static int fastadefaultlength = 60;
	
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
	
	//Splits a string into a lines of length splitsize
	public static String splitintolines(int splitsize, String seq){
		String newline = System.getProperty("line.separator");
		int i = 0;
		StringBuilder newstring = new StringBuilder();
		while(i < seq.length()-splitsize){
			newstring.append(seq.substring(i, i+splitsize) + newline);
			i= i+splitsize;
		}
		newstring.append(seq.substring(i, seq.length()) + newline);
		return newstring.toString();
	}
	
}
