package enderdom.eddie.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import enderdom.eddie.tools.Tools_System;

public abstract class BasicPropertyLoader implements PropertyLoader {

	protected boolean isLogging = false;
	public static Logger logger;
	protected Properties props;
	protected Level level = Level.WARN;
	public static String defaultlnf =  "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	
	public void setValue(String prop, String value){
		props.setProperty(prop, value);
	}

	public String getValueOrSet(String prop, String defaultvalue) {
		if (props.containsKey(prop)) {
			return props.getProperty(prop);
		} else {
			props.setProperty(prop, defaultvalue);
			return defaultvalue;
		}
	}
	
	public String getValue(String prop) {
		if (props.containsKey(prop)) {
			return props.getProperty(prop);
		} else {
			return null;
		}
	}

	public Properties getPropertyObject() {
		return this.props;
	}

	public void setPropertyObject(Properties props) {
		this.props = props;
	}

    public Properties loadPropertyFile(File file){
    	return this.loadPropertyFile(file, new Properties());
    }
    
    public Properties loadPropertyFile(File file, Properties prop){
    	 try{
            prop.load(new FileInputStream(file));
            if(isLogging())	logger.info("Trying to load Properties File @"+file.getPath());
            else	preLog("Trying to load Properties File @ "+file.getPath());
        	return prop;
         }
         catch(FileNotFoundException fi) {
        	 if(isLogging()){
        		 logger.error(fi);
        	 }
        	 else{
        		 preLog("Property File not found");
        		 fi.printStackTrace();
        	 }
        	 return null;
         }
         catch(IOException io) {
        	 if(isLogging()){
        		 logger.error(io);
        	 }
        	 else{
        		 preLog("IOException File not found");
        		 io.printStackTrace();
        	 }
        	 return null;
         }
    }
	public boolean savePropertyFile(File file, Properties props1){
		return this.savePropertyFile(file.getPath(), props1);
	}
	
	public boolean savePropertyFile(String filepath, Properties props1) {
		boolean success = false;
		try {
			FileOutputStream stream = new FileOutputStream(filepath);
			props1.store(stream, null);
			stream.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (success) {
			if(isLogging()){
				logger.info("Saved Properties @ "+filepath);
			}
			else{
				System.out.println("[PRE-LOG] Saved Properties @ "+filepath);
			}
		}
		if (!success) {
			if(isLogging()){
				logger.error("Saved Properties @ "+filepath);
			}
			else{
				preLog("[ERROR] failed to Saved Properties @ "+filepath);
			}
		}
		return success;
	}

	public void parseArgs(String[] args) {
		if(isLogging()){
			logger.warn("Warning, you should not be seeing this message, subclasses should always overwrite this");
		}
		else{
			preLog("Warning, you should not be seeing this message, subclasses should always overwrite this");
		}
	}
	
	public boolean isLogging(){
		return this.isLogging;
	}
	
	protected void setLogging(boolean logging){
		this.isLogging = logging;
	}
	
	protected static void preLog(String str){
		System.out.println("[PRE-LOG] "+str);
	}
	
	protected static Properties getDefaultLogProperties(String logfilepath){
		Properties defaults = new Properties();
		//Set Log File Properties
		defaults.setProperty("log4j.appender.rollingFile", "org.apache.log4j.RollingFileAppender");
		defaults.setProperty("log4j.appender.rollingFile.File", logfilepath+"eddie.log");
		defaults.setProperty("log4j.appender.rollingFile.MaxFileSize", "10MB");
		defaults.setProperty("log4j.appender.rollingFile.MaxBackupIndex", "3");
		defaults.setProperty("log4j.appender.rollingFile.layout", "org.apache.log4j.PatternLayout");
		defaults.setProperty("log4j.appender.rollingFile.layout.ConversionPattern", "[%5p] %t (%F:%L) - %m%n");
		//Mirror in console
		defaults.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
		defaults.setProperty("log4j.appender.stdout.layout",  "org.apache.log4j.PatternLayout");
		defaults.setProperty("log4j.appender.stdout.layout.ConversionPattern",  "[%5p] %t (%F:%L) - %m%n");
		
		defaults.setProperty("log4j.rootLogger", "WARN, rollingFile, stdout");
		return defaults;
	}    

    
	protected boolean startLog() {
		String slash = Tools_System.getFilepathSeparator();
		File logfolder = new File(getValue("WORKSPACE")+ slash + "logs");
		preLog("Initialising Log...");
		if (logfolder.isFile()) {
			System.out.println("Failed To log is standard location!!");
			int i = 0;
			while (logfolder.isFile()) {
				logfolder = new File(getValue("WORKSPACE")	+ slash + "logs" + i);
			}
		}
		if (!logfolder.exists()) {
			boolean done = logfolder.mkdir();
			if(!done)System.out.println("Could not make a log folder @ " + logfolder.getPath());
			else return false;
		}
		if(logfolder.isDirectory()){
            File log_properties = new File(logfolder.getPath()+slash+"log4j.properties");
            if(log_properties.isFile()){
                logger = Logger.getLogger(logfolder.getPath()+slash+"log4j.properties");
                PropertyConfigurator.configure(log_properties.getPath());
            }
            else{
                Properties defaults = getDefaultLogProperties(logfolder.getPath()+slash);
                savePropertyFile(log_properties.getPath(), defaults);
                logger = Logger.getLogger(logfolder.getPath()+slash+"log4j.properties");
                PropertyConfigurator.configure(defaults);
            }
            if(logger != null){
            	setLogging(true);
            	Logger.getRootLogger().setLevel(level);
            	Logger.getRootLogger().info("Logger Initialised LVL: "+level.toString()+" @ "+Tools_System.getDateNow());
            	return true;
            }
            else{
            	preLog("Failed to start logger, please submit as bug to software download page");
            	return false;
            }
        }
		else{
            preLog("Path at "+  logfolder.getPath() + " is supposed to be a directory but it's not");
            return false;
        }
	}
}

