package enderdom.eddie.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import enderdom.eddie.tools.comparators.StringLength_Comparator;


public abstract class Tools_String {
	
	public static int fastadefaultlength = 60;
	private static int counter = 0;
	
	public static Integer parseString2Int(String sun){
		try{
			return Integer.parseInt(sun.trim());
		}
		catch (NumberFormatException nfe){
			return null; //Not a great solution
		}
	}
	
	public static Double parseString2Double(String sun){
		if(sun == null || sun.length() < 1)return null;
		try{
			return Double.parseDouble(sun.trim());
		}
		catch (NumberFormatException nfe){
			return null;//Not a great solution
		}
	}
	

	public static Float parseString2Float(String sun) {
		if(sun == null || sun.length() < 1)return null;
		try{
			return Float.parseFloat(sun.trim());
		}
		catch (NumberFormatException nfe){
			return null;//Not a great solution
		}
	}
	
	/**
	 * Splits a string into a lines of length splitsize
	 * @param splitsize
	 * @param seq
	 * @return string split into lines, appended with a final newline
	 */
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
	
	/**
	 * Like splitintolines but saves straight to file
	 * 
	 * @param splitsize
	 * @param seq
	 * @param writer
	 */
	public static void splitintolinesandsave(int splitsize, String seq, BufferedWriter writer) throws IOException{
		String newline = System.getProperty("line.separator");
		int i = 0;
		while(i < seq.length()-splitsize){
			writer.write(seq.substring(i, i+splitsize));
			writer.write(newline);
			i= i+splitsize;
		}
		writer.write(seq.substring(i, seq.length()));
		writer.write(newline);
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
	
	public static boolean containsDigits(String str){
		return str.matches(".*\\d.*");
	}
	
	public static boolean containsOnlyAtoZ(String str){
		return containsDigits(str) ? true : str.matches(".*\\W.*");
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
	 * Like getLongestInt, but with workarounds
	 * 454 workaround
	 *  
	 * @param name
	 * @return
	 */
	public static int getContigNumber(String name){
		name = name.replaceAll( "[^\\d]", " ");
		name = name.replaceAll( " +", " ");
		name = name.trim();
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
			if(ii[ii.length-1] == 454){
				if(Tools_Math.sum(ii) > 454){
					return ii[ii.length-2];
				}
			}
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
		Collections.sort(list, new StringLength_Comparator());
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
	
	/**
	 * Pads the end of the string
	 * with spaces
	 * 
	 * @param m String to use
	 * @param len trim string to a specified length before padding
	 * @param trim set to true to trim
	 * @return the padded string
	 */
	public static String padString(String m, int len, boolean trim){
		return stringPadding(m, len, trim, ' ', true);
	}
	
	/**
	 * Pads a string to a final length as stipulated by
	 * the length variable
	 * 
	 * @param m The string 
	 * @param len The final length of the string
	 * @param trim Trim the string if it is already longer then length 
	 * @param c Character to use to pad
	 * @param right Whether to pad from the right, false adds to the start
	 * @return The padded string
	 */
	public static String stringPadding(String m, int len, boolean trim, char c, boolean right){
		if(m == null)m = "";
		if(m.length() > len && trim){
			return m.substring(0, len);
		}
		else{
			StringBuffer sb = new StringBuffer(len);
			if(right){
				sb.append(m);
				for (int i = m.length(); i < len; ++i){
					sb.append(c);
				}
			}
			else{
				for (int i = 0; i < len-m.length(); ++i){
					sb.append(c);
				}
				sb.append(m);
			}
			return sb.toString();
		}
	}

	public static String getStringofLenX(String name, int x){
		if(name.length() < x){
			StringBuffer b = new StringBuffer(name);
			while(b.length() < x)b.append(" ");
			return b.toString();
		}
		else return name=name.substring(0,x);
	}
	
	public static char getCounter(){
		counter++;
		switch(counter){ 
			case 0: return '\\';
			case 1: return '|';
			case 2: return '/';
			default:counter=-1; return '-';
		}
	}

	
}
