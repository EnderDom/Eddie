package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.homology.InterproObject;
import enderdom.eddie.bio.homology.InterproXML;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_Array;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

public class Task_IPRupload extends TaskXT {

	private String input;
	private int runid;
	private String suffix;
	private DatabaseManager manager;
	private int[] count;
	
	public Task_IPRupload(){
		setHelpHeader("--This is the Help Message for the IPR upload Task--");
		runid=-1;
		count = new int[2];
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("i")){
			this.input = cmd.getOptionValue("i");
		}	
		if(cmd.hasOption("r")){
			Integer r = Tools_String.parseString2Int(cmd.getOptionValue("r"));
			if(r != null) runid = r;
			else logger.warn("could not read " + cmd.getOptionValue("r") + " as number");
		}
		if(cmd.hasOption("suffix"))suffix=cmd.getOptionValue("suffix");

	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("i","input",true, "Input file from ESTscan containing proteins"));
		options.addOption("r", "runid", true, "Run id of the protein bioentry");
		options.addOption("suffix",true, "Suffix for protein names, whatever was used in ESTupload");
		
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running IPRUploadTask @ "+Tools_System.getDateNow());
		if(runid< 1){
			this.setCompleteState(TaskState.ERROR);
			logger.error("Run id not set");
			return;
		}
		File f;
		if(input != null && (f=new File(input)).exists()){
			try{
				manager = ui.getDatabaseManager(password);
				if(manager.open()){
					if(f.isDirectory()){
						File[] files = f.listFiles(); 
						for(int i =0; i < files.length;i++){
							count = Tools_Array.sum(count, parseXML(manager, files[i], suffix, runid));
							System.out.print("\r"+i+" of " +files.length);
						}
						System.out.println();
						logger.info("Parsed " + files.length+" files");
					}
					else{
						count = Tools_Array.sum(count, parseXML(manager, f, suffix, runid));
					}
					logger.info(count[0]+" Protein IPR scans with "+(count[0]-count[1])+ " failing");
				}
			}
			catch(Exception e){
				logger.error("Failed to parse xmls",e);
			}
		}
		logger.debug("Finished running IPRUpload Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}

	private int[] parseXML(DatabaseManager manager, File file, String suffix, int run_id)
			throws FileNotFoundException, XMLStreamException{
		InterproXML xml = new InterproXML(file, "", suffix, run_id);
		int[] count = new int[2]; //0 number of interpros, 1 number of uploads
		while(xml.hasNext()){
			InterproObject obj = xml.next();
			if(obj.upload(manager) > 0)count[1]++;
			count[0]++;
		}
		return count;
	}
}
