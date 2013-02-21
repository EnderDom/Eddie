package enderdom.eddie.tasks.bio;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import uk.ac.ebi.webservices.axis1.IPRScanClient;

public class Task_WebInterPro extends TaskXT{

	IPRScanClient client;
	CommandLine cli;
	
	public void buildOptions(){
		options = new Options();
		options.addOption(new Option("opts", false, "Help Menu for this specific task"));
		options.addOption("email", "email", true, "Your email address");
		options.addOption("stdout", "stdout", false, "print to standard output");
		options.addOption("outfile", "output", true, "file name to save the results");
		options.addOption("outformat", "outformat", true, "Output format (txt or xml)");
		options.addOption("params", "params", false, "List parameters");
		options.addOption("endpoint", "endpoint", true, "Service endpoint URL");
		options.addOption("multifasta", "multifasta", false, "Multiple fasta sequence input");
        options.addOption("appl", "appl", true, "Signature methods");
        options.addOption("app", "app", true, "Signature methods");
        options.addOption("crc", "crc", false, "Enable CRC");
        options.addOption("nocrc", "nocrc", false, "Disable CRC");
        options.addOption("goterms", "goterms", false, "Enable GO terms");
        options.addOption("nogoterms", "nogoterms", false, "Disable GO terms");
        options.addOption("sequence", true, "sequence file (fasta only) or datbase entry database:acc.no");
	}
	
	public void parseArgsSub(CommandLine commandline){
		this.cli = commandline;
	}
	
	/**
	 * Slimmed down version of the IPRScanClient main method
	 */
	public void runAlt(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		client= new IPRScanClient();
		try {
		    if(cli.hasOption("endpoint"))client.setServiceEndPoint(cli.getOptionValue("endpoint"));
	        if(cli.hasOption("params")){
				client.printParams();
	        }
	        else{
	            if(cli.hasOption("email") && (cli.hasOption("sequence") || cli.getArgs().length > 0)){
	               String dataOption = (cli.hasOption("sequence")) ? cli
							.getOptionValue("sequence") : cli.getArgs()[0];
					// Multi-fasta sequence input.
					if (cli.hasOption("multifasta")) {
						client.printDebugMessage("main", "Mode: multifasta", 11);
						int numSeq = 0;
						client.setFastaInputFile(dataOption);
						// Loop over input sequences, submitting each one.
						String fastaSeq = null;
						fastaSeq = client.nextFastaSequence();
						client.printDebugMessage("main", "fastaSeq: " + fastaSeq,12);
						while (fastaSeq != null) {
							numSeq++;
							client.submitJobFromCli(cli, fastaSeq, numSeq);
							fastaSeq = client.nextFastaSequence();
						}
						client.closeFastaFile();
						client.printProgressMessage("Processed " + numSeq
								+ " input sequences", 2);
					}
					// Entry identifier list.
					else if (dataOption.startsWith("@")) {
						client.printDebugMessage("main", "Mode: Id list", 11);
						int numId = 0;
						client.setIdentifierListFile(dataOption.substring(1));
						// Loop over input sequences, submitting each one.
						String id = null;
						id = client.nextIdentifier();
						while (id != null) {
							numId++;
							client.printProgressMessage("ID: " + id, 1);
							client.submitJobFromCli(cli, id, numId);
							id = client.nextIdentifier();
						}
						client.closeIdentifierListFile();
						client.printProgressMessage("Processed " + numId
								+ " input identifiers", 2);
					}
					// Submit a job
					else {
						client.printDebugMessage("main", "Mode: sequence", 11);
						client.submitJobFromCli(cli, new String(client.loadData(dataOption)),0);
					}
	            } 
	            else{
	            	logger.error("Not enough input variables for Interpro");
	            	logger.warn("Note! This Task uses '-sequences' as the input tag, not -'i'");
	            	logger.warn("Note! Tag multifasta must be used if multifasta");
	            }
	        }
        }
        catch(Exception e){
            logger.error("Error running/parsing the file for Interpro",e);
        }
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(this.cli.hasOption("sequence")){
			try {
				client= new IPRScanClient();
				SequenceList list = SequenceListFactory.getSequenceList(this.cli.getOptionValue("sequence"));
				int c=0;
				while(list.hasNext()){
					SequenceObject o = list.next();
					client.submitJobFromCli(cli, o.getSequence(), c, o.getName());
					c++;
				}
			} 
			catch (FileNotFoundException e) {
				logger.error(e);
			} catch (UnsupportedTypeException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} catch (ServiceException e) {
				logger.error(e);
			}
			
		}
		else{
			logger.error("No sequence file added with -sequence");
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
}
