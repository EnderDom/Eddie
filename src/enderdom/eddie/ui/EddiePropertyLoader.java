package enderdom.eddie.ui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import enderdom.eddie.cli.LazyPosixParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;

public class EddiePropertyLoader extends BasicPropertyLoader{

	//TODO hand over database properties to another class
    public static String propertyfilename = new String("eddie.properties");
    public static String infoFile = new String("eddie.info");
    public String rootfolder;
    private Options options;
    /* This is actually the 4th iteration of Eddie, 
     * though this one has been written from scratch
     */
    public static int engineversion = 4;
    public static double subversion = 0.37;
    public static String edition = "Development";
    public String[] actions;
	
	public String[] args;
	public String modulename;
	public int mode;
	
	private String[] defaultkeys;
	private String[] defaultvalues;
	private String[] defaulttooltips;
	
	//These should be the index to defaultKeysUN	
	
	public EddiePropertyLoader(String[] args) {
		this.props = new Properties();
		/*
		 * Load properties and then set default properties if property doesn't exist,
		 * this way any new or missing properties are auto added
		 */
		if(loadProperties()){
			setDefaultProperties();
			savePropertyFile(propfile, props);
			parseArgs(args);
			if(!startLog()){
				mode = -1;
			}
		}
	}
	
	public void parseArgs(String[] args){
		buildOptions();
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h")){
				mode = 1;
			}
			else if(cmd.hasOption("about")){
				mode = 6;
			}
			else{
				if(cmd.hasOption("g")){ 
					/* GUI */
					mode = 4;					
				}
				else{
					/*If Command Line iNterface*/
					mode = 2;
					/*
					 * Store arguments for further parsing by CLI
					 */
					this.args=args;
				}
			}
			if(cmd.hasOption("l")){
				String lev = cmd.getOptionValue("l");
				level = Level.toLevel(lev, level);
			}
		} catch (ParseException e) {
			System.out.println("Failed To Parse Input Options");
			e.printStackTrace();
			mode = -1;
		}
	}
	
	private void buildOptions(){
		options = new Options();
		options.addOption(new Option("p", "props", true, "Use this as default properties file"));
		options.addOption(new Option("g", "gui", false, "Set to GUI rather than CLI (indev)"));
		//options.addOption(new Option("e", "lite", false, "Eddie Lite, a lighter mode which loads less clunge")); WIP
		//options.addOption(new Option("persist", false, "If CLI set, this will stop CLI from closing without further args"));
		options.addOption(new Option("h", "help", false, "Help Menu"));
		options.addOption(new Option("l", "log", true, "Set Log Level {TRACE,DEBUG,INFO,WARN,ERROR,FATAL}"));
		options.addOption(new Option("about", false, "About the author"));
	}
	
	public void printHelp(){
		if(options == null){
			buildOptions();
		}
		HelpFormatter help = new HelpFormatter();
		help.printHelp("ls", "-- Eddie v"+subversion+" Help Menu --", options, "-- Share And Enjoy! --");
		System.out.println();
		System.out.println("Use -task for list of command line tasks");
		System.out.println("Use -task taskname -opts for that task's helpmenu");
	}

	public int getPropertiesCount(){
		return props.size();
	}
	
	public boolean loadProperties(){
		//Look first in the surrounding file
		String slash = Tools_System.getFilepathSeparator();
		//System.out.println(Tools_File.getEnvirons(this)+propertyfilename);
		//System.out.println(System.getProperty("user.home")+slash+".tina"+slash+"tina.properties");
		if(loadPropertiesFromFile(Tools_File.getEnvirons(this)+propertyfilename))return true;
		//Then in the home directory
		if(loadPropertiesFromFile(System.getProperty("user.home")+slash+".eddie"+slash+propertyfilename))return true;
		
		preLog("ERROR: don't have permission to save settings in either local folder of user home directory");
		return false;
	}
	
	public boolean loadPropertiesFromFile(String path){
		return loadPropertiesFromFile(new File(path));
	}
	
	public boolean loadPropertiesFromFile(File propsfile){
		preLog("Attempting to load file from "+ propsfile.getPath()+"...");
		if(propsfile.isFile() && propsfile.canWrite()){
			preLog("File exists, loading...");
			this.loadPropertyFile(propsfile, this.props);
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
	
	public void setDefaultProperties(){
		String slash = Tools_System.getFilepathSeparator();
		//Properties to add if not already available
		defaultkeys = new String[]{
				"WORKSPACE","AUXILTHREAD","CORETHREAD", 
				"BLAST_BIN_DIR","BLAST_DB_DIR", "ESTSCAN_BIN", 
				"FILES_XML","PREFLNF","TESTDATADIR",
				"DBTYPE","DBDRIVER","DBHOST", 
				"DBNAME", "DBUSER","UNI_VEC_DB", "UNIVEC_URL"
				 };
		defaultvalues = new String[]{
				propfile.getParent(), "5", "1", 
				"/usr/bin/", "/home/dominic/bioapps/blast/db/", "/usr/bin/",
				"null", defaultlnf, propfile.getParent()+slash+"test", 
				"mysql","com.mysql.jdbc.Driver", "Localhost", 
				DatabaseManager.default_database, "user", "","ftp://ftp.ncbi.nih.gov/pub/UniVec/UniVec"
				};
		defaulttooltips = new String[]{
				"Default Workspace directory", "Number of threads for core tasks (High CPU)", 
				"Number of auxiliary task threads", "Directory path containing blast binaries (Should be a folder)", 
				"Directory path containing blast databases", "Path for ESTscan binary", "Path for file to store file locations",
				"Preferred Look & Feel", "Directory Path for test data", "Database type, ie mysql", "Database driver",
				"Database host url", "Database name for Eddie", "Database user name", "Location of uni vec database",
				"Default URL to download the fasta for univec data"
		};
		
		if(defaultkeys.length != defaultvalues.length)System.out.println("You're being derp Dominic :(");
		
		for(int i =0; i < defaultkeys.length; i++){
			if(!this.props.containsKey(defaultkeys[i])){
				this.props.put(defaultkeys[i], defaultvalues[i]);
			}
		}
		
		//Forced Overwrite properties
		String[] tempkeys = new String[]{"SUBVERSION", "VERSION", 
				"FULLVERSION", "EDITION"};
		String[] tempvalues = new String[]{EddiePropertyLoader.subversion+"",EddiePropertyLoader.engineversion+"",
				(EddiePropertyLoader.subversion+EddiePropertyLoader.engineversion)+"", edition};
		if(tempkeys.length != tempvalues.length)System.out.println("You're being derp Dominic :(");
		for(int i =0; i < tempkeys.length; i++){
				this.props.put(tempkeys[i], tempvalues[i]);
		}
		

	}

	public String[][] getChangableStats(){
		String[][] stats = new String[3][defaultkeys.length];
		stats[0]=defaultkeys;
		stats[1]=defaultvalues;
		stats[2]=defaulttooltips;
		return stats;
	}
	
	public static String[][] getFullDBsettings(PropertyLoader load){
		String[][] db = new String[3][5];
		String[] d = new String[]{"DBTYPE", "DBDRIVER", "DBHOST","DBNAME","DBUSER"};
		String[] s = new String[]{"Type of database, currently only mysql supported"
		,"Database driver, this is unlikely to change"
		,"Name or ip of computer that hosts database, if local, localhost or 129.0.0.1"
		,"Name of database to store data",
		"Your mysql username"};
		for(int i =0 ; i < 5 ; i++){
			db[0][i] = d[i];
			db[1][i] = load.getValue(d[i]);
			db[2][i] = s[i];
		}
		return db;
	}
	
	public static double getFullVersion(){
		return engineversion+subversion;
	}
	
	public String getPropertyFilePath(){
		return this.propfile.getPath();
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
