package enderdom.eddie.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class Tools_File {
	
	public static Logger logger = Logger.getRootLogger();
	
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
			logger.warn(exe);
			return false;
		}
    }
	
	public static boolean quickWrite(String[] str, File file){
		try{
			FileWriter fstream = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(fstream);
			String newline = Tools_System.getNewline();
			for(String s : str){
				out.write(s);
				out.flush();
				out.write(newline);
				out.flush();
			}
			out.close();
			return true;
		}
		catch(IOException exe){
			logger.warn(exe);
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
			String newline = Tools_System.getNewline();
			int i =0;
			while((line = reader.readLine()) != null){
				if(lines && i!=0)buff.append(newline);
				buff.append(line);
				i++;
			}
			reader.close();
			in.close();
			fis.close();
			return buff.toString();
		}
		catch(IOException io){
			logger.warn(io);
			return buff.toString();
		}
	}
	
	public static String[] quickRead2Array(File file){
		return quickRead2Array(64, file, false);
	}
	
	public static String[] quickRead2Array(int l, File file, boolean trim){
		ArrayList<String> s = new ArrayList<String>(l);
		try{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			while((line = reader.readLine()) != null){
				if(trim)line=line.trim();
				s.add(line);
			}
			reader.close();
			in.close();
			fis.close();
			return s.toArray(new String[0]);
		}
		catch(IOException io){
			logger.warn(io);
			return s.toArray(new String[0]);
		}
	}
	
	public static BufferedWriter getWriter(File file) throws IOException{
		return new BufferedWriter(new FileWriter(file,false));
	}
	
	/**
	 * Returns 1 line from a file. File is opened
	 * line read and then closed and line is returned
	 * obviously all the ioexceptions apply, but will
	 * be caught within the method and logged
	 * 
	 * @param filepath path to a file
	 * @param i the line which you want to return, 0-based
	 * @return the line at integer i returned as a string
	 */
	public static String returnLine(String filepath, int i){
		try{
			File f = new File(filepath);
			if(f.isFile()){
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader in = new InputStreamReader(fis, "UTF-8");
				BufferedReader reader = new BufferedReader(in);
				String line = null;
				boolean complete =false;
				while((line = reader.readLine()) != null){
					if(i==0){
						complete=true;
						break;
					}
					i--;
				}
				reader.close();
				in.close();
				fis.close();
				if(complete)return line;
				else return null;
			}
			else{
				logger.warn("File is not a text file!");
				return null;
			}
		}
		catch(IOException io){
			logger.error("Failed to extract line information");
			return null;
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
			logger.warn(e);
			return -1;
		}
	}

	public static HashMap<String, String> mapFiles(HashMap<String, String> names, File folder){
		int count = names.size();
		File[] subfiles = folder.listFiles();
		if(subfiles.length != names.size()){
			Logger.getRootLogger().warn("There are "+subfiles.length + " to map to " + names.size() + ", it is advised, to another files, lest they affect mapping");
		}
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
				Logger.getRootLogger().debug("This method is not working skipping");
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
					Logger.getRootLogger().debug("This method is not working skipping");
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
						Logger.getRootLogger().debug("This method is not working skipping");
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

	public static boolean createFolderIfNotExists(File folder) {
		if(folder.isDirectory()) return true;
		else if(!folder.exists())return folder.mkdir();
		else return false;
	}
	
	public static String getEnvirons(Object a){
		return new File(a.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent()+Tools_System.getFilepathSeparator();
	}
	
	/**
	 * 
	 * @param stream Inputstream from file or web
	 * @param output Filepath to save to, note: no check is done to see if this is valid
	 * @return returns true if continued to completion, false if error
	 */
	public static boolean stream2File(InputStream stream, String output){
		try{
			InputStreamReader in = new InputStreamReader(stream, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			FileWriter fstream = new FileWriter(output);
			BufferedWriter out = new BufferedWriter(fstream);
			String line = "";
			while((line=reader.readLine()) != null){
				out.write(line+Tools_System.getNewline());
				out.flush();
			}
			out.close();
			return true;
		}
		catch(IOException io){
			logger.warn(io);
			return false;
		}
	}
	
	/**
	 * As I'm shit scared of deleting files
	 * this basically justs moves the file to some other place
	 * then deletes it
	 * 
	 * @param f File to be moved
	 * @return true if file was successfully moved
	 */
	public static boolean justMoveFileSomewhere(File f){
		File mov;
		for(int i =0;(mov=new File(f.getPath()+i)).exists();i++);
		return f.renameTo(mov);
	}
	
	
	public static File getOutFileName(File directory, File filename, String fileending){
		//Avoids overwriting previous stuff
		String outname = filename.getName();
		int e =-1;
		if((e=outname.lastIndexOf(".")) != -1)outname = outname.substring(0, e);
		e=0;
		File out;
		while((out=new File(directory.getPath()+Tools_System.getFilepathSeparator()+outname+e+fileending)).exists())e++;
		return out;
	}
}
