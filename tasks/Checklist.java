package tasks;

import java.io.File;
import java.io.IOException;

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
	protected Logger logger = Logger.getLogger("CheckListLogger");
	private boolean notasks;
	
	public Checklist(String workspace, String taskname){
		this.taskname = taskname;
		this.workspace = workspace;
		folder = new File(workspace + Tools_System.getFilepathSeparator() + "checklists");
		if(folder.exists()){
			if(!folder.isDirectory()){
				Logger.getRootLogger().error("Checklist folder exists but is not a folder");
			}
		}
		else{
			folder.mkdir();
		}
		int l =0;
		int k =0;
		for(File file : folder.listFiles()){
			if(file.getName().contains(taskname+"__")){
				l = Tools_String.parseString2Int(taskname.substring(taskname.indexOf("__"),taskname.length()));
				if(l > k){
					k = l;
				}
			}
		}
		if(k != 0){
			logger.warn("Task checklists not closed!");
			notasks = false;
		}
		File task = new File(workspace + Tools_System.getFilepathSeparator() + "checklists" +taskname+"__"+(k++));
		/*Double Check*/
		if(task.exists()){
			logger.error("Checklist already created with this name");
		}
	}
	
	public boolean check(){
		return notasks;
	}
	
	
	
	/**
	 * Sets the checklist to start,
	 * this will create a file and 
	 * data. Comment should be unique to 
	 * define if the task is the same being rerun.
	 */
	public boolean start(String comment, String input){
		boolean success = true;
		try {
			task.createNewFile();
		} catch (IOException e) {
			logger.error("Failed to create File checklist", e);
			success = false;
		}
		if(success){
			String write = "<START>"+Tools_System.getDateNow()+"</START>"+Tools_System.getNewline();
			String taskword = "<TASK>"+taskname+"</TASK>"+Tools_System.getNewline();
			input = "<INPUT>"+input+"</INPUT>"+Tools_System.getNewline();
			comment = "<COMMENT>"+comment+"</COMMENT>"+Tools_System.getNewline();
			String data = "<DATA>"+Tools_System.getNewline();
			success = Tools_File.quickWrite(write+input+taskword+comment+data, task, true);
		}
		return success;
	}
	/**
	 *
	 * @param Input to be saved
	 * @return whether saved or not
	 */
	public boolean update(String line){
		return Tools_File.quickWrite(line+Tools_System.getNewline(), task, true);
	}
	
	public boolean complete(){
		/*
		 * Probably not necessary, but in the event delete permissions
		 * and write permissions are not equivalent.
		 */
		String write = "</DATA>"+Tools_System.getNewline()+"<END>"+Tools_System.getDateNow()+"</END>"+Tools_System.getNewline();
		Tools_File.quickWrite(write, task, true);
		return task.delete();
	}
	
}
