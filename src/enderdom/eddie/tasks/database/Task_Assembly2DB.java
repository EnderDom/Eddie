package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.assembly.ACEFileParser;
import enderdom.eddie.bio.assembly.ACERecord;
import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.fasta.FastaParser;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceObject;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQL;
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.Tools_Assembly;
import enderdom.eddie.tools.bio.Tools_Bio_File;

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
	private double readcount;
	private double readcounter;
	private int perc;
	private BioFileType type;
	
	public Task_Assembly2DB(){
		setHelpHeader("--This is the Help Message for the Assemby2DB Task--");
		runid =-1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("uploadreads"))uploadreads=true;
		if(cmd.hasOption("uploadcontigs"))uploadcontigs=true;
		//if(cmd.hasOption("remaploc"))remaplocations=true;
		if(cmd.hasOption("mapcontigs"))mapcontigs=true;
		if(cmd.hasOption("identifier"))this.identifier=cmd.getOptionValue("identifier");
		if(cmd.hasOption("programname"))this.programname=cmd.getOptionValue("programid");
		if(cmd.hasOption("pad"))this.unpad = true;
		if(cmd.hasOption("runid")){
			Integer a = Tools_String.parseString2Int(cmd.getOptionValue("runid"));
			if(a != null)runid=a.intValue();
			else{
				logger.error("Run id is not a number!!");
			}
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("pad", false, "Leave padding characters in upload(*/-)"));
		options.addOption(new Option("u","uploadreads", false, "Uploads a read Fasta or Fastq file"));
		//options.addOption(new Option("r","remaploc", false, "Remap read Locations with ACE file (only if upload already run)"));
		options.addOption(new Option("c","uploadcontigs", false, "Uploads a contigs from ACE (run separate from uploadreads)"));
		options.addOption(new Option("m","mapcontigs", false, "Map Contigs to reads, reads should have been uploaded, can be done in parallel with -c"));
		options.addOption(new Option("id","identifier", true, "Uses this as the base identifier for contigs, such as CLCbio_Contig_"));
		//options.addOption(new Option("species", false, "Drags out the default Species")); //TODO
		//options.addOption(new Option("taxon_id", false, "Set the taxon_id"));
		options.addOption(new Option("pid","programname", true, "Set Assembly program namer"));
		options.addOption(new Option("runid", true, "Preset the run id (id column from db), this bypasses questioning if multiple assemblies with same assembler program"));
		options.removeOption("w");
		options.removeOption("o");
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	//TODO implement use of the checklist
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		this.checklist = openChecklist(ui);
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		if(manager.open()){
			//UPLOADING READS
			if(this.identifier == null){
				this.identifier = ui.requiresUserInput("Please Enter a unique identifier:", "Identifier required, maybe Digest_Read? or Mar12_CAP3_Contig?");
			}
			if(uploadreads){
				File file = new File(this.input);
				if(file.exists()){
					type = Tools_Bio_File.detectFileType(input);
					boolean fastq = false;
					if(type == BioFileType.FASTQ){
						fastq=true;
						logger.debug("File is detected as Fastq");
					}
					Fasta fasta = new Fasta();
					fasta = new Fasta();
					fasta.setFastq(true);
					FastaParser parser = new FastaParser(fasta);
					try{
						logger.debug("Parsing....");
						if(fastq)parser.parseFastq(file);
						else parser.parseFasta(file);
						
						super.checklist.start(this.args);
						
						BioSQL bs = manager.getBioSQL();
						int biodatabase_id = manager.getEddieDBID();
						logger.debug("Bio Database id: "+biodatabase_id);
						if(biodatabase_id < 0){
							logger.error("Nobiodatase entry for Eddie");
							return;
						}
						int count =0;
						int size = fasta.getNoOfSequences();
						logger.info("Fasta contains " + size + " sequences" );
						if(checklist.inRecovery()){
							logger.debug("Checklist is in recovery, trimming completed data");
							String[] seqs = checklist.getData();
							int rem = 0;
							for(int i =0 ; i < seqs.length;i++){
								fasta.remove(seqs[i]);
								rem++;
							}
							logger.info(rem+" sequences skipped due to already uploaded");
						}
						logger.debug("Uploading....");
						
						while(fasta.hasNext()){
							SequenceObject o = fasta.next();
							if(!bs.addSequence(manager.getCon(), biodatabase_id, null, o.getIdentifier(), o.getIdentifier(),
									this.identifier+count, "READ", null, 0, o.getSequence(), BioSQL.alphabet_DNA)){
								logger.error("An error occured uploading " + o.getIdentifier());
								break;
								
							}
							count++;
							System.out.print("\r"+(count) + " of " +size + "       ");
							checklist.update(o.getIdentifier());
						}
						System.out.println();
						if(count != size-1){
							logger.error("Failed to upload all the sequences");
						}
						else{
							checklist.complete();
						}
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
						if(runid == -1){
							runid = Tools_Assembly.getSingleRunId(manager, programname, BioSQLExtended.assembly);
							logger.debug("Run id for " + this.programname+ " returned as " + runid);
							if(runid == -1){
								logger.error("This upload needs to be tied to a run, see -task uploadrun");
								return;
							}
						}
						try{
							
							ACEFileParser parser = new ACEFileParser(file);
							ACERecord record = null;
							this.readcount=parser.getReadsSize();
							int count =1;
							boolean mapping = true;
							String[] done = null;
							if(checklist.inRecovery()) {
								done= checklist.getData();
								logger.debug("Can skip: " + done.length + " files previously done");
							}
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
											if(mapcontigs && mapping){
												mapping = mapReads(record, manager, this.identifier+count, biodatabase_id, runid, count);
												if(!mapping){
													int j =ui.requiresUserYNI("Mapping failed for some reason, Continue uploading contigs without mapping to reads?", "Mapping Failure Message");
													if(j != 0)return;
												}
											}
										}
										else{
											System.out.print("\r"+"Contig No.: "+count+"Skipped Contig ");
										}					
										count++;
									}
								}
								else{
									logger.debug("Skipped contig as it's in the checklist");
								}
								checklist.update(name);
							}
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
			else{
				logger.error("No option selected");
			}
			manager.close();
		}
		else{
			logger.error("Failed to open Connection");
		}
		
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public boolean mapReads(ACERecord record, DatabaseManager manager, String identifier, int biodatabase_id, int runid, int count){
		BioSQL bs = manager.getBioSQL();
		BioSQLExtended bsxt = manager.getBioSQLXT();
		int bioentry_id = bs.getBioEntry(manager.getCon(), identifier, null, biodatabase_id);
		for(int i =0; i < record.getNoOfReads() ; i++){
			
			String read = record.getReadName(i);
			int read_id = bs.getBioEntry(manager.getCon(), read, read, biodatabase_id);
			if(read_id < 0){
				logger.error("Oh dear read "+ read + " does not seem to be in the database we cannot map reads not int the db");
				return false;
			}
			else{
				int offset = record.getReadOffset(i);
				int start = offset;
				int end = offset+record.getRead(i).getLength();
				char c = record.getReadCompliment(i);
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
				readcounter+=1;
				perc = (int)((readcounter/readcount)*100);
				System.out.print("\r"+"Contig No>:"+count+", mapping Read No.:"+i+" (Completion: "+perc+"%)  ");
			}
		}
		return true;
	}	
	

}

