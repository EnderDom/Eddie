package bio.xml;

import java.io.File;

import databases.manager.DatabaseManager;

import tools.Tools_String;

public class XMLHelper_Blastx {

	public XML_Blastx blastx;
	public DatabaseManager manager;
	private int contig_id;
	
	/*
	 * Laziness :)
	 */
	
	public XMLHelper_Blastx(File file) throws Exception{
		new XML_Blastx(file);
		this.contig_id = -1;
	}
	
	public XMLHelper_Blastx(XML_Blastx blastx){
		this.blastx = blastx;
		this.contig_id = -1;
	}
	
	public XML_Blastx getBlastXML(){
		return this.blastx;
	}

	public String getBlastProgram(){
		return blastx.getBlastTagContents("BlastOutput_program");
	}
	
	public String getBlastDatabase(){
		return blastx.getBlastTagContents("BlastOutput_db");
	}

	public String getHitAccession(int index) throws Exception{
		return blastx.getHitTagContents("Hit_accession", index);
	}
	
	public double getHspEvalue(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_evalue", hit_num, hsp_num);
		Double d = Tools_String.parseString2Double(s);
		if(d != null)return d;
		else throw new Exception("An error occured attempting to parse the hsp evalue " +s );
	}
	
	public int getHspScore(int hit_num, int hsp_num)throws Exception{
		String s = blastx.getHspTagContents("Hsp_evalue", hit_num, hsp_num);
		Integer d = Tools_String.parseString2Int(s);
		if(d != null)return d;
		else throw new Exception("An error occured attempting to parse the hsp score " +s );
	}
	
	//Order is Hit-Start, Hit-Stop, Hit-Frame, Query-Start, Query-Stop, Query-Frame
	public int[] getStartsStopsFrames(int hit_num, int hsp_num) throws Exception{
		int[] arr = new int[6];
		String[] tags = new String[]{"Hsp_hit-from", "Hsp_hit-to","Hsp_hit-frame",
				"Hsp_query-from", "Hsp_query-to","Hsp_query-frame"};
		for(int j =0; j < tags.length; j++){
			String s = blastx.getHspTagContents(tags[j], hit_num, hsp_num);
			Integer i = Tools_String.parseString2Int(s);
			if(i != null)arr[j]=i;
			else throw new Exception("Failed to parse tag " + tags[i] + " to integer, tag value: " +s);
		}
		return arr;
	}

	public int getContig_id() {
		return contig_id;
	}

	public void setContig_id(int contig_id) {
		this.contig_id = contig_id;
	}
	
	public boolean upload2BioSQL(){
		if(contig_id == -1){
			
		}
		
		return false;
	}
}
