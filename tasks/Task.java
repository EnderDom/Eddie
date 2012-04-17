package tasks;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import cli.LazyPosixParser;

import tools.Tools_System;
import tools.Tools_Task;
import ui.TaskManager;
import ui.UI;

public class Task implements Runnable, Future<Object> {

	private int id;
	private boolean core;
	private TaskManager manager;
	protected int complete;
	public static int unstarted = -1;
	public static int started = 0;
	public static int finished = 1;
	public static int cancelled = 2;
	public static int error = 3;
	public Options options;
	public boolean helpmode;
	protected boolean testmode;
	protected String args;
	protected String helpheader = "--This is the Help Message of the Default Task--";

	/*
	 * complete note:
	 * -1 == unstarted, but init
	 * 0 == started
	 * 1 == finished without error
	 * 2 == cancelled
	 * 3 == Error
	 * >3 == Task-Specific Error
	 */
	
	private boolean try2Close;
	
	public boolean isCore(){
		return core;
	}
	
	public void setCore(boolean core){
		this.core = core;
	}
	
	public void run() {
		setComplete(started);
		Logger.getRootLogger().debug("Started running task @ "+Tools_System.getDateNow());
		if(testmode){
			runTest();
		}
		else{
			System.out.println();
			System.out.println("--This is the default Task run message--");
			System.out.println();
			System.out.println("If you're seeing this message it means the Task Manager is working,");
		    System.out.println("But that you are running the default Task class for some reason.");
		    System.out.println();
		    System.out.println("Quick Test of Process Runner");
		    if(Tools_System.isWindows())System.out.println(Tools_Task.runProcess("dir", true)[0].toString());
		    else System.out.println(Tools_Task.runProcess("ls", true)[0].toString());
		    System.out.println();
		    System.out.println();
		    System.out.println("That should be where we're at?");
		    System.out.println();
		}
		Logger.getRootLogger().debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void parseArgs(String[] args){
		buildOptions();
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption("test")){
				
				testmode =true;
			}
			if(cmd.hasOption("opts")){
				printHelpMessage();
				helpmode = true;
			}
			else{
				parseArgsSub(cmd);
			}
		}
		catch(ParseException e){
		}
		if(isKeepArgs()){
			String st = new String();
			for(String arg : args)st+=arg+" ";
			this.args = st;
		}
	}
	
	public void parseArgsSub(CommandLine cmd){
		
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void printHelpMessage(){
		System.out.println("");
		System.out.println("");
		System.out.println(getHelpHeader());
		HelpFormatter help = new HelpFormatter();
		help.printHelp("ls", "-- Task Help Menu --", options, "-- Share And Enjoy! --");
		System.out.println("");
		System.out.println("");
	}
	
	public String getHelpHeader(){
		return this.helpheader;
	}
	
	public void setHelpHeader(String str){
		this.helpheader = str;
	}
	
	public synchronized void update(){
		this.manager.update(this);
	}
	
	public boolean isStart(){
		if(complete == -1){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean cancel(boolean arg0) {
		try2Close = arg0;
		return false;
	}

	public Object get() throws InterruptedException, ExecutionException {
		return (Object) this;
	}

	public Object get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		return (Object) this;
	}

	public boolean isCancelled() {
		if(complete== 2){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isDone() {
		if(complete > 0){
			return true;
		}
		else{
			return false;
		}
	}
	public int getComplete() {
		return complete;
	}

	public void setComplete(int complete) {
		this.complete = complete;
	}

	public boolean isTry2Close() {
		return try2Close;
	}

	public void setTry2Close(boolean try2Close) {
		this.try2Close = try2Close;
	}

	public void setID(int taskcounter) {
		this.id = taskcounter;
	}
	public int getID(){
		return this.id;
	}
	
	public void buildOptions(){
		options = new Options();
		options.addOption(new Option("opts", false, "Help Menu for this specific task"));
		options.addOption(new Option("test", false, "Runs any test for this task"));
	}
	
	public Options getOptions(){
		return this.options;
	}

	public boolean isHelpmode() {
		return helpmode;
	}

	public void setHelpmode(boolean helpmode) {
		this.helpmode = helpmode;
	}

	public void runTest(){
		System.out.println("");
		System.out.println("--TEST MODE--");
		System.out.println("");
	}
	
	public boolean isKeepArgs(){
		return false;
	}
	
	public boolean wantsUI(){
		return false;
	}
	
	public void addUI(UI ui){
		
	}
	
}
