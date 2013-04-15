package enderdom.eddie.tasks.database;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;

public class Task_Taxonomy extends TaskXT{	
	
	private String localdb;
	private NCBI_DATABASE ncbidb;
	private boolean updateacc;
	private boolean updatepar;
	private boolean depth;
	private boolean node;
	private boolean speciesQuery;
	private boolean taxids;
	private String node_rank; 
	private int blastRunID;
	private int assRunID;
	private double evalue;
	private int hit_no;
	private String output;
	
	public Task_Taxonomy(){
		setHelpHeader("--This is the Help Message for the the Taxonomy Task--");
		localdb = "nr";
		ncbidb = NCBI_DATABASE.protein;
		updateacc=false;
		updatepar=false;
		depth=false;
		node_rank="phylum";
		blastRunID=-1;
		assRunID=-1;
		evalue=-1;
		hit_no=-1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("OP_nd")){
			ncbidb = NCBI_DATABASE.valueOf(cmd.getOptionValue("n").trim());
			if(ncbidb == null){
				logger.error(cmd.getOptionValue("n") + " is not a valid database");
				StringBuffer b = new StringBuffer();
				for(NCBI_DATABASE v : NCBI_DATABASE.values())b.append(v+", ");
				logger.info("Select from: "+b.toString());
			}
		}
		if(cmd.hasOption("OP_ld")){
			localdb = cmd.getOptionValue("ld");
		}

		if(cmd.hasOption("OP_ua"))updateacc=true;
		if(cmd.hasOption("OP_up"))updatepar=true;
		if(cmd.hasOption("OP_de"))depth=true;
		if(cmd.hasOption("OP_spec"))speciesQuery=true;
		if(cmd.hasOption("OP_spec_taxids")){
			speciesQuery=true;
			taxids=true;
		}
		if(cmd.hasOption("OP_node")){
			node_rank=cmd.getOptionValue("node");
			if(node_rank != null & node_rank.length() >0){
				node =true;
			}
			else{
				node=true;
				node_rank="phylum";
				logger.warn("Node not set, defaulting to 'phylum'");
			}
		}
		if(cmd.hasOption("OP_spec"))speciesQuery=true;
		if(cmd.hasOption("rb")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("rb"));
			if(i !=null)blastRunID=i;
			else logger.error("Failed to parse rb" + cmd.getOptionValue("rb"));
		}
		if(cmd.hasOption("ra")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("ra"));
			if(i !=null)assRunID=i;
			else logger.error("Failed to parse ra" + cmd.getOptionValue("ra"));
		}
		if(cmd.hasOption("no_hit")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("no_hit"));
			if(i !=null)hit_no=i;
			else logger.error("Failed to parse hit" + cmd.getOptionValue("no_hit"));
		}
		if(cmd.hasOption("evalue")){
			Double i = Tools_String.parseString2Double(cmd.getOptionValue("evalue"));
			if(i !=null)evalue=i;
			else logger.error("Failed to parse rb" + cmd.getOptionValue("evalue"));
		}
		if(cmd.hasOption("o"))this.output=cmd.getOptionValue("o");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("OP_nd", "ncbidb", true,  "Name of nbci database (default protein)"));
		options.addOption(new Option("OP_ld", "localdb", true,  "Name of as used in local biosql db (default nr)"));
		options.addOption(new Option("OP_ua", "updateAcc", false,  "Update ncbi accessions with taxonomy information"));
		options.addOption(new Option("OP_up", "updateParents", false,  "Update taxonomy with parent information"));
		options.addOption(new Option("OP_de", "depthTravel", false,  "Run this after all updates to regenerate depth map"));
		options.addOption(new Option("OP_node", "nodeStats", true,  "Run statistics for node_rank, ie -ne phylum will output phylum count"));
		options.addOption(new Option("OP_spec", "speciesQuery", false,  "Only hits with evalue will be used ie -evalue 1e-3 "));
		options.addOption(new Option("OP_spc_taxids", "speciesQueryTaxid", false,  "Output op_spec with taxon ids not species name"));
		options.addOption(new Option("rb", "blastRunId", true,  "Run id for blast for -nd"));
		options.addOption(new Option("ra", "assemblyRunId", true,  "Run id for assembly for -nd"));
		options.addOption(new Option("o", "output", true,  "Output for stats if not wanting to print to console"));
		options.addOption(new Option("no_hit", true,  "Set hit to filter by ie -hit 5 will only use hits 1-5"));
		options.addOption(new Option("evalue", true,  "Only hits with evalue will be used ie -evalue 1e-3 "));
		
	}
	
	public Options getOptions(){
		return this.options;
	}

	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Taxonomy Task @ "+Tools_System.getDateNow());
		if(updateacc){
			DatabaseManager manager = this.ui.getDatabaseManager(password);
			try {
				if(manager.open()){
					logger.info("About to update accessions with taxon ids");
					if(!manager.getBioSQLXT().updateAccs(manager, localdb, ncbidb)){
						logger.error("An error occured trying to update accessions");
					}
				}
			} catch (Exception e) {
				logger.error("An error occured trying to update accessions",e);
			}
		}
		if(updatepar){
			DatabaseManager manager = this.ui.getDatabaseManager(password);
			try {
				if(manager.open()){
					logger.info("About to update all parents with full records");
					if(!manager.getBioSQLXT().updateTaxonParents(manager)){
						logger.error("An error occured trying to add parents");
					}
				}
			} catch (Exception e) {
				logger.error("An error occured trying to add parents", e);
			}
		}
		if(depth){
			try {
				DatabaseManager manager = this.ui.getDatabaseManager(password);
				if(manager.open()){
					logger.info("About to run depth traversal");
					if(!manager.getBioSQLXT().depthTraversalTaxon(manager, Tools_NCBI.ncbi_root_taxon)){
						logger.error("An error occured trying to add depth");
					}
				}
			} catch (Exception e) {
				logger.error("An error occured trying to run depth traversal", e);
			}
		}
		if(node){
			try {
				DatabaseManager manager = this.ui.getDatabaseManager(password);
				if(assRunID < 1 || blastRunID < 1){
					logger.error("Failed as assembly or blast run ids not set");
					setCompleteState(TaskState.ERROR);
					return;
				}
				if(manager.open()){
					logger.info("About to generate statistics about " + node_rank);
					HashMap<Integer, String> map = manager.getBioSQLXT().getNodeRank(manager, node_rank);
					int[] m = new int[map.size()];
					int c=0;
					for(Integer i : map.keySet()){
						m[c]=i;
						c++;
					}
					int n[][] = manager.getBioSQLXT().getTaxonPerAssembly(manager, m, assRunID, blastRunID, evalue, hit_no);
					System.out.println("Outputting counts:");
					StringBuffer b = new StringBuffer();
					for(int i =0;i < m.length;i++){
						if(this.output == null){
							System.out.println(map.get(n[0][i])+" " + n[1][i]);
						}
						else b.append(map.get(n[0][i])+" " + n[1][i]+Tools_System.getNewline());
					}
					if(this.output != null){
						Tools_File.quickWrite(b.toString(), new File(this.output), false);
					}
				}
			} catch (Exception e) {
				logger.error("An error occured trying to run depth traversal", e);
			}
		}
		if(speciesQuery){
			try {
				DatabaseManager manager = this.ui.getDatabaseManager(password);
				logger.info("Running species query operation");
				if(blastRunID < 1){
					logger.error("Failed as blast run ids not set");
					setCompleteState(TaskState.ERROR);
					return;
				}
				if(output==null) throw new Exception("Output is null!");
				if(hit_no < 1)logger.warn("Hit number not set, so stats will be for all hits attached to run");
				if(evalue < 0)logger.warn("Evalue not set will output all hits regardless of e");
				File f= new File(FilenameUtils.getFullPath(output)+FilenameUtils.getBaseName(output)+".csv");
				if(manager.open()){
					if(assRunID > 0){
						if(manager.getBioSQLXT().runSpeciesQuery(manager,f, assRunID, blastRunID, evalue, hit_no, taxids)){
							logger.info("Appears to have succesfully run, output at " +f.getPath());
						}
						else logger.error("Error returned, please check logs");
					}
					else{
						int[] asses = manager.getBioSQLXT().getRunId(manager, null, Run.RUNTYPE_ASSEMBLY);
						for(int i=0;i < asses.length; i++){
							Run run = manager.getBioSQLXT().getRun(manager, asses[i]);
							String filename = run.getProgram().replaceAll(" ", "_") +"_"; 
							filename += (run.getSource().indexOf(" ") != 1) ? run.getSource().split(" ")[0] : run.getSource();
							f = new File(FilenameUtils.getFullPath(output)+filename+".csv");
							if(manager.getBioSQLXT().runSpeciesQuery(manager,f, asses[i], blastRunID, evalue, hit_no, taxids)){
								logger.info("Appears to have succesfully run, output at " +f.getPath());
							}
							else logger.error("Error returned, please check logs");
						}
					}
				}
			} catch (Exception e) {
				logger.error("An error occured run species query", e);
			}
			
		}
		logger.debug("Finished running Taxonomy Task @ "+Tools_System.getDateNow());
		setCompleteState(TaskState.FINISHED);
	}

}
