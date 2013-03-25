package enderdom.eddie.tasks;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.cli.LazyPosixParser;

import enderdom.eddie.tools.Tools_CLI;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Bio_File;
import enderdom.eddie.ui.TaskManager;

public abstract class Task extends BasicTask {

	private TaskManager manager;
	protected TaskState state;
	public Options options;
	public boolean helpmode;
	protected boolean testmode;
	protected String args;
	protected String helpheader = "--This is the Help Message of the Default Task--";
	protected String password =null;
	Logger logger = Logger.getRootLogger();
	protected int futurehash;

	
	public void run() {
		setCompleteState(TaskState.STARTED);
		Logger.getRootLogger().debug("Started running task @ "+Tools_System.getDateNow());
		if(testmode){
			runTest();
		}
		else{
			System.out.println();
			System.out.println("--This is the default Task run message--");
			System.out.println();
			System.out.println("If you're seeing this message it means the Task Manager is working,");
		    System.out.println("But that you are running the default Task run method for some reason.");
		    System.out.println("");
		    System.out.println("The most likely reason is that both testmode is not enabled and the " +
		    		"class that has extended this Task class has not overwrote the default run()");
		}
		Logger.getRootLogger().debug("Finished running task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public void parseArgs(String[] args){
		buildOptions();
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);

			if(cmd.hasOption("p")){
				this.password = cmd.getOptionValue("p");
			}
			
		/*	 Don't mess these 2 up, like i've done before :)	*/
		/**/if(cmd.hasOption("opts")){							//
		/**/	printHelpMessage();								//
		/**/	helpmode = true;								//
		/**/}													//
		/**/else{												//
		/**/	parseArgsSub(cmd);								//
		/**/}													//
		/*	 Don't mess these 2 up, like i've done before :)	*/
			
		}
		catch(ParseException e){
			Logger.getRootLogger().trace("ParseExecption was throw, " +
					"but not printed, as its not important");
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
		Tools_CLI.printHelpMessage(getHelpHeader(), "-- Share And Enjoy! --", this.options);
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
	
	
	public void buildOptions(){
		options = new Options();
		options.addOption(new Option("opts", false, "Help Menu for this specific task"));
		options.addOption(new Option("p", "password", true, "Add a password to a task if it is needed"));
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

	protected void runTest(){
		System.out.println("");
		System.out.println("--TEST MODE--");
		System.out.println("");
	}
	
	public boolean isKeepArgs(){
		return false;
	}

	/*
	 * Very basic File Type detection
	 * 
	 */
	public BioFileType detectFileType(String filename){
		return Tools_Bio_File.detectFileType(filename);
	}

}
