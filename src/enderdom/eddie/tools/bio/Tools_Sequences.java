package enderdom.eddie.tools.bio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.lists.FastaParser;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.GenericSequence;
import enderdom.eddie.bio.sequence.SequenceObject;

import enderdom.eddie.tools.Tools_Math;
import enderdom.eddie.tools.Tools_String;

public class Tools_Sequences {
	
	public static int UNKNOWN = -1;
	public static int DNA = 0;
	public static int RNA = 1;
	public static int PROTEIN = 2;
	public static int OTHER = 3;
	
	//AA codes
	//A,B,C,D,E,F,G,H,I,J,K,L,M,N,P,Q,R,S,T,V,W,X,Y,z
	//Nucleic
	//A,B,C,D,    G,H,I,   K,  M,N,    R,S,T,V,W,Y

	
	public static int detectSequence(SequenceObject o){
		String exc_aas[] = new String[]{"E","F","J","L","P","Q","Z"};
		for(int i =0; i < exc_aas.length; i++){
			if(o.getSequence().contains(exc_aas[i])){
				if(!o.hasQuality())return PROTEIN;
				else return UNKNOWN;
			}
		}
		if(o.getSequence().contains("U")){
			if(!o.getSequence().contains("T"))return RNA;
			else return UNKNOWN;
		}
		if(Tools_String.containsDigits(o.getSequence()))return UNKNOWN;
		else return DNA;
	}
	
	
	/**
	 * Source code taken from 
	 * 
	 * http://asap.ahabs.wisc.edu/mauve/mauve-developer-guide/mauve-development-overview.html
	 * 
	 * @see src/org/gel/mauve/assembly/AssemblyScorer.java
	 * 
	 * which appears to be under the GPL (v3) licence 
	 * 
	 * @param arr_of_lengths  and array of integers, the length
	 * of the array can be anything, this should be the lengths
	 * of all the sequences within a set of sequences.
	 * 
	 * @return Returns stats for an array of lengths
	 * presumably from a fasta or assembly file
	 * [0] Sum of Lengths (no of bp)
	 * [1] min length
	 * [2] max length
	 * [3] n50
	 * [4] n90
	 * [5] seqs > 500bp
	 * [6] seqs > 1Kb
	 * 
	 * 
	 * 
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
	
	/**
	 * @see SequenceStats() in this class
	 * @param arr_of_lengths 
	 * @return the n50 for this array of integers
	 */
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
	
	
	/*
	 * The number of errors is insane here,
	 * I may need to look into some sort of
	 * error codes... hmmm... However the level
	 * of abstraction may then get insane itself.
	*/
	/**
	 * This takes either a Contig name
	 * and either extracts the sequence based on the 
	 * name from a fasta, or from the database
	 * 
	 * 
	 * 
	 * @param logger
	 * @param manager DatabaseManager, won't actually be used if
	 * this is not a database query, should look into a better
	 * way of potentially doing this but I'm lazy
	 * @param input  should be a filepath
	 * @param contig  name of a sequence in the fasta or database
	 * @param index   if index is set, rather than getting a specific named sequence
	 * from the multifasta, it retrieves the index, with 0 being the first sequence
	 * @param fuzzy   set this to have a certain to use slight naming
	 *  variations, this is mainly for my personal convinience
	 * @param strip  boolean, set to true if you want all the '*'/'-' removed
	 * @return A SequenceObject object using the input
	 */
	public static SequenceObject getSequenceFromSomewhere(Logger logger, DatabaseManager manager, String input, String contig, int index, boolean fuzzy, boolean strip){
		SequenceObject seq = null;
		if(input != null){
			BioFileType type = Tools_Bio_File.detectFileType(input);
			if(type == BioFileType.FASTA || type == BioFileType.FASTQ){
				File in = new File(input);
				if(in.isFile()){
					Fasta fasta = new Fasta();
					FastaParser parser = new FastaParser(fasta);
					try{
						logger.debug("Parsing fast(a/q) file....");
						if(type == BioFileType.FASTA)parser.parseFastq(in);
						else parser.parseFasta(in);
						if(fasta.size() == 1)index=0;
						if(index != -1){
							seq = fasta.getSequence(index);
							if(seq == null)logger.error("Failure to retrieve data which should be retrievable");
						}
						else if(contig != null){
							if(fasta.hasSequence(contig)){
								seq = fasta.getSequence(contig);
							}
							else if(fuzzy){
								String[] s = fasta.getTruncatedNames();
								for(int j =0; j < s.length; j++){
									if(s.equals(contig)){
										seq= fasta.getSequence(j);
										logger.info(contig + " matched to the fasta " + fasta.getSequenceName(j));
										break;
									}
								}
								
								String[] names = Tools_Contig.stripContig(contig);
								if(names != null){
									for(int i =0; i < s.length; i++){
										for(int j =0; j < names.length; j++){
											if(s[i].toLowerCase().equals(names[j].toLowerCase())){
												logger.info(contig+" was retrieved as \""+fasta.getSequenceName(i)+"\"");
												seq = fasta.getSequence(i);
												break;
											}
										}
									}
								}
								if(seq == null){
									logger.error("Incomprehensible gibberish error: " + contig + " is not a real thing");
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
			logger.debug("Retrieving id for "+contig+" from database");
			try{
				manager.open();
				logger.debug("Database connection open...");
				int bioentry = manager.getBioSQLXT().getBioEntryId(manager, contig, fuzzy, manager.getEddieDBID(), -1);
				if(bioentry > 0){
					seq = new GenericSequence(manager.getBioSQL().getSequence(manager.getCon(), bioentry));				
					manager.close();
				}
				else if(bioentry < 1 && !fuzzy){
					logger.warn("Failed to retrieve sequence information for "+contig + " try using fuzzy (-f)");
				}
				else{
					logger.warn("Failed to retrieve sequence information for "+contig);
				}
			}
			catch(Exception e){
				logger.error("Failed to establish Database Connection", e);
			}
		}
		else{
			logger.error("No contig or input set");
		}
		if(seq==null){
			return null;
		}
		else{
			return seq;
		}
	}
}
