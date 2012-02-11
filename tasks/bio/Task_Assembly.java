package tasks.bio;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import bio.assembly.ACEParser;
import bio.assembly.BasicAssemblyObject;
import bio.assembly.SAMParser;

import tasks.TaskXT;

public class Task_Assembly extends TaskXT{

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
	}
	
	public void parseOpts(Properties props){
	
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("t", "test",false, "Run Assembly Task Test "));
	}
	
	public void runTest(){
		Logger.getRootLogger().debug("Testing Assembly Task");
		BasicAssemblyObject obj = new BasicAssemblyObject();
		File ace = null;
		if(input.endsWith(".ace") || input.endsWith(".ACE")){
			if((ace = getFile(input,2)) != null){
				Logger.getRootLogger().debug("Parsing ACE file");
				ACEParser parser = new ACEParser(obj);
				try {
					parser.parseAce(ace);
				} 
				catch (IOException e) {
					Logger.getRootLogger().error("Error Parsing ACE file",e);
				}
			}
		}
		else if (input.endsWith(".sam") || input.endsWith(".SAM")){
			if((ace = getFile(input,2)) != null){
				Logger.getRootLogger().debug("Parsing SAM file");
				SAMParser parser = new SAMParser(obj);
				try {
					parser.parseSAM(ace);
				} 
				catch (Exception e) {
					Logger.getRootLogger().error("Error Parsing SAM file",e);
				}
			}
		}
	}
	
}
