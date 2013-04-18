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
import enderdom.eddie.tools.Tools_CLI;
import enderdom.eddie.tools.Tools_File;
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
	private double evalue;
	private int hit_no;
	private String output;
	
	public Task_Taxonomy(){
		setHelpHeader("--This is the Help Message for the the Taxonomy Task--");
		ncbidb = NCBI_DATABASE.protein;
		updateacc=false;
		updatepar=false;
		depth=false;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		
		if(cmd.hasOption("ld")){
			localdb = cmd.getOptionValue("ld");
		}
		if(cmd.hasOption("o"))this.output=cmd.getOptionValue("o");
		if(cmd.hasOption("OP_ua"))updateacc=true;
		if(cmd.hasOption("OP_up"))updatepar=true;
		if(cmd.hasOption("OP_de"))depth=true;
		if(cmd.hasOption("OP_spec"))speciesQuery=true;
		if(cmd.hasOption("OP_spec_taxids")){
			speciesQuery=true;
			taxids=true;
		}
		
		if(cmd.hasOption("OP_node"))node=true;
		if(cmd.hasOption("OP_spec"))speciesQuery=true;
		node_rank = this.getOption(cmd, "OP_node", "phylum");
		output = this.getOption(cmd, "o", null);
		blastRunID = this.getOption(cmd, "rb", -1);
		hit_no = this.getOption(cmd, "no_hit", -1);
		evalue = this.getOption(cmd, "evalue", -1.0);	
		localdb = this.getOption(cmd, "ld", "nr");
		if(cmd.hasOption("nd")){
			ncbidb = NCBI_DATABASE.valueOf(cmd.getOptionValue("nd").trim());
			if(ncbidb == null){
				logger.error(cmd.getOptionValue("n") + " is not a valid database");
				StringBuffer b = new StringBuffer();
				for(NCBI_DATABASE v : NCBI_DATABASE.values())b.append(v+", ");
				logger.info("Select from: "+b.toString());
			}
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("nd", "ncbidb", true,  "Name of nbci database (default protein)"));
		options.addOption(new Option("ld", "localdb", true,  "Name of as used in local biosql db (default nr)"));
		options.addOption(new Option("OP_ua", "updateAcc", false,  "Update ncbi accessions with taxonomy information"));
		options.addOption(new Option("OP_up", "updateParents", false,  "Update taxonomy with parent information"));
		options.addOption(new Option("OP_de", "depthTravel", false,  "Run this after all updates to regenerate depth map"));
		options.addOption(new Option("OP_node", "nodeStats", true,  "Run statistics for node_rank, ie -OP_node phylum will output phylum count"));
		options.addOption(new Option("OP_spec", "speciesQuery", false,  "Output csv files containing species counts"));
		options.addOption(new Option("OP_spc_taxids", "speciesQueryTaxid", false,  "Output op_spec with taxon ids not species name"));
		options.addOption(new Option("rb", "blastRunId", true,  "Run id for blast for -nd"));
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
					if(manager.getBioSQLXT().resetDepth(manager)){
						if(!manager.getBioSQLXT().depthTraversalTaxon(manager, Tools_NCBI.ncbi_root_taxon)){
							logger.error("An error occured trying to add depth");
						}
					}
					else logger.error("Failed to reset depth");
				}
			} catch (Exception e) {
				logger.error("An error occured trying to run depth traversal", e);
			}
		}
		if(node){
			try {
				DatabaseManager manager = this.ui.getDatabaseManager(password);
				if(blastRunID < 1){
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
					int n[][] = manager.getBioSQLXT().getTaxonPerAssembly(manager, m, blastRunID, evalue, hit_no);
					System.out.println("Outputting counts:");
					StringBuffer b = new StringBuffer();
					for(int i =0;i < m.length;i++){
						if(this.output == null){
							System.out.println(map.get(n[0][i])+" " + n[1][i]);
						}
						else b.append(map.get(n[0][i])+" " + n[1][i]+Tools_System.getNewline());
					}
					if(this.output != null){
						if(new File(this.output).isDirectory()){
							Tools_File.quickWrite(b.toString(), new File(this.output+"out.dat"), false);
						}
						else Tools_File.quickWrite(b.toString(), new File(this.output), false);
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
					if(blastRunID > 0){
						if(manager.getBioSQLXT().runSpeciesQuery(manager,f, blastRunID, evalue, hit_no, taxids)){
							logger.info("Appears to have succesfully run, output at " +f.getPath());
						}
						else logger.error("Error returned, please check logs");
					}
					else{
						int[] blasts = manager.getBioSQLXT().getRunId(manager, null, Run.RUNTYPE_blast);

						for(int i=0;i < blasts.length; i++){
							Run run = manager.getBioSQLXT().getRun(manager, blasts[i]);
							Run parent = manager.getBioSQLXT().getRun(manager, run.getParent_id());
							String filename = parent.getProgram().replaceAll(" ", "_") +"_"; 
							filename += (parent.getSource().indexOf(" ") != 1) ? parent.getSource().split(" ")[0] : parent.getSource();
							f = new File(FilenameUtils.getFullPath(output)+filename+".csv");
							if(manager.getBioSQLXT().runSpeciesQuery(manager,f, blasts[i], evalue, hit_no, taxids)){
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
	
	public void printHelpMessage(){
		Tools_CLI.printHelpMessage(getHelpHeader(), "-- Share And Enjoy! --", this.options);
		System.out.println("Database upload operations are designed to be run in a sequence:");
		System.out.println("1) -OP_ua   Updates all accessions " +
				"with ncbi tax-ids");
		System.out.println("2) -OP_up   Gets the taxonomy data " +
				"for hierarchical groups such as order, clade phylum etc...");
		System.out.println("3) -OP_de   Creates a new depth map " +
				"(Needed after each update) which allows hierarchichal selection");
	}

}
