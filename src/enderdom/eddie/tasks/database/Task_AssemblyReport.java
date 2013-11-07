package enderdom.eddie.tasks.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.databases.bioSQL.psuedoORM.Dbxref;
import enderdom.eddie.databases.bioSQL.psuedoORM.Dbxref_Bioentry_Link;
import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.exceptions.EddieDBException;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;
import enderdom.eddie.tools.bio.Tools_TAIR;

public class Task_AssemblyReport extends TaskXT{

	private int run_id;
	private int ncross;
	private String output;
	
	public Task_AssemblyReport(){
		setHelpHeader("--This is the Help Message for the AssemblyReport Task--");
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		run_id = getOption(cmd, "r", -1);
		ncross = getOption(cmd, "n", 3);
		output = getOption(cmd, "o", null);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("n", "ncross", true, "Number to cross section"));
		options.addOption(new Option("r","run_id", true, "run id for the meta assembly"));
		options.addOption(new Option("o","output", true, "Output folder, will save html report and related files within folder"));
	}
	
	public void run(){
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		setCompleteState(TaskState.STARTED);

		String p = Tools_System.getFilepathSeparator();
		File f = new File(output);
		if(f.exists() && !f.isDirectory()){
			logger.error("output file already exists and is not a directory");
			return;
		}
		else f.mkdirs();
		
		String reportfile = f.getPath()+p+"report.html";
		
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		LinkedList<Integer> programindexstart = new LinkedList<Integer>();
		String currentprogram="";
		try{
			FileWriter writer = new FileWriter(reportfile);
			BufferedWriter out = new BufferedWriter(writer);
			
			try {
				if(manager.open()){
					out.write("<h1>Assembly Report</h1>");
					out.write("<head/><body>");
					//Lots of debug logs because this array maths is making my brain hurt
					logger.debug("Generating list of contigs attached to meta assembly run_id " + run_id);
					
					Run assembly = manager.getBioSQLXT().getRun(manager, run_id);
					out.write("<hr/>");
					out.write("<p><b>Assembler Program:</b> "+assembly.getProgram()+"</p>");
					out.write("<p><b>Date:</b> "+assembly.getDateValue("dd-MM-YYYY")+"</p>");
					out.write("<p><b>Source:</b> "+assembly.getSource()+"</p>");
					out.write("<hr/>");
					
					String[][] contigs = manager.getBioSQLXT().getListOfContigsfromMetaAssembly(manager, run_id);
					for(int i =0; i < contigs[0].length;i++){
						if(!contigs[3][i].equals(currentprogram)){
							programindexstart.add(i);
							currentprogram = contigs[3][i];
							logger.debug(currentprogram + " assembly program identified beginning at index " + programindexstart.getLast());
						}
					}
					programindexstart.add(contigs[0].length-1);
					for(int i = 1 ; i< programindexstart.size();i++){
						logger.debug("Assessing program " + contigs[3][programindexstart.get(i-1)]);
						int[] indices = new int[ncross];
						int delta = programindexstart.get(i)-programindexstart.get(i-1);
						logger.debug("Sub array of contigs between "+(programindexstart.get(i-1)) 
								+ " and " + (programindexstart.get(i)) +" (delta: "+delta+")"  );
						for(int j = 0; j < indices.length;j++){
							indices[j]= programindexstart.get(i-1)+(delta/(ncross-1))*j;
							logger.debug("Contig at index " + indices[j] + " called "+contigs[1][indices[j]]
							+" selected and stored  " + j);
							
						}
						indices[ncross-1]=programindexstart.get(i-1)+delta-1;
						logger.debug(ncross-1+" store reset to last contig (compensating for remainder drift) at "
								+indices[ncross-1] + " called " +contigs[1][indices[ncross-1]]);
						for(int j =0;j< indices.length;j++){
							out.write("<h3>Contig: "+contigs[1][indices[j]]+"</h4>");
							out.write("<p>Length: "+contigs[2][indices[j]]+"<p>");
							out.write("<p>Assembler: "+contigs[3][indices[j]]+"<p> </br></br>");
							//TODO get all overlapping contigs
							Dbxref_Bioentry_Link[] links = manager.getBioSQLXT().getDbxRef_Bioentry_Links(
									manager.getCon(), Tools_String.parseString2Int(contigs[0][indices[j]]), -1);
							for(Dbxref_Bioentry_Link link : links){
								if(link.getHit_no() == 1 && link.getRank() == 1){
								
									Dbxref d = manager.getBioSQLXT().getDbxRef(manager.getCon(), link.getDbxref_id());
									out.write("<p>Top Match: " + d.getAccession() + " ("+d.getDbname()+")</p>");
									out.write("<p>Evalue: " + link.getEvalue() + " ("+d.getDbname()+")</p>");
									out.write("<p>Score: " + link.getScore() + " ("+d.getDbname()+")</p>");
									String info = "<p>Protein Name: No info available</p>";
									if(d.getDbname().equalsIgnoreCase("refseq")){
										try {
											SequenceObject o = Tools_NCBI.getSequencewAcc(NCBI_DATABASE.protein, d.getAccession());
											info = "<p>Protein Name:"+ o.getIdentifier() + "</p> ";
										} catch (Exception e) {
											logger.error("Failed to acquire protein name from accession " + d.getAccession(),e);
										}
									}
									else if(d.getDbname().equalsIgnoreCase("TAIR")){
										String acc = d.getAccession();
										int s = acc.lastIndexOf(".");
										if(s != -1)acc=acc.substring(0, s);
										String tairinfo = Tools_TAIR.getGeneDescription(acc);
										System.out.println(tairinfo);
										if(tairinfo != null && tairinfo.contains(Tools_System.getNewline())){
											String[] splis = tairinfo.split(Tools_System.getNewline());
											info ="<p>";
											for(int k =1;k< splis.length;k++)info+=splis[k]+ " " ;
											info+="</p>";
										}
										else if(tairinfo != null)info = "<p>"+tairinfo+"</p>";
										else{
											logger.error("Failed to access TAIR information from the database");	
										}
									}
									else{
										logger.error("No support for lookup of the " + d.getDbname() + " database currently");
									}
									out.write(info);
								}
							}
							out.write("</br></hr>");
						}
					}
					
						
					out.write("</body>");
					out.close();
				}
				else logger.error("Failed to open database"); 
				
			} catch (InstantiationException e) {
				logger.error("Failed to open database", e);
			} catch (IllegalAccessException e) {
				logger.error("Failed to open database", e);
			} catch (ClassNotFoundException e) {
				logger.error("Failed to open database", e);
			} catch (SQLException e) {
				logger.error("Failed to open database", e);
			} catch (EddieDBException e) {
				logger.error("Failed to open database", e);
			} catch (InterruptedException e) {
				logger.error("Failed to open database", e);
			}
			
		}
		catch(IOException io){
			logger.error("Failed to open and write to file "+reportfile, io);
		}
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
}
