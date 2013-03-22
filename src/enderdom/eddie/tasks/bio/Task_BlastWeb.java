package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.cli.Option;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;

import enderdom.eddie.bio.homology.blast.NCBIQBlastServiceXT;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;

public class Task_BlastWeb extends TaskXTwIO{

	public Task_BlastWeb(){
		setCore(false);
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		File in = new File(input);
		if(in.isFile()){
			//TODO
			logger.warn("Incomplete task!!!");
		}
		else{
			logger.error("File stated as input is not file");
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("bdb", "blast_db", true, "Blast database"));
		options.getOption("i").setDescription("Input sequence file (Ace/Fast(a/q)");
		options.getOption("o").setDescription("Output folder");
		options.addOption(new Option("bpr", "blast_prog", true, "Specify blast program"));
		options.addOption(new Option("x", "outputformat", true, "Set Output Format, else defaults to xml"));
		options.addOption(new Option("p", "params", true, "Additional Parameters separate with '/_' not space"));
		//options.addOption(new Option("pf", "paramater file", true, "Additional blast Parameters in external file"));
		options.addOption(new Option("filetype", true, "Specify filetype (rather then guessing from ext)"));
		options.addOption(new Option("clip", false, "Clip output file name to whitespace in input"));
	}
	
	public void runBlast(String sequence, String email, String blastpr, String blastdb, String adv){
		try {
			NCBIQBlastServiceXT ncbi = new NCBIQBlastServiceXT();
			ncbi.setEmail(email);
			ncbi.setTool("eddie");
			NCBIQBlastAlignmentProperties props = new NCBIQBlastAlignmentProperties();
			props.setBlastProgram(convert2enum(blastpr));
			props.setBlastDatabase(blastdb);
			props.setBlastAdvancedOptions(adv);
			String align = ncbi.sendAlignmentRequest(sequence, props);
			int i =0;
			while((i = ncbi.isReadyOrErrd(align, System.currentTimeMillis())) == 0){
				logger.info("Waiting for Blast [RID:"+align+"] ... ");
				Thread.sleep(10000);
			}
			if(i == 1){
				NCBIQBlastOutputProperties outprops = new NCBIQBlastOutputProperties();
				InputStream stream = ncbi.getAlignmentResults(align, outprops);
				Tools_File.stream2File(stream, this.output);
			}
			else{
				logger.error("An error occured with the remote blast: "+Tools_System.getNewline()+ncbi.getErrorString());
			}
			
		}
		catch (Exception e) {
			logger.error("Failed to run web blast correctly", e);
		}
	}
	
	/**
	 * This is a pain :(
	 * 
	 * @param blastpr
	 * @return
	 */
	private BlastProgramEnum convert2enum(String blastpr) throws Exception{
		return BlastProgramEnum.valueOf(blastpr.toLowerCase().trim());
	}
}
