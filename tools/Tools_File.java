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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class Tools_File {

	
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
	
	public static String quickRead(File file){
		return quickRead(file, true);
	}
	/*
	 * Quick and simple File Read method, with no real error checking though
	 */
	public static String quickRead(File file, boolean lines){
		StringBuffer buff = new StringBuffer();
		try{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			String newline = Tools_System.getFilepathSeparator();
			while((line = reader.readLine()) != null){
				buff.append(line);
				if(lines)buff.append(newline);
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
		} 
		catch (IOException e) {
			return -1;
		}
	}

	public static HashMap<String, String> mapFiles(HashMap<String, String> names, File folder){
		int count = names.size();
		File[] subfiles = folder.listFiles();
		int matchcount=0;
		int namecount=0;
		boolean[] matched = new boolean[subfiles.length];
		for(String name : names.keySet()){
			for(int i =0; i < subfiles.length;i++){
				if(!matched[i]){
					if(subfiles[i].getName().contains(names.get(name))){
						matchcount++;
						names.put(name, subfiles[i].getPath());
						matched[i] =true;
						break;
					}
				}
			}
			namecount++;
			System.out.print("\rPASS 1: "+namecount);
			if(namecount == 20 && matchcount != 20){
				System.out.println("");
				System.out.println("This method is not working skipping");
				break;
			}
		}
		if(matchcount == count){
			Logger.getRootLogger().debug("Method 1 successfull");
			return names;
		}
		else{
			if(matchcount==0){
				Logger.getRootLogger().debug("No matches using method 1");
			}
			else if(matchcount != count){
				Logger.getRootLogger().debug("Hmmmm... some matches retrying");
			}
			matchcount=0;
			namecount=0;
			matched = new boolean[subfiles.length];
			for(String name : names.keySet()){
				for(int i =0; i < subfiles.length;i++){
					if(!matched[i]){
						if(Tools_String.getLongestInt(subfiles[i].getName()) == Tools_String.getLongestInt(name)){
							matchcount++;
							names.put(name, subfiles[i].getPath());
							matched[i] =true;
							break;
						}
					}
				}
				namecount++;
				if(namecount == 20 && matchcount != 20){
					System.out.println("");
					System.out.println("This method is not working skipping");
					break;
				}
				System.out.print("\rPASS 2: "+namecount);
			}
			System.out.println("");
			if(matchcount == count){
				Logger.getRootLogger().debug("Method 1 successfull");
				return names;
			}
			else{
				matchcount=0;
				namecount=0;
				ArrayList<String> arr = new ArrayList<String>(names.size());
				for(String name : names.keySet()){
					arr.add(name);
				}
				arr = (ArrayList<String>)Tools_String.sortStringsByLength(arr);
				matched = new boolean[subfiles.length];
				for(int i =arr.size()-1; i > -1; i++){
					for(int j =0; j < subfiles.length;j++){
						if(!matched[j]){
							if(subfiles[j].getName().contains(arr.get(i))){
								names.put(arr.get(i), subfiles[j].getPath());
								matchcount++;
								matched[j]=true;
								break;
							}
						}
					}
					namecount++;
					if(namecount == 20 && matchcount != 20){
						System.out.println("");
						System.out.println("This method is not working skipping");
						break;
					}
					System.out.print("\rPASS 3: "+namecount);
				}
				System.out.println("");
				if(matchcount == count){
					Logger.getRootLogger().debug("Method 3 for mapping successful");
					return names;
				}
				else{
					Logger.getRootLogger().debug("Files could not be mapped based on file name");
					return null;
				}
			}
		}
		
	}
	
}
