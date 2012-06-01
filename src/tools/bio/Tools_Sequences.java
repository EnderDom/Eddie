package tools.bio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import databases.manager.DatabaseManager;

import bio.fasta.Fasta;
import bio.fasta.FastaParser;
import bio.sequence.FourBitSequence;

import tools.Tools_Math;
import ui.UI;

public class Tools_Sequences {
	
	/**
	 * Source code taken from 
	 * 
	 * http://asap.ahabs.wisc.edu/mauve/mauve-developer-guide/mauve-development-overview.html
	 * 
	 * @see src/org/gel/mauve/assembly/AssemblyScorer.java
	 * 
	 * which appears to be under the GPL (v3) licence 
	 * 
	 * Returns stats for an array of lengths
	 * presumably from a fasta or assembly file
	 * [0] Sum of Lengths (no of bp)
	 * [1] min length
	 * [2] max length
	 * [3] n50
	 * [4] n90
	 * [5] seqs > 500bp
	 * [6] seqs > 1Kb
	 */
	public static long[] SequenceStats(int[] arr_of_lengths){
		Arrays.sort(arr_of_lengths);
		long[] ret = new long[7];
		for(int k : arr_of_lengths)ret[0]+=k;
		int i=0;
		ret[1] = arr_of_lengths[0];
		ret[2] = arr_of_lengths[arr_of_lengths.length-1];
		long cur = 0;
		for(i=arr_of_lengths.length-1; i>=0 && cur*2 < ret[0]; i--){
			cur += arr_of_lengths[i];
		}
		ret[3]= arr_of_lengths[i];
		for(; i>0 && cur < ret[0]*0.9d;){
			cur += arr_of_lengths[--i];
		}
		ret[4] = arr_of_lengths[i];
		
		for(i =0; i < arr_of_lengths.length; i++){
			if(arr_of_lengths[i] > 500){
				ret[5]++;
			}
			if(arr_of_lengths[i] > 1000){
				ret[6]++;
			}
		}
		return ret;
	}
	
	public static int n50(int[] arr_of_lengths){
		Arrays.sort(arr_of_lengths);
		long sum = Tools_Math.sum(arr_of_lengths);
		long cur = 0;
		int i=0;
		for(i=arr_of_lengths.length-1; i>=0 && cur*2 < sum; i--){
			cur += arr_of_lengths[i];
		}
		return arr_of_lengths[i];
	}
	
	
	public static FourBitSequence getSequenceFromSomewhere(Logger logger, UI ui, String input, String contig, int index, boolean fuzzy){
		if(input != null){
			String file = Tools_Bio_File.detectFileType(input);
			if(file.contains("FAST")){
				File in = new File(input);
				if(in.isFile()){
					Fasta fasta = new Fasta();
					FastaParser parser = new FastaParser(fasta);
					try{
						logger.debug("Parsing fast(a/q) file....");
						if(file.contentEquals("FASTQ"))parser.parseFastq(in);
						else parser.parseFasta(in);
						if(fasta.size() == 1)index=0;
						if(index != -1){
							String seq = fasta.getSequence(index);
							if(seq != null){
								return new FourBitSequence(seq);
							}
							else{
								logger.error("Failure to retrieve data which should be retrievable");
							}
						}
						else if(contig != null){
							if(fasta.hasSequence(contig)){
								return new FourBitSequence(fasta.getSequence(contig));
							}
							else if(fuzzy){
								String[] names = Tools_Contig.stripContig(contig);
								int i =0;
								while(!fasta.hasSequence(names[i]) && i < names.length)i++;
								if(fasta.hasSequence(names[i])){
									logger.info("Retrieved sequence " + names[i] + " from fasta file");
									return new FourBitSequence(fasta.getSequence(names[i]));
								}
								else{
									logger.error("Could not get " + contig +" from fasta");
								}
							}
						}
						else{
							logger.error("fasta has multiple sequences, but non specified, specifiy with -c or -index");
						}
					}
					catch(IOException io){
						logger.error(io);
					}
				}
				else{
					logger.error("Failure, Input is not a file");
					return null;
				}
			}
			else{
				logger.error("Only fast(a/q) input supported");
				return null;
			}
		}
		else if(contig != null){
			DatabaseManager manager = new DatabaseManager(ui);
			manager.open();
			int bioentry = manager.getBioSQL().getBioEntry(manager.getCon(), contig, contig, manager.getEddieDBID());
			if(bioentry < 1 && fuzzy){
				logger.info("Could not find "+ contig + " looking with fuzzy names");
				bioentry = manager.getBioSQLXT().getBioEntryId(manager.getBioSQL(),manager.getCon(), contig, fuzzy, manager.getEddieDBID());
			}
			if(bioentry > 0){
				String seq = manager.getBioSQL().getSequence(manager.getCon(), bioentry);
				manager.close();
				return new FourBitSequence(seq);
			}
			else{
				logger.warn("Failed to retrieve sequence information for "+contig);
			}
		}
		return null;
	}
}
