package enderdom.eddie.tasks.bio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_CLI;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.bio.NCBI_DATABASE;
import enderdom.eddie.tools.bio.Tools_NCBI;

public class Task_AminoAnalysis extends TaskXTwIO {
	
	private String phylum;
	private int start;
	private int stop;
	private SequenceList list;
	private String save;
	private String load;
	
	public Task_AminoAnalysis(){
		setHelpHeader("--This is the Help Message for the the Amino Analysis Task--");

	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("ph","phylum", true, "Phylum to isolate for specific analysis, or use -specify"));
		options.addOption(new Option("sp","specify", true, "List of sequence names to isolate (ie all sequences from one phylum)"));
		options.addOption(new Option("st","start", true, "Start of region to consider, 0-based (0 is the initial bp)"));
		options.addOption(new Option("se","stop", true, "End of region to consider, 0-based (0 is the initial bp)"));
		options.addOption(new Option("saveSpecFile", true, "Save the list of sequences in the given phylum, so you don't have to re-search"));
		options.addOption(new Option("specFile", true, "Input a list of sequences to analyse in comparison to the rest"));
		options.getOption("input").setDescription("Input file, if -specify not used");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		phylum = getOption(cmd, "phylum", null);
		start = getOption(cmd, "start", -1);
		stop = getOption(cmd, "stop", -1);
		save = getOption(cmd, "saveSpecFile", null);
		load = getOption(cmd, "specFile", null);
	}
	
	public void printHelpMessage(){
		Tools_CLI.printHelpMessage(getHelpHeader(), Tools_System.getNewline()+"Analyses clustal file alignment "+
				"and identifies potentially significant unique amino acids linked to a specific phylum"
				+Tools_System.getNewline()+"-- Share And Enjoy! --", this.options);
		System.out.println("Example(1):");
		System.out.println("-task aminoanalysis -i seqs.aln -phylum \"Mollusca\" -saveSpecFile /home/dominic/Desktop/spec.txt");
		System.out.println("Would parse alignment, find mollusca sequences and analyse that group compared with the rest");
		System.out.println("Saving the spec File would mean you could input that next time,");
		System.out.println("the specfiles are just a list of names found in the clustal (Use #for header)");
		System.out.println("Example(2):");
		System.out.println("-task aminoanalysis -i seqs.aln -specFile /home/dominic/Desktop/spec.txt -start 100");
		System.out.println("Same as above, but only analsyes from bp 100 (0-based) to the end");
	}
	

	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(input == null){
			logger.error("No input file specified");
			return;
		}
		if(start > stop){
			logger.error("Start and stop should not be greater in size than stop, you clot");
			return;
		}
		if(phylum == null){
			logger.error("Phylum needs to be set");
			return;
		}
		try {
			//Import clustal
			list = SequenceListFactory.getSequenceList(input);
			//Get list of sequences to in phylum or whatever
			logger.info("Loading Sequences...");
			String[] special = load == null ? getSeqsWithPhyla() : Tools_File.quickRead2Array(new File(load));
			if(special.length ==0){
				logger.error("Failed to load sub group from either file or ncbi");
				return;
			}
			else logger.debug("Comparing "+special.length+ " with others");
			//Save list if requested
			if(save != null)Tools_File.quickWrite(special, new File(save));
			
			//Number of sequences
			int size= list.getNoOfSequences();
			//Total sequence Length
			int lengths = list.getQuickMonomers();
			//Check that the sequences are all the same
			if(lengths%size != 0){
				logger.warn("All sequences in this file are expected to " +
						"be the same size but they're not! This will break.");
			}
			int length = lengths/size;
			if(start == -1)start++;
			if(stop == -1)stop=length;
			StringBuffer outstring = startOutString();
			StringBuffer secondstr = startSecond();
			HashSet<Character> phyla = new HashSet<Character>(22);
			HashMap<Character, Integer> count = new HashMap<Character, Integer>(22);
			HashSet<Character> others = new HashSet<Character>(22);
			if(phylum ==null)phylum="Custom";
			String newline = Tools_System.getNewline();
			startOutString();
			
			logger.info("Running Analysis...");
			int j=start;
			int unmatches=0;
			
			//All worlds of fail
			String[] antilist = list.keySet().toArray(new String[0]);
			ArrayList<String> strs = new ArrayList<String>(Arrays.asList(antilist));
			strs.removeAll(Arrays.asList(special));
			antilist = strs.toArray(new String[0]);
			//

			boolean warned =false;
			boolean skip=false;
			for(; j < stop; j++){
				int numb = 0;
				for(String name : list.keySet()){
					skip=false;
					for(String spec : special){
						//If this sequence is one of the special phyla
						if(spec.equals(name)){
							char c = list.getSequence(name).getSequence().charAt(j);
							phyla.add(c);
							if(count.containsKey(c)){
								count.put(c, count.get(c)+1);
							}
							else count.put(c, 1);
							numb++;
							skip=true;
							break;
						}
					}
					if(!skip){
						for(String anti : antilist){
							if(anti.equals(name)){
								others.add(list.getSequence(name).getSequence().charAt(j));
								break;
							}
						}
					}
				}
				if(numb != special.length && !warned){
					logger.warn("Some sequences in spec file are missing from clustal alignment");
					warned=true;
				}
				int init = phyla.size();
				phyla.removeAll(others);
				if(phyla.size() == init){
					outstring.append(j+"\t[");
					for(Character ch : phyla)outstring.append(ch);
					outstring.append("]\t[");
					for(Character ch : others)outstring.append(ch);
					outstring.append("]"+newline);
					unmatches++;
				}
				if(phyla.size() > 0){
					for(Character c : phyla){
						secondstr.append(j+"\t[");
						secondstr.append(c+"]\t");
						secondstr.append(count.get(c)+ "/" + special.length +" " + phylum+newline);
					}
				}
				others.clear();
				phyla.clear();
				count.clear();
			}
			outstring.append(secondstr);
			logger.info("Analysis complete "+ (j-start) +" residues compared");
			logger.info(unmatches + " residues found to be unique in subgroup");
			if(output != null)Tools_File.quickWrite(outstring.toString(), new File(output), false);
			else{
				System.out.println(outstring);
			}
		}
		catch (Exception e) {
			logger.error("Failed to analyse clustal " + input, e);
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	private StringBuffer startOutString() {
		String newline = Tools_System.getNewline();
		StringBuffer outstring = new StringBuffer();
		outstring.append("--Unique Residue Position Analysis--"+newline+newline);
		outstring.append("Analysis: Are there unique residues in all members of this sub group?"+newline);
		outstring.append(newline);
		outstring.append("Relative Position|\tSubGroup ("+phylum+")|\tNon-Subgroup|"+newline);
		return outstring;
	}
	
	private StringBuffer startSecond() {
		String newline = Tools_System.getNewline();
		StringBuffer outstring = new StringBuffer();
		outstring.append(newline+"--Unique Amino Acid at Position--"+newline+newline);
		outstring.append("Analysis: Are there unique residues in some members of this sub group?"+newline);
		outstring.append(newline);
		outstring.append("Relative Position|\tResidue|\tNo. of Group containing"+newline);
		return outstring;
	}

	private String[] getSeqsWithPhyla() throws Exception{
		ArrayList<String> strs = new ArrayList<String>(list.getNoOfSequences()/10);
		int c =0;
		for(String name : list.keySet()){
			String gi = Tools_NCBI.getNCBIGi(name);
			System.out.print("\r"+c+" of " + list.getNoOfSequences());
			if(gi !=null){
				String[] lines = Tools_NCBI.getLineageFromGI(NCBI_DATABASE.protein, gi);
				for(int i =0; i < lines.length; i++){
					if(lines[i].toLowerCase().trim().equals(phylum.toLowerCase().trim())){
						strs.add(name);
						logger.debug(Tools_System.getNewline()+name + " is found to be " + phylum);
					}
				}
			}
		}
		return strs.toArray(new String[0]);
	}
	
}
