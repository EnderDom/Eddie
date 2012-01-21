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

import ui.TaskManager;

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
		System.out.println();
		System.out.println("--This is the default Task run message--");
		System.out.println();
		System.out.println("If you're seeing this message it means the Task Manager is working,");
	    System.out.println("But that you are running the default Task class for some reason.");
	    System.out.println();
	    System.out.println("Quick Test of Process Runner");
	    System.out.println(runProcess("dir", true)[0].toString());
	    System.out.println();
	    System.out.println();
	    System.out.println("That should be where we're at?");
	    System.out.println();
	    setComplete(finished);
	}
	
	public void parseArgs(String[] args){
		buildOptions();
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption("opts")){
				printHelpMessage();
			}
			else{
				parseArgsSub(cmd);
			}
		}
		catch(ParseException e){
		}
	}
	
	public void parseArgsSub(CommandLine cmd){
		
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void printHelpMessage(){
		System.out.println("--This is the Help Message of the Default Task--");
		HelpFormatter help = new HelpFormatter();
		help.printHelp("ls", "-- Task Help Menu --", options, "-- Share And Enjoy! --");
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
		options.addOption(new Option("opts", false, "Options for this specific task"));
	}
	
	public Options getOptions(){
		return this.options;
	}

	/*
	 * Laziness next to godliness
	 */
	public static StringBuffer[] runProcess(String coms, boolean cache){
		return runProcess(new String[]{coms}, cache);
	}
	
	public static StringBuffer[] runProcess(String[] coms, boolean cache){
		StringBuffer[] output = null;
		String osName = System.getProperty("os.name" );
		String[] cmd =  new String[coms.length+2];
        if( osName.indexOf("Windows NT" ) != -1 ){
            cmd[0] = "command.com" ;
            cmd[1] = "/C" ;
            Logger.getRootLogger().debug("Man your computer's old! You dug this up?");
        }
        else if ( osName.indexOf( "Windows") != -1){
        	 cmd[0] = "cmd.exe" ;
             cmd[1] = "/C" ;
             Logger.getRootLogger().debug("Using process commands for Windows 95 and higher");
        }
        else{ 
        	cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			Logger.getRootLogger().debug("Using process command for Unix (AKA Boss Mode)");
        }
        for(int i =0; i < coms.length; i++){
			cmd[i+2] = coms[i];
		}
        try{
	        
	        Logger.getRootLogger().debug("Execing " + cmd[0] + " " + cmd[1] 
	                           + " " + cmd[2]);
	        Runtime rt = Runtime.getRuntime();
	        Process proc = rt.exec(cmd);
	        // any error message
	        StreamGobbler errorGobbler = new 
	            StreamGobbler(proc.getErrorStream(), "ERROR", cache);
	        
	        // any output?
	        StreamGobbler outputGobbler = new 
	            StreamGobbler(proc.getInputStream(), "OUTPUT", cache);
	        
	        // kick them off
	        errorGobbler.start();
	        outputGobbler.start();
	                                
	        // any error???
	        int exitVal = proc.waitFor(); //Note:: This will pause the thread
	        if(exitVal == 0){Logger.getRootLogger().debug("Process Exited Normally");}
	        else Logger.getRootLogger().error("Abnormal exit value");
	        
	        if(cache){
	        	output = new StringBuffer[]{outputGobbler.getOutput(), errorGobbler.getOutput()};
	        }
	        
	        errorGobbler.close();
	        outputGobbler.close();
        } 
        catch (Throwable t){
        	Logger.getRootLogger().error("Process Error", t);
        }
        return output;
	}
	
}
