package tasks.database;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import databases.manager.DatabaseManager;

import bio.xml.XMLHelper_Blastx;
import bio.xml.XML_Blastx;

import tasks.TaskXT;
import tools.Tools_String;
import tools.Tools_System;

public class Task_Blast extends TaskXT{
	
	private boolean upload;
	private boolean fuzzynames;
	private String input;
	private File[] files;
	private boolean[] ignore;
	DatabaseManager manager;
	private String dbname;
	private String date;
	private int run_id;
	
	public Task_Blast(){
		setHelpHeader("--This is the Help Message for the the blast Task--");
		run_id =-1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		if(cmd.hasOption("u"))upload=true;
		if(cmd.hasOption("db"))dbname=cmd.getOptionValue("db");
		if(cmd.hasOption("i"))input=cmd.getOptionValue("i");
		if(cmd.hasOption("f"))fuzzynames = true;
		if(cmd.hasOption("run_id")){
			Integer g = Tools_String.parseString2Int(cmd.getOptionValue("run_id"));
			if(g !=  null)run_id =g;
			else logger.error("Run id should be integer only, crap in, crap out");
		}
		if(cmd.hasOption("date"))date=cmd.getOptionValue("date");
	}
	
	public void buildOptions(){
		super.buildOptions();
		
		options.addOption(new Option("r","run_id", true, "Force set run id"));
		options.addOption(new Option("date", true, "Set date when blast was run, use format"+Tools_System.SQL_DATE_FORMAT+", [RECOMMENDED]"));
		options.addOption(new Option("u","upload", false, "Perform default setup"));
		options.addOption(new Option("i","input", true, "Input folder or file"));
		options.addOption(new Option("db","dbname", true, "Default database name, such as genbank, uniprot etc..."));
		options.addOption(new Option("f","fuzzy", false, "Check for fuzzy names before failing, " +
				"may be help if blast query-id is different from database id. May lead to incorrect "));
	}
	
	public Options getOptions(){
		return this.options;
	}

	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		File in=null;
		if(input !=null)in = new File(input);
		openChecklist();
		if(in == null || !in.exists()){
			logger.error("File "+input+" does not exists");
			return;
		}
		if(upload){
			manager.open();
			if(in.isDirectory()){
				files = in.listFiles();
				ignore = new boolean[files.length];
				if(checklist.inRecovery()){
					trimRecovered(checklist.getData());
				}
				int i=0;
				int c =0;
				for(;i < files.length; i++){
					if(!ignore[i]){
						try{
							logger.debug("Running blast upload method");
							uploadBlastFile(manager, files[i], this.fuzzynames, this.dbname, run_id, date);
						}
						catch(Exception e){
							logger.error("Failed to parse file " + files[i].getName(),e);
						}
						checklist.update(files[i].getName());
						c++;
					}
					System.out.print("\rFile No.: "+i+" 		");
				}
				System.out.println();
				logger.info("Files: "+i+", Uploaded: "+c+", Skipped: "+(i-c));
			}
			else{
				try{
					uploadBlastFile(manager, in, this.fuzzynames, this.dbname, run_id, date);
				}
				catch(Exception e){
					logger.error("Failed to parse file " + in.getName(),e);
				}
			}
			checklist.complete();
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public static void uploadBlastFile(DatabaseManager manager, File file, boolean fuzzynames, String dbname, int run_id, String date) throws Exception{
		XMLHelper_Blastx helper = new XMLHelper_Blastx(new XML_Blastx(file));
		helper.setRun_id(run_id);
		helper.setDate(date);
		helper.upload2BioSQL(manager, fuzzynames, dbname);
	}
	
	private void trimRecovered(String[] data){
		for(int i =0;i < data.length; i++){
			for(int j =0; j < files.length; j++){
				if(data[i].equals(files[i].getName())){
					this.ignore[i] = true;
					break;
				}
			}
		}
	}
}
