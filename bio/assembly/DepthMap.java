package bio.assembly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tools.Tools_System;

public class DepthMap implements ACEHandler{

	HashMap<String, String> sequences;
	HashMap<String, int[]> depths;
	private String currentcontig;
	private long totalbp;
	private HashMap<String, Integer> tempos;
	
	
	//TODO still brokened
	public DepthMap(){
		sequences = new HashMap<String, String>();
		depths = new HashMap<String, int[]>();
		tempos = new HashMap<String, Integer>();
		
	}

	public void setRefName(String name) {
		this.sequences.put(name, null);
		currentcontig = name;
		tempos = new HashMap<String, Integer>();
		System.out.println("DELETEALL:"+name);
	}

	public String getRefName(int i) {
		String name = null;
		for(String s : sequences.keySet()){
			if(i == 0)name=s;
			i--;
		}
		return name;
	}

	public void setRefConsensus(String buffer) {
		this.sequences.put(currentcontig, buffer);
		this.depths.put(currentcontig, new int[buffer.length()]);
	}

	public String getRefConsensus(String refname) {
		return this.sequences.get(refname).toString();
	}

	public void setRefConsensusQuality(String buffer) {
	}

	public String getRefConsensusQuality(String refname) {
		return null;
	}

	public int getRefLength(String refname) {
		return this.sequences.get(refname).length();
	}

	public void setNoOfBases(int i) {	
	}

	public void setNoOfReads(int i) {
	}

	public void setBaseSegments(int i) {
	}

	public void setOrientation(char orient) {
	}

	public String addQNAME(String name) {
		return null;
	}

	public void addSEQ(String sequence, String qname) {
		int[] data = this.depths.get(currentcontig);
		for(int i =0; i < sequence.length(); i++){
			if( sequence.charAt(i) != '-' && sequence.charAt(i) != '*'){
				totalbp++;
				int temp = tempos.get(qname);
				System.out.println("GETTING:"+qname);
				if(temp+i < data.length && temp+i> -1){
					data[temp+i]++;
				}
			}
		}
		this.depths.put(currentcontig, data);
	}

	public void addPOS(int start, String qname) {
		this.tempos.put(qname, start);
		System.out.println("ADDED:"+qname);
	}

	public void addOrientation(char orient, String qname) {
	}

	public void addRange(int start, int end, String qname) {

	}

	public void addRangePadded(int start, int end, String qname) {
	}
	
	public boolean dumpData(File file){
		try{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			
			StringBuilder build = new StringBuilder();
			for(String s : sequences.keySet()){
				build.append(s +": LEN="+sequences.get(s).length());
				build.append(Tools_System.getNewline());
				build.append(sequences.get(s));
				build.append(Tools_System.getNewline());
				int[] deps = this.depths.get(s);
				for(int i = 0; i < deps.length; i++){
					build.append(deps[i]+", ");
				}
				build.append(Tools_System.getNewline());
				out.write(build.toString());
				out.flush();
				build= new StringBuilder();
			}
			out.close();
			return true;
		}
		catch(IOException io){
			Logger.getRootLogger().error("DATA Dump IO exception", io);
			return false;
		}
	}
	
	public long getTotalBp(){
		return this.totalbp;
	}
	
}
