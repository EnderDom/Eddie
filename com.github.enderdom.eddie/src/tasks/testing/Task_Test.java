package tasks.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;

import bio.assembly.ACEFileParser;
import bio.assembly.ACERecord;
import bio.fasta.Fasta;
import bio.fasta.FastaParser;

import tasks.Checklist;
import tools.Tools_Array;
import tools.Tools_Bit;
import tools.Tools_Fun;
import tools.Tools_String;
import tools.Tools_System;
import tools.Tools_Task;
import ui.PropertyLoader;
//import net.sf.samtools.SAMFileHeader;
//import net.sf.samtools.SAMProgramRecord;
//import net.sf.samtools.SAMReadGroupRecord;
//import net.sf.samtools.SAMRecord;
//import net.sf.samtools.SAMFileHeader.SortOrder;

/**
 * Testing class script
 * Holds various Methods for testing different portions of Eddie
 * 
 * @author dominic
 *
 */
public class Task_Test extends Task_Test_Basic{

	
	protected Checklist checklist;

	
	public void runTest(){
		/*
		 * Testing 
		 */
		try{
			System.out.println("Running test");
			task4bitSequence();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	/************************************************************/
	/*															*/
	/*				Testing & Experimental Methods	 			*/
	/*															*/
	/*															*/
	/************************************************************/
	
	
	public void testProcessRunner(){
		 System.out.println("Quick Test of Process Runner");
		 if(Tools_System.isWindows())System.out.println(Tools_Task.runProcess("dir", true)[0].toString());
		 else System.out.println(Tools_Task.runProcess("ls", true)[0].toString());
		 System.out.println();
		 System.out.println();
		 System.out.println("That should be where we're at?");
	}
	
	public void testBitTools(){
		long a = 0x000000000000042FL;
		long b = 0xF000FFFF0000042FL;
		String a1 = "0x000000000000042F";
		String b1 = "0xF000FFFF0000042F";
		String a2 = " 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0011 0010 1111";
		String b2 = " 1111 0000 0000 0000 1111 1111 1111 1111 0000 0000 0000 0000 0000 0011 0010 1111";
		System.out.println("Expected:");
		System.out.println(a1);
		System.out.println(a2);
		System.out.println("Returned:");
		System.out.println(Tools_Bit.LongAsBitString(a));
		System.out.println(Tools_Bit.LongAsHexString(a));
		System.out.println("Expected:");
		System.out.println(b1);
		System.out.println(b2);
		System.out.println("Returned:");
		System.out.println(Tools_Bit.LongAsBitString(b));
		System.out.println(Tools_Bit.LongAsHexString(b));
	}
	
	
	public void task4bitSequence(){

		
	}
	
	public void testChecklist(){
		
	}
	
	public void runSomething(){
		try{
			File file = new File("/home/dominic/Desktop/nc.cgi.html");
			File file2 = new File("/home/dominic/Desktop/ncbiupload.fna");
			
			Fasta fasta = new Fasta();
			FastaParser parser = new FastaParser(fasta);
			parser.parseFasta(file2);
			
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(in);
			String line = "";
			String name = "";
			int start = -1;
			String ot = "";
			while((line = reader.readLine()) != null){
				if((start = line.indexOf("Sequence ID :")) != -1){
					int end = line.indexOf("</strong>");
					if(end != -1 && end > start){
						name = line.substring(start+new String("Sequence ID :").length(), end).trim();
					}
				}
				else if((start = line.indexOf(" match:")) != -1){
					ot = line.substring(start+new String(" match:</a> ").length(), line.length()).trim();
					//System.out.println(name + " : " + ot);
					String[] s = ot.trim().split("-");
					if(s.length > 0){
						Integer sta = Tools_String.parseString2Int(s[0]);
						Integer sto = Tools_String.parseString2Int(s[1]);
						if(sta != null && sto != null){
							if(fasta.hasSequence(name+" [organism=Deroceras reticulatum]" )){
								String seq = fasta.getSequences().get(name+" [organism=Deroceras reticulatum]");
								int halfway = seq.length()/2;
								if(sta > halfway){
									logger.debug(name + " to be trimmed left");
									logger.debug("Trim @"+sta+"-"+sto);
									logger.debug("Length Before " + seq.length());
									String trim = seq.substring(sta-1, seq.length());
									seq = seq.substring(0, sta-1);
									logger.debug("Length After " + seq.length());
									logger.debug("Trimmed Region:" +trim);
								}
								else{
									logger.debug(name + " to be trimmed right");
									logger.debug("Trim @"+sta+"-"+sto);
									logger.debug("Length Before " + seq.length());
									String trim = seq.substring(0, sto-1);
									seq = seq.substring(sto-1, seq.length());
									logger.debug("Length After " + seq.length());
									logger.debug("Trimmed Region:" +trim);
								}
								fasta.getSequences().put(name+" [organism=Deroceras reticulatum]", seq);
							}
							else{
								logger.error("Sequence  "+ name +"does not exist in fasta file");
							}
						}
						else{
							logger.error(ot + " could not parsed into start and stop ");
						}
					}
					else{
						logger.error(ot + " could not parsed into start and stop ");
					}
				}
			}
			fasta.save2Fasta(new File("/home/dominic/Desktop/ncbiupload_trimmed.fna"));
			
		}
		catch(IOException io){
			io.printStackTrace();
		}
	}
	
	
	public void testRot13(){
		String hw = new String("hello my name is dominic.");
		System.out.println(hw);
		hw = Tools_Fun.rot13(hw);
		System.out.println(hw);
		hw = Tools_Fun.rot13(hw);
		System.out.println(hw);
	}
	
	
	public void testSortMethod(){
		int a[] = new int[]{5,5,5,1,2,2,2,2,2,100};
		int b[] = new int[]{1,2,3,4,5,6,7,8,9,10};
		System.out.println("Inputted:");
		for(int i : a)System.out.print(i +",");
		System.out.println();
		for(int i : b)System.out.print(i +",");
		System.out.println();
		System.out.println("Expected:");
		int c[] = new int[]{1,2,2,2,2,2,5,5,5,100};
		int d[] = new int[]{4,5,6,7,8,9,1,2,3,10};
		for(int i : c)System.out.print(i +",");
		System.out.println();
		for(int i : d)System.out.print(i +",");
		System.out.println();
		System.out.println("Observed:");
		Tools_Array.sortBothByFirst(a, b);
		for(int i : a)System.out.print(i +",");
		System.out.println();
		for(int i : b)System.out.print(i +",");
		System.out.println();
	}
	
	
	public void databaseTest(){
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		manager.open();
		BioSQL bs = manager.getBioSQL();
		BioSQLExtended bsxt = manager.getBioSQLXT();
		int i = bsxt.getEddieFromDatabase(manager);
		if(i == -1)bsxt.addEddie2Database(manager);
		i = bsxt.getEddieFromDatabase(manager);
		if(i != -1){
			bs.addSequence(manager.getCon(), i, null, "TEST1",
					"TEST1", null, null, null, 0, "ATGCGACTAG", BioSQL.alphabet_DNA);
		}
		else{
			logger.error("Failed to add Eddie to biodatabase in the database");
		}
		manager.close();
	}
	
	public void testgetUniqueValues(){
		System.out.println("Input:");
		int[] i = new int[]{0,0,1,1,1,1,2,2,4,5,6,7,10,10,11,122,121,11,111,0};
		for(int j : i)System.out.print(j+",");
		System.out.println();
		System.out.println("Expected:");
		int[] k = new int[]{0,1,2,4,5,6,7,10,11,122,121,111};
		for(int j : k)System.out.print(j+",");
		System.out.println();
		System.out.println("Observed:");
		i = Tools_Array.getUniqueValues(i);
		for(int j : i)System.out.print(j+",");
	}
	
	
	@SuppressWarnings("unused")
	public void testSAM(){
		SAMFileHeader header = new SAMFileHeader();
		header.setSortOrder(SortOrder.unsorted);
		String l1="@SQ	SN:chr1	LN:101";
		SAMReadGroupRecord contig1 = new SAMReadGroupRecord(l1);
		header.addReadGroup(contig1);
		String prog = "@PG	ID:Eddie"+PropertyLoader.getFullVersion()+"	PN:Eddie	VN:"+PropertyLoader.getFullVersion()+"	CL:"+cmd.toString();
		SAMProgramRecord record = new SAMProgramRecord(prog);
		header.addProgramRecord(record);
		SAMRecord test = new SAMRecord(header);
	}
	
	public void testACE(){
		try{
			FileInputStream stream = new FileInputStream(new File("/home/dominic/PhD_Data/newbler_projects/Digest_Only_Mk3/DigestMK3/assembly/454Isotigs.ace"));
			ACEFileParser parser = new ACEFileParser(stream);
			LinkedList<ACERecord> records = new LinkedList<ACERecord>();
			while(parser.hasNext()){
				records.add(parser.next());
				System.out.print("\r"+records.getLast().getContigName()+"      ");
				//ACERecord record = parser.next();
				//System.out.print("\r"+record.getContigName()+"      ");
			}
		}
		catch(IOException io){
			logger.error("Error thrown",io);
		}
		System.out.println();
	}
	
	public void testStringSort(){
		System.out.println("Running String Sort by length Method");
		String[] r = new String[]{"a", "BBBB", "AA", "FASFSFSAFSD", "fddd", "A"};
		for(int i =0; i < r.length; i++)System.out.print(r[i]+", ");
		System.out.println();
		ArrayList<String> rs = new ArrayList<String>();
		for(int i =0; i < r.length; i++)rs.add(r[i]);
		rs = (ArrayList<String>)Tools_String.sortStringsByLength(rs);
		for(int i =0; i < rs.size(); i++)System.out.print(rs.get(i)+", ");
		System.out.println();
		System.out.println("Done");
	}
	
	public void testLongestNumberGetterer(){
		System.out.println("Running getLongestNumber in String Method");
		String[] r = new String[]{"a222", "B22BBB4343", "A11A", "FASFS123212FSAFS2D", "fdd3333d", "A"};
		int[] r2 = new int[]{222, 4343, 11, 123212, 3333, -1};
		System.out.println("Strings inputted");
		for(int i =0; i < r.length; i++)System.out.print(r[i]+", ");
		System.out.println();
		System.out.println("What I expect returned:");
		for(int i =0; i < r.length; i++)System.out.print(r2[i]+", ");
		System.out.println();
		System.out.println("What I actually is returned:");
		int[] ia = new int[r.length];
		for(int i =0; i < r.length; i++)ia[i]=Tools_String.getLongestInt(r[i]);
		for(int i =0; i < r.length; i++)System.out.print(ia[i]+", ");
		System.out.println();
		for(int i =0; i < r.length; i++){
			if(ia[i]==r2[i])System.out.print("PASS, ");
			else System.out.print("FAIL, ");
		}
		System.out.println();
	}
	
}

