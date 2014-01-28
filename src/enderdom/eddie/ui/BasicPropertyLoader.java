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
    protected File propfile;
	//Default logger level, if not set by user, this level will be used for logging
	protected Level level = Level.DEBUG;
	public static String defaultlnf =  "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static int numbercache = 0;
	protected String logname;
	
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

    public Properties loadPropertyFile(String file, boolean isLogging){
    	return loadPropertyFile(new File(file), new Properties(), isLogging);
    }
    
    public Properties loadPropertyFile(File file, Properties prop, boolean isLogging){
    	 try{
            prop.load(new FileInputStream(file));
            if(isLogging)	logger.info("Trying to load Properties File @"+file.getPath());
            else	preLog("Trying to load Properties File @ "+file.getPath());
        	return prop;
         }
         catch(FileNotFoundException fi) {
        	 if(isLogging){
        		 logger.error(fi);
        	 }
        	 else{
        		 preLog("Property File not found");
        		 fi.printStackTrace();
        	 }
        	 return null;
         }
         catch(IOException io) {
        	 if(isLogging){
        		 logger.error(io);
        	 }
        	 else{
        		 preLog("IOException File not found");
        		 io.printStackTrace();
        	 }
        	 return null;
         }
    }
    
	
	public boolean checkPropertiesAndLoadFile(String path){
		return checkPropertiesAndLoadFile(new File(path));
	}
	
	public boolean checkPropertiesAndLoadFile(File propsfile){
		preLog("Attempting to load file from "+ propsfile.getPath()+"...");
		if(propsfile.isFile() && propsfile.canWrite()){
			preLog("File exists, loading...");
			this.props = loadPropertyFile(propsfile, this.props, this.isLogging());
			propfile = propsfile;
			return true;
		}
		else if(propsfile.isFile() && !propsfile.canWrite()){
			preLog("File exists, but it cannot be written to.");
			return false;
		}
		else if(!propsfile.exists()){
			try{
				preLog("File doesn't exist, creating...");
				propsfile.getParentFile().mkdirs();
				if(propsfile.createNewFile()){
					propfile = propsfile;
					return true;
				}
				else return false;
			}
			catch(IOException io){
				preLog("Error thrown, can't create file at "+propsfile.getPath());
				io.printStackTrace();
				return false;
			}
		}
		else return false;
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
		else {
			if(isLogging()){
				logger.error("Saved Properties @ "+filepath);
			}
			else{
				preLog("[ERROR] failed to Saved Properties @ "+filepath);
			}
		}
		return success;
	}
	
	public void update(){
		savePropertyFile(propfile, props);
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
	
	protected Properties getDefaultLogProperties(String logfilepath){
		Properties defaults = new Properties();
		//Set Log File Properties
		defaults.setProperty("log4j.appender.rollingFile", "org.apache.log4j.RollingFileAppender");
		defaults.setProperty("log4j.appender.rollingFile.File", logfilepath);
		defaults.setProperty("log4j.appender.rollingFile.MaxFileSize", "10MB");
		defaults.setProperty("log4j.appender.rollingFile.MaxBackupIndex", "3");
		defaults.setProperty("log4j.appender.rollingFile.layout", "org.apache.log4j.PatternLayout");
		defaults.setProperty("log4j.appender.rollingFile.layout.ConversionPattern", "[%5p] %t (%F:%L) - %m%n");
		//Mirror in console
		defaults.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
		defaults.setProperty("log4j.appender.stdout.layout.ConversionPattern",  "[%5p] %t (%F:%L) - %m%n");
		if(this.getValueOrSet("COLORSTDOUT", "TRUE").equals("TRUE")){
			defaults.setProperty("log4j.appender.stdout.layout","enderdom.eddie.ui.ANSIColorLayout");
		}
		else defaults.setProperty("log4j.appender.stdout.layout",  "org.apache.log4j.PatternLayout"); 
		defaults.setProperty("log4j.rootLogger", "WARN, rollingFile, stdout");
		return defaults;
	}    

    
	protected boolean startLog() {
		String slash = Tools_System.getFilepathSeparator();
		File logfolder = new File(getValue(EddieProperty.WORKSPACE.toString())+ slash + "logs");
		preLog("Initialising Log at  "+logfolder.getPath()+"...");
		if (logfolder.isFile()) {
			System.out.println("Failed To log is standard location!!");
			int i = 0;
			while (logfolder.isFile()) {
				logfolder = new File(getValue(EddieProperty.WORKSPACE.toString())	+ slash + "logs" + i);
			}
		}
		if (!logfolder.exists()) {
			boolean done = logfolder.mkdir();
			if(!done)System.out.println("Could not make a log folder @ " + logfolder.getPath());
		}
		if(logfolder.isDirectory()){
            File log_properties = new File(logfolder.getPath()+slash+"log4j.properties");
            //If properties previously written
            if(log_properties.isFile()){
                configureProps(log_properties.getPath(), log_properties.getPath());
            }
            //If properties never written, use defaults
            else{
                Properties defaults = getDefaultLogProperties(logfolder.getPath()+slash+getLogname());
                savePropertyFile(log_properties.getPath(), defaults);
                configureProps(log_properties.getPath(), defaults);
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
	
	public static void configureProps(String filepath, Properties props){
		logger = Logger.getLogger(filepath);
        PropertyConfigurator.configure(props);
	}
	
	public static void configureProps(String filepath, String props){
		logger = Logger.getLogger(filepath);
        PropertyConfigurator.configure(props);
	}
	
	public String getPropertyFilePath(){
		return this.propfile.getPath();
	}

	public String getLogname() {
		return logname;
	}

	public void setLogname(String logname) {
		this.logname = logname;
	}
}

