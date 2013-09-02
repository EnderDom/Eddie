package enderdom.eddie.tasks.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.EddieOntologyFactory;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.EddieTerm;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;

public class Task_ESTUpload extends TaskXT{
	
	private String prot_suffix = "_prot"; 
	private String input;
	private int runid;
	private DatabaseManager manager;
	private String version;
	private String params;
	private String comment;
	private boolean force;
	private boolean doNotTrim;
	
	public Task_ESTUpload(){
		setHelpHeader("--This is the Help Message for the Est upload Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		input = getOption(cmd, "i", null);
		runid = getOption(cmd, "r", -1);
		version = getOption(cmd, "version", "2.1");
		params = getOption(cmd, "params", null);
		comment = getOption(cmd, "comment", null);
		force=cmd.hasOption("force");
		doNotTrim=cmd.hasOption("d");
		prot_suffix=getOption(cmd, "s", "_prot");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("i","input",true, "Input file from ESTscan containing proteins"));
		options.addOption("r", "runid", true, "Run id of the contig assembly");
		//options.addOption("z", "overwrite", false, "Replace an existing ESTscan of this assembly");
		options.addOption("v", "version", true, "Version of ESTScan, not added will assume 2.1");
		options.addOption("params",true, "Parameters used for the ESTScan");
		options.addOption("force",false, "Force upload, even if source sequence can't be found");
		options.addOption("comment",true, "Add a comment to the run record?");
		options.addOption("suffix",true, "Suffix for protein names, as they are likely the same as parent so need change. Default: _prot");
		options.addOption("d", "dontTrimName", false, "Don't trim names to first space char (or ; char)");
		options.removeOption("w");
		options.removeOption("o");
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running ESTUploadTask @ "+Tools_System.getDateNow());
		if(input != null && runid > 0){
			File f = new File(input);
			if(f.isFile()){
				try {
					manager = ui.getDatabaseManager(password);
					if(manager.open()){
						Run parent = manager.getBioSQLXT().getRun(manager, runid);
						if(parent == null)throw new Exception("Parent run id used, not a run id");		
						//TODO check for run already uploaded
						Run run = new Run(Tools_System.getDateNowAsDate(),"TRANSLATE", runid, "ESTScan", version, parent.getDbname(), parent.getSource(), params, comment);
						int estrun = run.uploadRun(manager);
						LinkedList<String> skipped = new LinkedList<String>();
						if(estrun == -1)throw new Exception("Could not generate new run record");	
						else{
							EddieOntologyFactory onto = new EddieOntologyFactory(manager);
							Term t = onto.getTerm(EddieTerm.TRANSLATE);
							if(t == null)throw new Exception("Failed to provide term id to link the protein with source");
							SequenceList l = SequenceListFactory.getSequenceList(input);
							logger.debug("Parsing Sequences");
							int c=0;int u=0;
							manager.getBioSQL().largeInsert(manager.getCon(), true);
							while(l.hasNext()){
								SequenceObject o2 = l.next();
								String iden = o2.getIdentifier();
								if(!doNotTrim){
									iden=iden.replaceAll(";", " ");
									String[] dents = iden.split(" ");
									if(dents.length > 0)iden = dents[0];
								}
								int parent_id = manager.getBioSQL().getBioEntry(manager.getCon(), iden, iden, manager.getEddieDBID());
								if(parent_id < 1) parent_id = manager.getBioSQL().getBioEntrywName(manager.getCon(), iden);
								if(parent_id > 0 || force){
									manager.getBioSQL().addSequence(manager.getCon(), manager.getEddieDBID(), null, iden+prot_suffix, 
											iden+prot_suffix, iden+prot_suffix, "CDS", null, 1, o2.getSequence(), BioSQL.alphabet_PROTEIN);
									int bioen = manager.getBioSQL().getBioEntry(manager.getCon(),
											iden+prot_suffix,iden+prot_suffix, manager.getEddieDBID());
									if(bioen < 1)throw new Exception("We cannot recover bioentry id after adding the bioentry");
									manager.getBioSQLXT().addRunBioentry(manager, bioen, estrun);
									manager.getBioSQL().addBioEntryRelationship(manager.getCon(), bioen, parent_id, onto.getTerm(EddieTerm.TRANSLATE).getTerm_id(), 1);
									u++;
								}
								else{
									skipped.add(iden);
								}
								
								c++;
								if(c%1000==0){
									manager.getBioSQL().largeInsert(manager.getCon(), false);
									manager.getBioSQL().largeInsert(manager.getCon(), true);
								}
								System.out.print("\r"+c);
							}
							System.out.println();
							manager.getBioSQL().largeInsert(manager.getCon(), false);
							logger.debug("Dealt with " + c + " sequences in file of which "+ u+" appeared to upload");
						}
						if(skipped.size() > 0)dealwithSkipped(skipped);
					}
					else{
						logger.error("Failed to open Database Manager");
						this.setCompleteState(TaskState.ERROR);
						return;
					}
				}catch (Exception e) {
					logger.error("Failed to parse sequences ", e);
					setCompleteState(TaskState.ERROR);
				}
			}
			else logger.error("Input not a file.");
		}
		else if(input == null)logger.error("Input is null");
		else if(runid < 1)logger.error("You need to set the run id for the assembly or sequence set");
		logger.debug("Finished running ESTupload Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	
	public void dealwithSkipped(LinkedList<String> skipped){
		try{
			String work = FilenameUtils.getFullPath(ui.getPropertyLoader().getValue("WORKSPACE")+"out");
			File dir = null;
			if(!(dir=new File(work)).exists())dir.mkdir();
			String newline = Tools_System.getNewline();
			logger.info("Skipped sequences have been listed in this file " + work +"skipped_files.txt" );
			BufferedWriter writ = new BufferedWriter(new FileWriter(new File((work+"skipped_files.txt")),false));
			for(String s : skipped){writ.write(s+newline);writ.flush();}
			writ.close();
		}
		catch(IOException io){
			logger.error("Could not save to file for some reason, dumping skipped ids");
			for(String s: skipped)System.out.print(s+",");
			System.out.println();
		}
	}
}
