package tasks.database;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import databases.manager.DatabaseManager;

import bio.xml.XML_Blastx;

import tasks.TaskXT;
import tools.Tools_System;

public class Task_Blast extends TaskXT{
	
	private boolean upload;
	private String input;
	private File[] files;
	private boolean[] ignore;
	DatabaseManager manager;
	
	public Task_Blast(){
		setHelpHeader("--This is the Help Message for the the blast Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		if(cmd.hasOption("u"))upload=true;
		if(cmd.hasOption("i"))input=cmd.getOptionValue("i");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("u","upload", false, "Perform default setup"));
		options.addOption(new Option("i","input", true, "Input folder or file"));
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
				else{
					
				}
				int i=0;
				int c =0;
				for(;i < files.length; i++){
					if(!ignore[i]){
						try{
							uploadBlastFile(manager, files[i]);
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
					uploadBlastFile(manager, in);
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
	
	public static void uploadBlastFile(DatabaseManager manager, File file) throws Exception{

			XML_Blastx xml = new XML_Blastx(file);
			
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
