package enderdom.eddie.tasks.subtasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.tasks.Checklist;
import enderdom.eddie.tasks.TaskStack;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Blast;
import enderdom.eddie.tools.bio.Tools_Fasta;


public class SubTask_Blast extends SubTask{
	
	TaskStack tasker;
	boolean remote;
	Checklist check;
	String blast_prg; 
	String blast_bin; 
	String blast_db;
	String blastparams;
	SequenceList seqlist;
	boolean clipname;
	String outfolder;
	
	public SubTask_Blast(SequenceList list, TaskStack tasker, boolean remote, Checklist check, String outfolder, boolean clipname){
		this.tasker = tasker;
		this.remote = remote;
		this.check = check;
		this.seqlist = list;
		this.outfolder = outfolder;
	}
	
	
	public void setBlastDetails(String blast_prg, String blast_bin, String blast_db, String blastparams){
		this.blast_prg   = blast_prg;
		this.blast_bin   = blast_bin; 
		this.blast_db    = blast_db; 
		this.blastparams = blastparams;
	}

	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		String id;
		if(tasker.empty()){
			logger.error("Task started, but Vector holding task information is empty?");
		}
		while(!tasker.empty()){
			id=tasker.pop();
			logger.debug("Task with id " + id + " retrieved, writing to temp file");
			FileWriter fstream = null;
			BufferedWriter out = null;
			File temp = null;
			try{
				logger.debug("Building temp file to blast " + id);
				temp = File.createTempFile("TempBlast", ".fasta");
				fstream = new FileWriter(temp, false);
				out = new BufferedWriter(fstream);
				Tools_Fasta.saveFasta(id,seqlist.getSequence(id).getSequence(),out);
				logger.trace("Saved to " + temp.getPath() + " stream closed");
				fstream.close();
				String outname = id;
				if(outname.indexOf(" ") != -1 && clipname)outname = outname.substring(0, outname.indexOf(" "));
				else if (outname.indexOf(" ") != -1 && !clipname) outname = outname.replaceAll(" ", "_");
				File ou = new File(outfolder+Tools_System.getFilepathSeparator()+outname+".xml");
				if(!remote)logger.debug("Running blast locally...");			
				else logger.debug("Running remote blast...");
				Tools_Blast.runLocalBlast(temp, this.blast_prg, this.blast_bin, this.blast_db, this.blastparams,ou, this.remote);
				check.update(id);
			}
			catch(IOException io){
				logger.error("Error saving Fasta to temporary directory");
				setComplete(error);
				return;
			}
		}
		check.complete();
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
}
