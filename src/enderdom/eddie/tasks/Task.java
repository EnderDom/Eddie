package enderdom.eddie.tasks;

import java.io.File;
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
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_String;
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
	
	/**
	 * Tries to cut down the code a bit in the initial parsing of
	 * args[]. But only so much can be done
	 * 
	 * @param cmd
	 * @param opt
	 * @param defaul
	 * @return  either the value input by the commandline
	 * or the default line if either the value is not set, or
	 * it is not parsable. If unparseable logs a warning, but does
	 * not throw exception
	 */
	public static String getOption(CommandLine cmd, String opt, String defaul){
		if(cmd.hasOption(opt) && cmd.getOptionValue(opt) != null){
			String r = cmd.getOptionValue(opt);
			if(r != null && r.length() != 0)return r;
			else Logger.getRootLogger().warn("Failed to parse -" + opt + cmd.getOptionValue(opt) + " as string");
		}
		return defaul;
	}
	
	/**
	 * 
	 * 
	 * @param cmd
	 * @param opt
	 * @param defaul
	 * @return either the value input by the commandline
	 * or the default line if either the value is not set, or
	 * it is not parsable. If unparseable logs a warning, but does
	 * not throw exception
	 */
	public static int getOption(CommandLine cmd, String opt, int defaul){
		if(cmd.hasOption(opt) &&  cmd.getOptionValue(opt) != null){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue(opt));
			if(i != null)return i.intValue();
			else Logger.getRootLogger().warn("Failed to parse -" + opt + cmd.getOptionValue(opt) + " as integer");
		}
		return defaul;
	}
	
	/**
	 * 
	 * @param cmd
	 * @param opt
	 * @param defaul
	 * @return either the value input by the commandline
	 * or the default line if either the value is not set, or
	 * it is not parsable. If unparseable logs a warning, but does
	 * not throw exception
	 */
	public static double getOption(CommandLine cmd, String opt, double defaul){
		if(cmd.hasOption(opt) && cmd.getOptionValue(opt) != null){
			Double i = Tools_String.parseString2Double(cmd.getOptionValue(opt));
			if(i != null)return i.doubleValue();
			else Logger.getRootLogger().warn("Failed to parse -" + opt + cmd.getOptionValue(opt) + " as double");
		}
		return defaul;
	}
	
	public static String[] getOptions(CommandLine cmd, String opt, String[] defaul){
		if(cmd.hasOption(opt) && cmd.getOptionValues(opt) != null){
			String[] r = cmd.getOptionValues(opt);
			if(r != null && r.length> 0)return r;
			else Logger.getRootLogger().warn("Failed to parse -" + opt + cmd.getOptionValue(opt) + " as String");
		}
		return defaul;
	}
	
	/**
	 * Quick and dirty pull strings from 
	 * file
	 * 
	 * @param cmd
	 * @param opt
	 * @param f
	 * @return
	 */
	public static String getOptionFromFile(CommandLine cmd, String opt){
		String f = null;
		String filename = cmd.getOptionValue(opt);
		if(filename != null){
			File fie = new File(filename);
			if(fie.isFile()){
				f = Tools_File.quickRead(fie, false);
			}
		}	
		return f;
	}

}
