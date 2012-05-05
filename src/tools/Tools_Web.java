package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Tools_Web {
	
	public static String urlReader(String url){
		StringBuilder text = new StringBuilder("");
		try{
			URLEncoder.encode(url, "UTF-8");
		}
		catch(UnsupportedEncodingException encod){
			Logger.getRootLogger().error("Encoding Exception in method urlReader.", encod);
		}
		try{
			URL site = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				text = text.append(inputLine +" "+ Tools_System.getNewline());
			}
			in.close();
		}
		catch(IOException iox){
			Logger.getRootLogger().error("IO Issue: " + url, iox);
		}
		return text.toString();	
	}
	
	public static String[] stripImages(String website){
		Pattern p = Pattern.compile("<img[^>]*src=\"([^\"]*)",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(website);
		LinkedList<String> strs = new LinkedList<String>();
		int start = 0;
		while(m.find(start)){
			strs.add(m.group());
			start = m.end();
		}
		return strs.toArray(new String[0]);
	}
	
	public static String[] stripUrls(String website){
		Pattern p = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
		return Tools_String.pattern2List(p, website);
	}
	
	public static String[] stripHrefs(String website){
		Pattern p = Pattern.compile("[href|HREF]=[\"|'](.*?)[\"|']");
		String[] s = Tools_String.pattern2List(p, website);
		try{
			for(int i =0; i < s.length; i++)s[i]= s[i].indexOf('\'') != -1 ? s[i].substring(s[i].indexOf('\'')+1, s[i].lastIndexOf('\''))
					:s[i].substring(s[i].indexOf('"')+1, s[i].lastIndexOf('"'));
		}
		catch(Exception e){
			Logger.getRootLogger().warn("Dodgy Coding for the win");
		}
		return s;
	}
	
	public static String[] urlencode(String[] input){
		for(int i =0; i < input.length; i++){
			try {
				input[i] = URLEncoder.encode(input[i], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return input;
	}
}
