package enderdom.eddie.tools;

import org.apache.log4j.Logger;

import enderdom.eddie.tasks.StreamGobbler;

public abstract class Tools_Task {
	
	/**
	 * 
	 * @param coms String containing a command line command
	 * @param cache boolean denote whether or not to store data returned
	 * @return StringBuffer array containing Sys err and out if cache is true
	 * else is empty
	 */
	public static StringBuffer[] runProcess(String coms, boolean cache){
		return runProcess(new String[]{coms}, cache);
	}
	
	/**
	 * Runs external process, Uses StreamGobbler to receive all input Streams
	 * this seems to be the general feel that if the data doesn't go somewhere, bad 
	 * things happens.
	 * 
	 * Cache tells the Method to tell StreamGobbler whether or not to actually store the data.
	 * 
	 * Set to false returned value Array will be empty. In reality I'm not sure why you wouldn't
	 * cache everything so I may get rid of this in the future. 
	 * 
	 * @param coms String array containing command lines
	 * @param cache boolean denote whether or not to store data returned
	 * @return StringBuffer array containing Sys err and out if cache is true
	 * else is empty
	 */
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
			Logger.getRootLogger().debug("Using process command for Unix");
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
        	if (t.getMessage().contains("allocate memory")){
        		Logger.getRootLogger().error("Note: Messages like this:" +
        				"{ \"/bin/sh\": java.io.IOException: error=12, Cannot allocate memory}" +
        				"can be due to runtime.exec() allocating same amount of memory as jvm." +
        				"Try setting the -Xmx/s settings to below half of total system memory for this task run.");
        	}
        }
        return output;
	}
}
