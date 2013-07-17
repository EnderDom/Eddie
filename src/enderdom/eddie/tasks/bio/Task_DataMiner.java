package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.bio.homology.PantherGene;
import enderdom.eddie.bio.homology.blast.BlastObject;
import enderdom.eddie.bio.homology.blast.MultiblastParser;
import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.EddieException;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;
import enderdom.eddie.tools.bio.Tools_Panther;
import enderdom.eddie.tools.bio.Tools_Uniprot;
import enderdom.eddie.tools.bio.UNIPROT_EXIST;

public class Task_DataMiner extends TaskXT{

	private String panther;
	private String blast;
	private String organism;
	private String output;
	private boolean exist;
	
	public Task_DataMiner(){
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		panther = getOption(cmd, "PTHR", null);
		organism = getOption(cmd, "organism", null);
		output = getOption(cmd, "output", null);
		exist = cmd.hasOption("confirmedOnly");	
		blast = getOption(cmd, "BLAST", null);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption("PTHR",true, "Download sequences attached to this panther term");
		options.addOption("BLAST",true, "Download sequences in this blast file");
		options.addOption("organism",true, "Limit to organism");
		options.addOption("o","output",true, "Output file for data");
		options.addOption("confirmedOnly", false, "Only get sequences with protein confirmed (Will only download from uniprot) [PTHR Only]");
		options.removeOption("p");
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(panther != null && output != null){
			runPantherScript();
		}
		else if(blast != null  && output != null){
			runBlastScript();
		}
		else if(output == null)logger.error("output is null");
		else logger.error("Missing parameters not set");
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public void runBlastScript(){
		File f = new File(blast);
		if(f.exists()){
			Fasta out = new Fasta();
			if(f.isFile()){
				subBlastScript(f, out);
			}
			else if(f.isDirectory()){
				File[] fs = f.listFiles();
				for(int i =0;i < fs.length;i++){
					if(FilenameUtils.isExtension(fs[i].getName(), "xml")){
						subBlastScript(fs[i], out);
					}
				}
			}
			try {
				out.saveFile(new File(output), BioFileType.FASTA);
			} 
			catch (IOException e) {
				logger.error("Failed to save to file " + output);
				logger.info("Attempting to dump file into log...");
				out.dump();
			}
			catch( UnsupportedTypeException u){
				logger.error("You should not see this unless things have gone terribly wrong");

			}
		}
		else logger.error("Blast file is not a file");
	}
	
	public void subBlastScript(File f, Fasta out){
		logger.debug("Retrieving sequences from file:"+f.getName());
		try {
			MultiblastParser parser = new MultiblastParser(MultiblastParser.BASICBLAST, f);
			BlastObject o = null;
			NCBI_DATABASE db = null;
			while(parser.hasNext()){
				o = parser.next();
				db = Tools_NCBI.getDBfromDB(o.getBlastTagContents("BlastOutput_db"));
				String s = null;
				for(int i =1; i <= o.getNoOfHits() ;i++){
					s = o.getHitTagContents("Hit_accession", i);
					if(s!=null){
						out.addSequenceObject(Tools_NCBI.getSequencewAcc(db,s));
					}
					else logger.warn("Failed to get hit accession for hit number "+i);
				}
			}
		} 
		catch (Exception e) {
			logger.error("Failed to parser file as a blast file", e);
		}
	}
	
	public void runPantherScript(){
		try{
			Fasta f = new Fasta();
			PantherGene[] genes = Tools_Panther.getGeneList(panther);
			String shortn = null;
			if(organism != null){
				shortn = Tools_Panther.getShortSpeciesName(organism);
				logger.debug("Retrieved short name as "  +shortn);
			}
			int ncbin=0, uidn =0, fail=0, cull =0;
			logger.debug("Retrieving sequences from online databases");
			for(PantherGene gene : genes){
				String uid = gene.getUniprot();
				String ncbi = gene.getNCBI();
				if(exist){
					ncbi = null;
					if(uid != null){
						try{
							uid = Tools_Uniprot.getExistLvl(uid) == UNIPROT_EXIST.protein ? uid : null;
						}
						catch(Exception e){
							logger.error("Failed to ascertain if protein exists",e);
							uid=null;
						}
					}
				}
				if(shortn != null){
					if(gene.getShortSpecies().equals(shortn)){
						try{
							if(uid != null){
								f.addSequenceObject(Tools_Uniprot.getUniprot(uid));
								uidn++;
							}
							else if(ncbi != null){
								f.addSequenceObject(Tools_NCBI.getSequencewAcc(NCBI_DATABASE.protein, ncbi));
								ncbin++;
							}
							else{
								logger.trace("No support for downloading " +gene.getGeneAcc());
								fail++;
							}
						}
						catch(EddieException e){
							logger.error("Could not download (Record possibly deleted?)  " +gene.getGeneAcc(), e);
							fail++;
						}
					}
					else cull++;
				}
				else{
					try{
						if(uid != null){
							f.addSequenceObject(Tools_Uniprot.getUniprot(uid));
							uidn++;
						}
						else if(ncbi != null){
							f.addSequenceObject(Tools_NCBI.getSequencewAcc(NCBI_DATABASE.protein, ncbi));
							ncbin++;
						}
						else{
							logger.trace("No support for downloading " +gene.getGeneAcc());
							fail++;
						}
					}
					catch(EddieException e){
						logger.error("Could not download (Record possibly deleted?) " +gene.getGeneAcc(), e);
						fail++;
					}
				}
				System.out.print("\r"+ (uidn+ncbin)+" sequences downloaded    ");
			}
			System.out.println();
			logger.info(cull+" Sequences removed due to incorrect species");
			logger.info(fail+" Sequences not included no uniprot or ncbi sequence available");
			logger.info(uidn+" sequences retrieved from uniprot and "+ ncbin +" retrieved from ncbi");
			logger.info("Saving "+f.getNoOfSequences()+" sequences to fasta");
			f.save2Fasta(output);
		}
		catch(Exception e){
			logger.error("Failed to download panther data", e);
		}
	}
	
	
}
