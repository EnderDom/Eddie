package tools;


public abstract class Tools_String {
	
	public static int fastadefaultlength = 60;
	
	public static Integer parseString2Int(String sun){
		try{
			return Integer.parseInt(sun.trim());
		}
		catch (NumberFormatException nfe){
			return null; //Not a great solution
		}
	}
	
	public static Double parseString2Double(String sun){
		try{
			return Double.parseDouble(sun.trim());
		}
		catch (NumberFormatException nfe){
			return null;//Not a great solution
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
	
	public static boolean isIntegerParseInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		}
		catch (NumberFormatException nfe) {}
			return false;
	}

	public static boolean isIntegerParseDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		}
		catch (NumberFormatException nfe) {}
			return false;
	}
	
	public static boolean isNumb(String s){
		try{
			Double.parseDouble(s);
			return true;
		}
		catch(NumberFormatException nfe){
			return false;
		}
	}
	
	public static String cutLineBetween(String start, String end, String src) throws Exception{
		int i =0;
		int j =0;
		
		
		if((i=src.indexOf(start)) == -1 || (j=src.indexOf(end))== -1){
			throw new Exception("IndexOutOfBounds Cannot cut string of len="+src.length() + " between "+i+ " : " +j);
		}
		i = i+start.length();
		if(i > j){
			throw new Exception("IndexOutOfBounds Cannot cut string of len="+src.length() + " between "+i+ " : " +j);
		}
		return src.substring(i,j);
	}
	
}
