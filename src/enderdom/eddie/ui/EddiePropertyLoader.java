package enderdom.eddie.ui;

import java.util.Properties;

import enderdom.eddie.cli.LazyPosixParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;

public class EddiePropertyLoader extends BasicPropertyLoader{

    public static String propertyfilename = new String("eddie.properties");
    public static String infoFile = new String("eddie.info");
    public String rootfolder;
    private Options options;
    /* This is actually the 4th iteration of Eddie, 
     * though this one has been written from scratch
     */
    public static int engineversion = 4;
    public static double subversion = 0.68;
    public static String edition = "Development";
    public String[] actions;
	
	public String[] args;
	public String modulename;
	public int mode;
	
	private EddieProperty[] defaultkeys;
	private String[] defaultvalues;
	
	public EddiePropertyLoader(String[] args) {
		setLogname("eddie.log");
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
			else{
				Logger.getRootLogger().info("##### NEW SESSION ######");
				
				StringBuffer buffer = new StringBuffer();
				boolean pass = false;
				for(String s : args){
					if(pass) s ="******";//Quick hack to not save passwords
					pass =(s.equals("-password") || s.equals("-p"));
					buffer.append(s+ " ");
				}
				//Log args because I keep forgetting where i leave folders when dumping them places
				Logger.getRootLogger().info("ARGS: "+buffer.toString());
			}
		}
	}
	
	public void parseArgs(String[] args){
		buildOptions();
		CommandLineParser parser = new LazyPosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h")){
				if(cmd.hasOption("task")){
					for(int i =0; i < args.length; i++){					
						if(args[i].equals("-h") || args[i].equals("--help")){
							mode = 1;
							break;
						}
						if(args[i].equals("-task")){
							break;
						}
					}
				}
			}
			else if(cmd.hasOption("about")){
				mode = 6;
			}
			else{
				/*If Command Line iNterface*/
				mode = 2;
				/*
				 * Store arguments for further parsing by CLI
				 */
				this.args=args;
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
		help.printHelp("java -jar Eddie.jar [args]", "-- Eddie v"+(engineversion+subversion)+" Help Menu --", options, "-- Share And Enjoy! --");
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
		if(checkPropertiesAndLoadFile(Tools_File.getEnvirons(this)+propertyfilename))return true;
		//Then in the home directory
		if(checkPropertiesAndLoadFile(System.getProperty("user.home")+slash+".eddie"+slash+propertyfilename))return true;
		
		preLog("ERROR: don't have permission to save settings in either local folder of user home directory");
		return false;
	}

	
	public void setDefaultProperties(){
		String slash = Tools_System.getFilepathSeparator();
		//Properties to add if not already available
		defaultkeys = new EddieProperty[]{
				EddieProperty.WORKSPACE,EddieProperty.AUXILTHREAD,EddieProperty.CORETHREAD,
				EddieProperty.BLAST_BIN_DIR,EddieProperty.BLAST_DB_DIR, EddieProperty.ESTSCAN_BIN, 
				EddieProperty.FILES_XML,EddieProperty.PREFLNF,EddieProperty.TESTDATADIR,
				EddieProperty.DBTYPE,EddieProperty.DBDRIVER,EddieProperty.DBHOST, 
				EddieProperty.DBNAME,EddieProperty.DBUSER,EddieProperty.UNI_VEC_DB,
				EddieProperty.UNIVEC_URL,EddieProperty.IPRSCAN_BIN,EddieProperty.COLORSTDOUT,
				EddieProperty.CLUSTALW2BIN
				 };
		defaultvalues = new String[]{
				propfile.getParent(), "5", "1", 
				"/usr/bin/", "/home/dominic/bioapps/blast/db/", "/usr/bin/ESTScan",
				"null", defaultlnf, propfile.getParent()+slash+"test", 
				"mysql","com.mysql.jdbc.Driver", "Localhost", 
				DatabaseManager.default_database, "user", "",
				"ftp://ftp.ncbi.nih.gov/pub/UniVec/UniVec",	"/usr/bin/iprscan", "TRUE",
				"/usr/bin/clustalw2"
				};
		
		if(defaultkeys.length != defaultvalues.length)System.out.println("You're being derp Dominic :(");
		
		for(int i =0; i < defaultkeys.length; i++){
			if(!this.props.containsKey(defaultkeys[i].toString())){
				this.props.put(defaultkeys[i].toString(), defaultvalues[i]);
			}
		}
		
		//Forced Overwrite properties
		String[] tempkeys = new String[]{EddieProperty.SUBVERSION.toString(), EddieProperty.VERSION.toString(), 
				EddieProperty.FULLVERSION.toString(), EddieProperty.EDITION.toString()};
		String[] tempvalues = new String[]{EddiePropertyLoader.subversion+"",EddiePropertyLoader.engineversion+"",
				(EddiePropertyLoader.subversion+EddiePropertyLoader.engineversion)+"", edition};
		if(tempkeys.length != tempvalues.length)System.out.println("You're being derp Dominic :(");
		for(int i =0; i < tempkeys.length; i++){
				this.props.put(tempkeys[i], tempvalues[i]);
		}
	}
	
	public static double getVersion(){
		return engineversion+subversion;
	}
	
	public static String getFullVersion(){
		return new String(""+(engineversion+subversion));
	}

	public boolean isTest() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
