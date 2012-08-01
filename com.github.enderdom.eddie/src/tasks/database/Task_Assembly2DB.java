package tasks.database;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import bio.assembly.ACEFileParser;
import bio.assembly.ACERecord;
import bio.fasta.Fasta;
import bio.fasta.FastaParser;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.bioSQL.psuedoORM.BioSequence;
import databases.manager.DatabaseManager;

import tasks.TaskXTwIO;
import tools.Tools_System;
import tools.bio.Tools_Assembly;

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
	private boolean remaplocations;
	private boolean mapcontigs;
	private boolean unpad;
	private String identifier;
	private String programname;
	private int runid;
	
	public Task_Assembly2DB(){
		setHelpHeader("--This is the Help Message for the Assemby2DB Task--");
		runid =-1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("uploadreads"))uploadreads=true;
		if(cmd.hasOption("uploadcontigs"))uploadcontigs=true;
		if(cmd.hasOption("remaploc"))remaplocations=true;
		if(cmd.hasOption("mapcontigs"))mapcontigs=true;
		if(cmd.hasOption("identifier"))this.identifier=cmd.getOptionValue("identifier");
		if(cmd.hasOption("programname"))this.programname=cmd.getOptionValue("programid");
		if(cmd.hasOption("unpad"))this.unpad = true;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("u","uploadreads", false, "Uploads a read Fasta or Fastq file"));
		options.addOption(new Option("r","remaploc", false, "Remap read Locations with ACE file (only if upload already run)"));
		options.addOption(new Option("c","uploadcontigs", false, "Uploads a contigs from ACE (run separate from uploadreads)"));
		options.addOption(new Option("m","mapcontigs", false, "Map Contigs to reads, reads should have been uploaded, can be done in parallel with -c"));
		options.addOption(new Option("id","identifier", true, "Uses this as the base identifier for contigs, such as CLCbio_Contig_"));
		//options.addOption(new Option("species", false, "Drags out the default Species")); //TODO
		//options.addOption(new Option("taxon_id", false, "Set the taxon_id"));
		options.addOption(new Option("pid","programname", true, "Set Assembly program namer"));
		options.addOption(new Option("runid", true, "Preset the run id (id column from db), this bypasses questioning if multiple assemblies with same assembler program"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	//TODO implement use of the checklist
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		openChecklist();
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		if(manager.open()){
			//UPLOADING READS
			if(uploadreads){
				File file = new File(this.input);
				if(file.exists()){
					if(filetype == null)this.filetype =detectFileType(input);
					boolean fastq = false;
					if(filetype.equals("FASTQ")){
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
						
						checklist.start(this.args);
						
						LinkedHashMap<String, String> sequences = fasta.getSequences();
						BioSQL bs = manager.getBioSQL();
						int biodatabase_id = manager.getEddieDBID();
						logger.debug("Bio Database id: "+biodatabase_id);
						if(biodatabase_id < 0){
							logger.error("Nobiodatase entry for Eddie");
							return;
						}
						int count =0;
						int size = sequences.size();
						if(checklist.inRecovery()){
							logger.debug("Checklist is in recovery, trimming completed data");
							String[] seqs = checklist.getData();
							for(int i =0 ; i < seqs.length;i++){
								sequences.remove(seqs[i]);
							}
						}
						logger.debug("Uploading....");
						
						for(String s : sequences.keySet()){
							String seq = sequences.get(s);
							if(!bs.addSequence(manager.getCon(), biodatabase_id, null, s, s, this.identifier+count, "READ", null, 0, seq, BioSQL.alphabet_DNA))break;
							count++;
							System.out.print("\r"+(count) + " of " +size + "       ");
							checklist.update(s);
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
			else if(uploadcontigs || remaplocations){
				File file = new File(this.input);
				if(file.exists()){
					if(filetype == null)this.filetype =detectFileType(input);
					if(this.filetype.equals("ACE")){
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
						}
						try{
							ACEFileParser parser = new ACEFileParser(file);
							ACERecord record = null;
							int count =0;
							boolean mapping = true;
							String[] done = null;
							if(checklist.inRecovery()) {
								done= checklist.getData();
								logger.debug("Can skip: " + done.length + " files previously done");
							}
							while(parser.hasNext()){
								record = parser.next();
								String name = record.getContigName();
								boolean skip =false;
								if(checklist.inRecovery())for(int i =0 ; i < done.length ; i ++){if(done[i].equals(name))skip=true;};
								if(!skip){
									if(uploadcontigs){
										if(bs.getBioEntry(manager.getCon(),  this.identifier+count,  this.identifier+count, biodatabase_id) <0){
											
											String seq = record.getConsensusAsString();
											if(unpad)seq.replaceAll("-", "");
											if(!bs.addSequence(manager.getCon(), biodatabase_id, null, name, this.identifier+count, this.identifier+count, "CONTIG", record.getContigName(), 0, seq, BioSQL.alphabet_DNA))break;
											if(mapcontigs && mapping){
												mapping = mapReads(record, manager, this.identifier+count, biodatabase_id, runid);
												if(!mapping){
													int j =ui.requiresUserYNI("Mapping failed for some reason, Continue uploading contigs without mapping to reads?", "Mapping Failure Message");
													if(j != 0)return;
												}
											}
										}
										else{
											System.out.print("\r"+"Skipped Contig, already exists...                ");
										}
									}
								}
								else{
									logger.debug("Skipped as in checklist");
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
						logger.warn("Sorry the filetype " + this.filetype + " is not supported yet.");
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
	
	public boolean mapReads(ACERecord record, DatabaseManager manager, String identifier, int biodatabase_id, int runid){
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
				System.out.print("\r"+"Mapping "+identifier+"... Read No."+i+"             ");
				int read_version = readVersioning(record, manager, read_id, i);
				//As ace isn't normally trimmed, assumes not trimmed
				if(!bsxt.mapRead2Contig(manager, bioentry_id, read_id, read_version, runid, start, end, false)){
					logger.error("Read mapping has failed");
					return false;
				}
			}
		}
		return true;
	}

	public int readVersioning(ACERecord record, DatabaseManager manager, int read_id, int read_numb){
		BioSequence[] biosequences = manager.getBioSQLXT().getBioSequences(manager, read_id);
		int v_high = -1;
		for(BioSequence s : biosequences){
			try{
				if(s.rawDNACompare(record.getRead(read_numb))){
					return s.getVersion();
				}
			}
			catch(Exception e){
				logger.error("Problem with the reads, they day they're not actually DNA/RNA");
			}
			if(s.getVersion() > v_high){
				v_high = s.getVersion();
			}
		}
		v_high++;
		manager.getBioSQL().addBiosequence(manager.getCon(), v_high, record.getRead(read_numb).getLength(), BioSQL.alphabet_DNA, record.getReadAsString(read_numb), read_id);
		return v_high;
	}

}

