package enderdom.eddie.bio.fasta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.bio.Tools_Fasta;

public class FastaParser{

	FastaHandler handler;
	boolean fastq;
	private boolean shorttitles;
	
	/*
	 * Eddie v3 class
	 */
			
	public FastaParser(){
		shorttitles =false;
	}
	public FastaParser(FastaHandler handler){
		shorttitles = false;
		this.handler = handler;
	}
	
	public int parseFastq(File fasta) throws IOException{
		int count = 0;
		int linecount = 0;
		int multi =0;
		FileInputStream fis = new FileInputStream(fasta);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		
		String line = null;
		StringBuilder sequence = new StringBuilder();
		StringBuilder quality = new StringBuilder();
		String title = "";
		boolean qual = false;
		while ((line = reader.readLine()) != null){
			if(line.startsWith("@") && !qual){
				if(sequence.length() > 0){
					count++;				
					handler.addAll(title, sequence.toString(), quality.toString());
					sequence = new StringBuilder();
					quality = new StringBuilder();
				}
				if(this.shorttitles && line.indexOf(" ") != 0){
					title = line.substring(1, line.indexOf(" "));
				}
				else{
					title= line.substring(1, line.length());
				}
			}
			else if(line.startsWith("+") && !qual){
				if(!this.fastq){
					this.fastq=true;handler.setFastq(this.fastq);
				}
				qual = true;
			}
			else{
				if(!qual){
					sequence = sequence.append(line);
				}
				else{
					quality = quality.append(line);
					if(quality.length() >= sequence.length()){
						qual = false;
						if(quality.length() != sequence.length()){
							Logger.getRootLogger().debug("Name: " + title);
							Logger.getRootLogger().trace("Sequence: " + Tools_String.splitintolines(Tools_String.fastadefaultlength, sequence.toString()));
							Logger.getRootLogger().trace("Quality: " + Tools_String.splitintolines(Tools_String.fastadefaultlength, quality.toString()));
							Logger.getRootLogger().warn("Quality out of sync by " + (quality.length()-sequence.length()));
							throw new IOException("Quality too long!");
						}
					}
				}
			}
			if(multi==5000){
				multi=0;
				System.out.print("\rParsing Line: "+linecount);
			}
			multi++;
			linecount++;
		}
		System.out.println();
		if(sequence.length() > 0){
			count++;
			handler.addAll(title, sequence.toString(), quality.toString());
		}
		return count;
	}
	
	
	/*
	 * This method might be borked, //TODO fix
	 * 
	 */
	public int parseFastawQual(File fasta, File qual)  throws IOException{
		Logger.getRootLogger().error("Using Borked Method!!");
		int count = 0;
		int count2=0;
		int linecount=0;
		int multi=0;
		FileInputStream fis = new FileInputStream(fasta);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		
		FileInputStream fis2 = new FileInputStream(qual);
		InputStreamReader in2 = new InputStreamReader(fis2, "UTF-8");
		BufferedReader reader2 = new BufferedReader(in2);
		
		String line = "";
		StringBuilder sequence = new StringBuilder();
		StringBuilder quality = new StringBuilder();
		String title = "";
		String qualtitle = "";
		while(line != null){
			while ((line = reader.readLine()) != null){
				System.out.println(line);
				if(line.startsWith(">")){
					if(sequence.length() > 0){
						handler.addSequence(title, sequence.toString());
						sequence = new StringBuilder();
						count++;
					}
					if(this.shorttitles && line.indexOf(" ") != 0){
						title = line.substring(1, line.indexOf(" "));
					}
					else{
						title= line.substring(1, line.length());
					}
				}
				else{
					sequence = sequence.append(line);
				}
			}
			while ((line = reader2.readLine()) != null){
				if(line.startsWith(">")){
					if(quality.length() > 0){
						handler.addQuality(qualtitle, Tools_Fasta.Qual2Fastq(quality.toString()));
						sequence = new StringBuilder();
						count2++;
					}
					if(this.shorttitles && line.indexOf(" ") != 0){
						qualtitle = line.substring(1, line.indexOf(" "));
					}
					else{
						qualtitle= line.substring(1, line.length());
					}
				}
				else{
					quality =quality.append(line);
				}
			}
			if(multi==5000){
				multi=0;
				System.out.print("\rParsing Line: "+linecount);
			}
			multi++;
			linecount++;
		}
		System.out.println();
		if(sequence.length() > 0){
			handler.addSequence(title, sequence.toString());
			count++;
		}
		if(quality.length() > 0){
			handler.addQuality(title, quality.toString());
			count2++;
		}
	
		if(count!=count2){
			throw new IOException("Number of fasta sequence and Number of quality sequences do not match");
		}
		return count;
	}
	
	public int parseFasta(File fasta, boolean qual)  throws IOException{
		int count = 0;
		int linecount=0;
		int multi =0;
		FileInputStream fis = new FileInputStream(fasta);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		
		String line = null;
		StringBuilder sequence = new StringBuilder();
		String title = "";
		while ((line = reader.readLine()) != null){
			if(line.startsWith(">")){
				if(sequence.length() > 0){
					if(!qual)handler.addSequence(title, sequence.toString());
					else handler.addQuality(title, Tools_Fasta.Qual2Fastq(sequence.toString()));
					sequence = new StringBuilder();
					count++;
				}
				if(this.shorttitles && line.indexOf(" ") != 0){
					title = line.substring(1, line.indexOf(" "));
				}
				else{
					title= line.substring(1, line.length());
				}
			}
			else{
				if(!qual)sequence = sequence.append(line);
				else sequence = sequence.append(line + " "); /*Space is important as 
				the quality string is broken into an int array based on spaces.
				Without adding the space the last and first numbers on each line are
				parsed as a single number.
				*/
			}
			if(multi==5000){
				multi=0;
				System.out.print("\rParsing Line: "+linecount);
			}
			multi++;
			linecount++;
		}
		System.out.println();
		if(sequence.length() > 0){
			if(!qual)handler.addSequence(title, sequence.toString());
			else handler.addQuality(title, sequence.toString());
			count++;
		}
		return count;
	}
	
	public int parseFasta(File fasta)  throws IOException{
		return parseFasta(fasta, false);
	}
	
	public int parseQual(File fasta)  throws IOException{
		if(handler.isFastq())handler.setFastq(false);
		return parseFasta(fasta, true);
	}
	
	public boolean isShorttitles() {
		return shorttitles;
	}
	public void setShorttitles(boolean shorttitles) {
		this.shorttitles = shorttitles;
	}
	
	
}
