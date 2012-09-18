package enderdom.eddie.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;


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

	/**
	 *  Doesn't work for floats and negative numbers
	 *  Will return real integers, ie contig0001 wil be returned as 1
	 * @param name string containing some numbers
	 * @return longest integer within the text string, assumming
	 * that non-numeric characters separate the integers,
	 *  or -1 if no number is found
	 */
	
	public static int getLongestInt(String name) {
		name = name.replaceAll( "[^\\d]", " ");
		String[] i = name.split(" ");
		int[] ii = new int[i.length];
		for(int j =0; j < i.length; j++){
			if(i[j].trim().length() > 0){
				Integer a = parseString2Int(i[j]);
				if(a == null){
					a=-1;
					Logger.getRootLogger().warn("This error message suggests an intrinsic code based bug {"+i[j] + "}");
				}
				ii[j]=a;
			}
		}
		if(ii.length > 0){
			Arrays.sort(ii);
			return ii[ii.length-1];
		}
		else{
			return -1;
		}
	}
	
	/**
	 * 
	 * @param list list object containing Strings, with len>1
	 * @return list sorted by string length and then by alphabetical order
	 * 
	 */
	public static List<String> sortStringsByLength(List<String> list){
		Collections.sort(list, new Tools_String_Comparator());
		return list;
	}
	
	public static String[] pattern2List(Pattern p, String s){
		Matcher m = p.matcher(s);
		LinkedList<String> strs = new LinkedList<String>();
		int start = 0;
		while(m.find(start)){
			strs.add(m.group());
			start = m.end();
		}
		return strs.toArray(new String[0]);
	}
	
	/**
	 * 
	 * @param String to search
	 * @param Character to look for 
	 * @return number of times character appears in string, case sensitive
	 */
	public static int count(String s, char c){
		int count = 0;
		for(int i=0; i < s.length(); i++){
			if(s.charAt(i) == c)count++;
		}
		return count;
	}
	
	public static String capitalize(String s){
		return WordUtils.capitalize(s);
	}
}
