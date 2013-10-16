package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACERecord;
import enderdom.eddie.bio.lists.FastaParser2;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.exceptions.EddieGenericException;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Assembly;
import enderdom.eddie.tools.bio.Tools_Bio_File;
import enderdom.eddie.ui.UserResponse;

/**
 * The idea here is to upload an entire ACE fileset of data  
 * into a biological database ie bioSQL. This is probably beyond
 * what this database is for as is refered to in the  schema regarding location
 * It also may well cause issues if you try and use gbrowse with this
 * ... But I think I'd prefer to use bioSQL than use a custom schema
 * it at least gives others a fighting chance of being able to 
 * interact with this data easily. 
 * ...Perhaps...
 * ...Maybe.
 */

public class Task_Assembly2DB extends TaskXTwIO{

	private boolean uploadreads;
	private boolean uploadcontigs;
	private boolean mapcontigs;
	private boolean unpad;
	private String identifier;
	private String programname;
	private int runid;
	private boolean notrim;
	
	public Task_Assembly2DB(){
		setHelpHeader("--This is the Help Message for the Assemby2DB Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		uploadreads=cmd.hasOption("uploadreads");
		uploadcontigs= cmd.hasOption("uploadcontigs");
		//if(cmd.hasOption("remaploc"))remaplocations=true;
		this.mapcontigs=cmd.hasOption("mapcontigs");
		this.identifier= getOption(cmd, "bid", null);
		this.programname = getOption(cmd, "programid", null);
		this.unpad = cmd.hasOption("pad");
		this.runid = getOption(cmd, "runid", -1);
		this.notrim = cmd.hasOption("notrim");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("pad", false, "Leave padding characters in upload(*/-)"));
		options.addOption(new Option("u","uploadreads", false, "Uploads a read Fasta or Fastq file"));
		//options.addOption(new Option("r","remaploc", false, "Remap read Locations with ACE file (only if upload already run)"));
		options.addOption(new Option("c","uploadcontigs", false, "Uploads a contigs from ACE (run separate from uploadreads)"));
		options.addOption(new Option("m","mapcontigs", false, "Map Contigs to reads, reads should have been uploaded, can be done in parallel with -c"));
		options.addOption(new Option("bid","identifier", true, "Uses this as the base identifier for contigs, such as CLCbio_Contig_"));
		//options.addOption(new Option("species", false, "Drags out the default Species"));
		//options.addOption(new Option("taxon_id", false, "Set the taxon_id"));
		options.addOption(new Option("pid","programname", true, "Set Assembly program namer"));
		options.addOption(new Option("runid", true, "Preset the run id (id column from db), " +
				"this bypasses questioning if multiple assemblies with same assembler program (for mapping use assembly run id)"));
		options.addOption(new Option("noTrim", false, "Don't trim names to whitespace"));
		options.removeOption("w");
		options.removeOption("o");
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	//TODO checklist messed up due to largeInsert
	public void run(){
		setCompleteState(TaskState.STARTED);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		this.checklist = openChecklist(ui);
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		try{
			manager.open();
			//UPLOADING READS
			if(this.identifier == null){
				if(uploadreads || uploadcontigs){
					this.identifier = ui.requiresUserInput("Please Enter a unique identifier:", 
							"Identifier required, maybe Digest_Read? or Mar12_CAP3_Contig?");
				}
			}
			if(uploadreads){
				logger.debug("Running upload reads...");
				if(input == null)logger.fatal("Input is null!");
				File file = new File(this.input);
				if(file.exists()){					
					try{
						logger.debug("Starting checklist");					
						super.checklist.start(this.args);
						
						logger.debug("Initialising Parser...");
						FastaParser2 parser = new FastaParser2(file, !notrim, false, false);
						
						BioSQL bs = manager.getBioSQL();
						int biodatabase_id = manager.getEddieDBID();
						bs.largeInsert(manager.getCon(),true);
						logger.debug("Bio Database id: "+biodatabase_id);
						if(biodatabase_id < 0){
							logger.error("Nobiodatase entry for Eddie");
							return;
						}
						logger.debug("Retrieving Run id");
						if(runid < 1){
							this.runid=spawnRun(manager);
							if(this.runid < 1){
								logger.error("Failed to add run, please add Run with " +
										"-task runDatabase or with mysql client");
								return;
							}
							else logger.debug("Run ID assigned as " + runid);
						}
						int count =0;
						HashSet<String> seqs = null;
						int rem = 0;
						
						if(checklist.inRecovery()){
							logger.debug("Checklist is in recovery, trimming completed data");
							seqs = new HashSet<String>(checklist.getDataList());
						}
						
						while(parser.hasNext()){
							SequenceObject o = parser.next();
							if(seqs != null && seqs.contains(o.getIdentifier())){
								rem++;
								logger.info(rem+" sequences skipped due to already uploaded");
							}
							else{
								if(!bs.addSequence(manager.getCon(), biodatabase_id, null, o.getIdentifier(), o.getIdentifier(),
										this.identifier+(count+rem), "READ", null, 0, o.getSequence(), BioSQL.alphabet_DNA)){
									logger.error("An error occured uploading " + o.getIdentifier());
									break;
								}
								int bioentry = bs.getBioEntry(manager.getCon(), this.identifier+(count+rem), o.getIdentifier(), biodatabase_id);
								if(bioentry < 0){
									throw new EddieGenericException("Failed to retrieve bioentry id after adding sequence");
								}
								manager.getBioSQLXT().addRunBioentry(manager, bioentry, this.runid);
								
								count++;
								if(count%10000 == 0){
									System.out.println();
									logger.debug("Uploading cached statements...");
									bs.largeInsert(manager.getCon(),false);
									bs.largeInsert(manager.getCon(),true);
								}
								System.out.print("\r"+(count) + " Sequences    ");
								checklist.update(o.getIdentifier());
							}		
						}
						System.out.println();
						logger.debug("Uploading cached statements...");
						bs.largeInsert(manager.getCon(),false);
						checklist.complete();
					}
					catch(IOException io){
						logger.error("Failed to parse fasta/q file "  +input, io);
					}
					
				}
				else{
					logger.error("File "+ input + " does not exist");
				}
			}
			//UPLOADING CONTIGS
			else if(uploadcontigs){
				logger.debug("Running upload contigs...");
				File file = new File(this.input);
				if(file.exists()){
					BioFileType type = Tools_Bio_File.detectFileType(input);
					if(type == BioFileType.ACE){
						BioSQL bs = manager.getBioSQL();
						int biodatabase_id = manager.getEddieDBID();
						if(biodatabase_id < 0){
							logger.error("No Biodatase entry for Eddie");
							return;
						}
						else logger.debug("Biodatabase id: "+biodatabase_id);
						if(runid < 1){
							runid = Tools_Assembly.getSingleRunId(manager, programname, BioSQLExtended.assembly);
							logger.debug("Run id for " + this.programname+ " returned as " + runid);
							if(runid == -1){
								logger.error("This upload needs to be tied to a run, see -task runDatabase");
								return;
							}
						}
						try{				
							ACEFileParser parser = new ACEFileParser(file);
							ACERecord record = null;
							int count =1;
							boolean mapping = true;
							String[] done = null;
							if(checklist.inRecovery()) {
								done= checklist.getData();
								logger.debug("Can skip: " + done.length + " files previously done");
							}
							bs.largeInsert(manager.getCon(),true);
							while(parser.hasNext()){
								record = (ACERecord) parser.next();
								String name = record.getConsensus().getIdentifier();
								boolean skip =false;
								if(checklist.inRecovery())for(int i =0 ; i < done.length ; i ++){if(done[i].equals(name))skip=true;};
								if(!skip){
									if(uploadcontigs){
										if(bs.getBioEntry(manager.getCon(),  this.identifier+count,  this.identifier+count, biodatabase_id) <0){
											String seq = record.getConsensus().getSequence();
											if(!unpad){
												seq=seq.replaceAll("-", "");
												seq=seq.replaceAll("\\*", "");
											}
											//NOTE: Name is truncated here to fit in, this will need to be taken into account elsewhere!
											if(name.length() > 35)name = name.substring(0, 30)+"..." + count;
											if(!bs.addSequence(manager.getCon(), biodatabase_id, null, name, this.identifier+count, this.identifier+count, "CONTIG", record.getContigName(), 0, seq, BioSQL.alphabet_DNA))break;
											if(runid > 0){
												int bioentry = bs.getBioEntry(manager.getCon(), this.identifier+count, this.identifier+count, biodatabase_id);
												manager.getBioSQLXT().addRunBioentry(manager, bioentry, this.runid);
											}
											if(mapcontigs && mapping){
												mapping = mapReads(record, manager, this.identifier+count, biodatabase_id, runid, count);
												if(!mapping){
													UserResponse j =ui.requiresUserYNI("Mapping failed for some reason, Continue uploading contigs without mapping to reads?", "Mapping Failure Message");
													if(j != UserResponse.YES)return;
												}
											}
											System.out.print("\r"+"Contig No.: "+count );
										}
										else{
											System.out.print("\r"+"Contig No.: "+count+"Skipped Contig ");
										}					
										count++;
										if(count%10000==0){
											bs.largeInsert(manager.getCon(),false);
											bs.largeInsert(manager.getCon(),true);
										}
									}
								}
								else{
									logger.debug("Skipped contig as it's in the checklist");
								}
								checklist.update(name);
							}
							bs.largeInsert(manager.getCon(),false);
							System.out.println();
							checklist.complete();
						}
						catch(IOException io){
							logger.error("Failed to parse file "+this.input+" as ACE", io);
						}
					}
					else{
						logger.warn("Sorry the filetype " + type.toString() + " is not supported yet. Should be ACE");
					}
				}
				else{
					logger.error("File " + this.input + " does not exist");
				}
			}
			else if(mapcontigs){
				logger.debug("Running map contigs to reads...");
				this.checklist.complete();
				if(this.runid < 0){
					logger.error("Assembly run id is required for mapping contigs, " +
							"please find the run id of this assembly" +
							", see -task runDatabase -list");
					return;
				}
				ACEFileParser parser = new ACEFileParser(new File(input));
				int count=0;
	
				manager.getBioSQL().largeInsert(manager.getCon(),true);
				while(parser.hasNext()){
					count++;
					ACERecord record = parser.next();
					if(!mapReads(record, manager, record.getContigName(), manager.getEddieDBID(), this.runid, count)){
						logger.error("Failed to upload "+ record.getContigName());
					}
					if(count%10000==0){
						manager.getBioSQL().largeInsert(manager.getCon(),false);
						manager.getBioSQL().largeInsert(manager.getCon(),true);
					}
				}
				System.out.println();
				manager.getBioSQL().largeInsert(manager.getCon(),false);
			}
			else{
				logger.error("No option selected");
			}
			manager.close();
		}
		catch(Exception e){
			logger.error("Failed to open Connection", e);
			setCompleteState(TaskState.ERROR);
			return;
		}
		
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	/*
	 * TODO Known bug means that run.uploadRun seems to fail
	 * here. I don't know why as it works fine when being called
	 * in Task_AddRunData. There is evidently some issue I'm missing
	 * but I don't currently have time to look into it.
	 * For now just upload a Run with Task_AddRunData separately and use
	 * those...  :S
	 * 
	 */
	private int spawnRun(DatabaseManager manager) {
		Run run = new Run();
		run.setRuntype(Run.RUNTYPE_454);
		run.setDate(Tools_System.getDateNowAsDate());
		logger.warn("No run ID set, we will need to make it now");
		logger.warn("Run IDs can be made with -task runDatabase before running this");
		String s = ui.requiresUserInput("Source?",
				"Enter Source of Reads eg 'E.coli cDNA library', 'Digestive gland' etc..");
		run.setSource(s);
		s = ui.requiresUserInput("Program/Machine?",
				"Enter Program or machine used ie 454 GS FLX, 'Ion Torrent' etc...: ");
		run.setProgram(s);
		s = ui.requiresUserInput("Comment?",
				"Any comment you want to add ie '454 Sequencing on Drosophila larvae'?");
		run.setComment(s);
		return run.uploadRun(manager);	
	}

	public boolean mapReads(ACERecord record, DatabaseManager manager, String identifier, int biodatabase_id, int runid, int count){
		BioSQL bs = manager.getBioSQL();
		BioSQLExtended bsxt = manager.getBioSQLXT();
		int bioentry_id = bs.getBioEntry(manager.getCon(), identifier, identifier, biodatabase_id);
		if(bioentry_id < 1)bioentry_id=bs.getBioEntrywName(manager.getCon(), identifier);
		if(bioentry_id > 0){
			int i =0;
			for(String read : record.getReadNames()){
				int read_id = bs.getBioEntry(manager.getCon(), read, read, biodatabase_id);
				if(read_id < 0){
					int slim = read.lastIndexOf("_");
					String newread=null;
					if(slim !=-1){
						newread = read.substring(0, slim);
						read_id = bs.getBioEntry(manager.getCon(), newread, newread, biodatabase_id);
						if(read_id > 0){
							System.out.println();
							logger.warn("Read "+read+"  not found, but "
									+newread+" was, probably duplicate use of read");
						}
					}
				}
				if(read_id < 0){
					logger.error("Oh dear read "+ read + " does not seem to be in the database we cannot map reads not int the db");
					return false;
				}
				else{
					int offset = record.getOffset(read, 0);
					int start = offset;
					int end = offset+record.getSequence(read).getLength();				
					char c = record.getCompliment(read);
					@SuppressWarnings("unused")
					int comp = 0;
					if(c == 'C'){
						comp = 1;
						//TODO add better strand info
					}
					//As ace isn't normally trimmed, assumes not trimmed
					if(!bsxt.mapRead2Contig(manager, bioentry_id, read_id, 0, runid, start, end, false)){
						logger.error("Read mapping has failed");
						return false;
					}
					System.out.print("\r"+"Contig No>:"+count+", mapping Read No.:"+(i++)+ "     ");
				}
			}
			return true;
		}
		
		else{
			logger.error("Could not retrieve bioentry_id with identifier: "+identifier);
			return false;
		}
	}	
	

}

