package tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import tools.Tools_File;
import tools.Tools_String;
import tools.Tools_System;

/**
 * A helper class
 * receives messages to save
 * to file with the idea that
 * Tasks can then recover unfinished
 * runs
 * 
 * * Note;
	 * All information about what the 
	 * saved checklist data represents should be held by the class calling
	 * the method. Ie the ownace of recovering the position
	 * of the task is left to the task not the checklist.
	 * 
	 * For instance a series of names as strings could be
	 * sent via update and saved. Then on recovery, the strings are returned and 
	 * the calling class will have to decide what to do with them.
	 * 
	 * An example:
	 * 
	 * -> Parse MultiFasta file
	 * -> Create list of subsequences
	 * -> Do something for each subsequence 
	 * -> Send the name of the subsequence to checklist when acting on it
	 * -> Checklist saves it
	 * -> Crashes before finishing going through all subsequences
	 * 
	 * After restart:
	 * -> Ask checklist for any unfinished Task (This could be done first, depends
	 * on what the Task is doing)
	 * -> Ask user if they want to complete? [Optional]
	 * -> reParse MultiFasta file
	 * -> Create list of subsequences
	 * -> Checklist returns the list of names sent to it by the Task.
	 * -> The Task must then go through the names and remove
	 * the names its list, so as to not repeat the process unneccesarily
	 * 
 * 
 * I haven't decided if this is a good way of doing a recovery of a task
 * but will reconsider later.
 */

public class Checklist {

	
	private String taskname;
	private String workspace;
	private File folder;
	private File task;
	protected Logger logger = Logger.getRootLogger();
	private boolean opentasks;
	private File lasttask;
	private String comment;
	private boolean recover;
	
	public Checklist(String workspacea, String tasknamea){
		this.taskname = tasknamea;
		this.workspace = workspacea;
		folder = new File(workspace + Tools_System.getFilepathSeparator() + "checklists");
		if(folder.exists()){
			if(!folder.isDirectory()){
				logger.error("Checklist folder exists but is not a folder");
			}
			else{
				logger.debug("Checklist folder exists");
			}
		}
		else{
			logger.debug("Making Folder for Checklists");
			folder.mkdir();
		}
		Integer l =0;
		int k =-1;
		if(folder.listFiles() != null){
			for(File file : folder.listFiles()){
				System.out.println(file.getPath());
				String name = file.getName();
				if(name.contains(taskname+"__")){
					logger.debug("Checklist found " + file.getName());
					if(lasttask == null)lasttask =file;
					l = Tools_String.parseString2Int(name.substring(name.indexOf("__")+2,name.length()));
					if(l != null){
						if(l > k){
							k = l;
							lasttask = file;
						}
					}
				}
			}
			if(k != -1){
				logger.warn("Task checklists not closed!");
				opentasks = true;
			}
		}
		k++;
		this.task = new File(workspace + Tools_System.getFilepathSeparator() + "checklists" + Tools_System.getFilepathSeparator() +taskname+"__"+(k));
		/*Double Check*/
		if(task.exists()){
			logger.error("Checklist already created with this name");
		}
		else{
			logger.trace("Checklist filepath set");
		}
	}
	
	public String getLast(){
		String[] lastinfo = null;
		logger.debug("Loading data from previous task");
		lastinfo = loadHeadFromFile(lasttask);
		if(lastinfo == null){
			logger.error("File now missing??");
		}
		StringBuilder bui = new StringBuilder();
		String n= Tools_System.getNewline();
		bui.append("Task: "+ lastinfo[1] + Tools_System.getNewline()+n);
		bui.append("Date/Time started: "+ lastinfo[0]+n);
		bui.append("Info: "+ lastinfo[2]+n);
		return bui.toString();
	}
	
	public boolean check(){
		return opentasks;
	}
	
	public void recoverLast(){
		this.task = lasttask;
		this.recover = true;
	}
	
	/**
	 * Sets the checklist to start,
	 * this will create a file and 
	 * data. Comment should be unique to 
	 * define if the task is the same being rerun.
	 */
	public boolean start(String args){
		logger.debug("Checklist started");
		boolean success = true;
		this.comment = args;
		if(!recover){
			try {
				logger.trace("Creating new file...");
				task.createNewFile();
			} catch (IOException e) {
				logger.error("Failed to create File checklist", e);
				success = false;
			}
			if(success){
				String write = "<START>"+Tools_System.getDateNow()+"</START>"+Tools_System.getNewline();
				String taskword = "<TASK>"+taskname+"</TASK>"+Tools_System.getNewline();
				String com = "<ARGS>"+comment+"</ARGS>"+Tools_System.getNewline();
				String data = "<DATA>"+Tools_System.getNewline();
				success = Tools_File.quickWrite(write+taskword+com+data, task, true);
			}
		}
		else{
			logger.debug("No start needed as recovery");
		}
		logger.debug("Checklist startup complete");
		return success;
	}
	/**
	 *
	 * @param Input to be saved
	 * @return whether saved or not
	 */
	public boolean update(String line){
		logger.trace("Checklist updated");
		return Tools_File.quickWrite(line+Tools_System.getNewline(), task, true);
	}
	
	public boolean complete(){
		/*
		 * Probably not necessary, but in the event delete permissions
		 * and write permissions are not equivalent.
		 */
		logger.debug("Checklist completed");
		String write = "</DATA>"+Tools_System.getNewline()+"<END>"+Tools_System.getDateNow()+"</END>"+Tools_System.getNewline();
		Tools_File.quickWrite(write, task, true);
		return task.delete();
	}
	
	//TODO FIX ASAP!!
	//TODO FIX ASAP!!
	//TODO FIX ASAP!!
	private String[] loadHeadFromFile(File file){
		String[] data = new String[3];
		logger.debug("Reading Data from checklist file");
		try{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			while((line = reader.readLine()) != null){
				if(line.contains("<DATA>"))break;				
				if(line.startsWith("<START>")){
					data[0] = Tools_String.cutLineBetween("<START>", "</START>", line);
				}
				if(line.startsWith("<TASK>")){
					data[1] = Tools_String.cutLineBetween("<TASK>", "</TASK>", line);
				}
				if(line.startsWith("<ARGS>")){
					data[2] = Tools_String.cutLineBetween("<ARGS>", "</ARGS>", line);
					this.comment = data[2];
					break;
				}
			}
			reader.close();
			in.close();
			fis.close();
		}
		catch(IOException io){
			logger.error("Failed loading text", io);
		}
		catch(Exception e){
			logger.error("Failed dividing tags",e);
		}
		return data;
	}
	
	public String[] getData(){
		LinkedList<String> strs = new LinkedList<String>();
		try{
			System.out.println(task.getPath());
			FileInputStream fis = new FileInputStream(task);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			boolean start = false;
			while((line = reader.readLine()) != null){
				if(start){
					strs.add(line);
				}
				if(line.contains("</DATA>"))break;				
				if(line.contains("<DATA>"))start=true;
			}
			reader.close();
			in.close();
			fis.close();
		}
		catch(IOException io){
			logger.error("Failed loading text", io);
		}
		catch(Exception e){
			logger.error("Failed dividing tags",e);
		}
		return strs.toArray(new String[0]);
	}
	
	public boolean closeLastTask(){
		return lasttask.delete();
	}
	
	public String getArgs(){
		if(comment == null){
			loadHeadFromFile(lasttask);
		}
		return comment;
	}
	
	public boolean inRecovery(){
		return this.recover;
	}
}
