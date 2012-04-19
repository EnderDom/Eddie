package tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;

import bio.assembly.ACEFileParser;
import bio.assembly.ACERecord;

import tools.Tools_String;
import ui.PropertyLoader;
import ui.UI;
//import net.sf.samtools.SAMFileHeader;
//import net.sf.samtools.SAMProgramRecord;
//import net.sf.samtools.SAMReadGroupRecord;
//import net.sf.samtools.SAMRecord;
//import net.sf.samtools.SAMFileHeader.SortOrder;
import gui.EddieGUI;

public class Task_Test extends Task{

	UI ui;
	PropertyLoader load;
	CommandLine cmd;
	Logger logger = Logger.getLogger("Testing");
	
	public Task_Test(){
		complete = -1;
		this.testmode = true;
	}
	
	
	
	public void runTest(){
		/*
		 * Testing 
		 */
		try{
			System.out.println("Running test");
			
			DatabaseManager manager = this.ui.getDatabaseManager();
			manager.open();
			BioSQL bs = manager.getBioSQL();
			BioSQLExtended bsxt = manager.getBioSQLXT();
			Connection con = manager.getCon();
			int i = bsxt.getEddieFromDatabase(con);
			if(i == -1)bsxt.addEddie2Database(con);
			i = bsxt.getEddieFromDatabase(con);
			if(i != -1){
				bs.addSequence(con, i, null, "TEST1",
						"TEST1", null, null, null, 0, "ATGCGACTAG", BioSQL.alphabet_DNA);
			}
			else{
				logger.error("Failed to add Eddie to biodatabase in the database");
			}
			manager.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void addToGui(EddieGUI biodesktopgui) {
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		this.cmd = cmd;
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		logger.trace("UI added to " + this.getClass().getName());
		this.ui = ui;
		this.load = ui.getPropertyLoader();
	}
	
	/************************************************************/
	/*															*/
	/*				Testing & Experimental Methods	 			*/
	/*															*/
	/*															*/
	/************************************************************/
	
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

