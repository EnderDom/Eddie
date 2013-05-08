package enderdom.eddie.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class Tools_Web {
	
	public static String urlReader(String url) throws MalformedURLException{
		URL site = new URL(url);
		return urlReader(site);
	}
	
	public static String urlReader(URL site){
		StringBuilder text = new StringBuilder("");
		try{
			
			BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				text = text.append(inputLine +" "+ Tools_System.getNewline());
			}
			in.close();
		}
		catch(IOException iox){
			Logger.getRootLogger().error("IO Issue: " + site.toString(), iox);
		}
		return text.toString();
	}
	
	public static String[] stripImages(String website){
		Pattern p = Pattern.compile("<img[^>]*src=\"([^\"]*)",Pattern.CASE_INSENSITIVE);
		return Tools_String.pattern2List(p, website);
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
	
	public static BufferedImage imageFromUrl(String url){
		BufferedImage img = null;
		try{
			URLEncoder.encode(url, "UTF-8");
		}
		catch(UnsupportedEncodingException encod){
			Logger.getRootLogger().error("Encoding Exception in method urlReader.", encod);
		}
		try{
			URL site = new URL(url);
			img = ImageIO.read(site);
		}
		catch(IOException iox){
			Logger.getRootLogger().error("IO Issue: " + url, iox);
		}
		return img;
	}
	
	/**
	 * Very basic FTP file download, only works if file is 
	 * at urlstring and the server supports this download method
	 * 
	 * @param urlstring
	 * @param file
	 * @return
	 */
	public static boolean basicFTP2File(String urlstring, String file){
		try {
			URL url = new URL(urlstring);
			Logger.getRootLogger().debug("About to open connection: " + urlstring+"...");
			URLConnection conn = url.openConnection();  
			Logger.getRootLogger().info("Downloading files from FTP, this may take some time...");
			InputStream is = conn.getInputStream();
			Logger.getRootLogger().debug("Saving download to "+ file);
			return Tools_File.stream2File(is, file);
		}
		catch (MalformedURLException e) {
			Logger.getRootLogger().error("Failed to access FTP ("+urlstring+")", e);
		} 
		catch (IOException e) {
			Logger.getRootLogger().error("Failed to download file ("+file+") from FTP ("+urlstring+")", e);
		}  
		return false;
	}
}
