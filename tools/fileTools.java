package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class fileTools {

	
	/*
	 * Quick and simple File Write method, with no real error or overwrite checking though
	 */
	public static boolean quickWrite(String string, File file, boolean append){
		try{
			FileWriter fstream = new FileWriter(file, append);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(string);
			out.close();
			return true;
		}
		catch(IOException exe){
			return false;
		}
    }
	/*
	 * Quick and simple File Read method, with no real error checking though
	 */
	public static String quickRead(File file){
		StringBuffer buff = new StringBuffer();
		try{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			while((line = reader.readLine()) != null){
				buff.append(line + "\n");
			}
			reader.close();
			in.close();
			fis.close();
			return buff.toString();
		}
		catch(IOException io){
			return buff.toString();
		}
		
	}
	
	//From stack overflow, will fail if file larger than Long.MAX_VALUE, I suspect JVM may well too though ;)
	public static int countLines(File file){
		try {
			LineNumberReader  lnr = new LineNumberReader(new FileReader(file));
			lnr.skip(Long.MAX_VALUE);
			return lnr.getLineNumber();
		} catch (IOException e) {
			return -1;
		}
	}

}
