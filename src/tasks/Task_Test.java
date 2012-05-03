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

import tools.Tools_Array;
import tools.Tools_Fun;
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

	protected UI ui; 
	protected PropertyLoader load;
	protected CommandLine cmd;
	protected Logger logger = Logger.getLogger("Testing");
	protected Checklist checklist;
	
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
			
			testRot13();
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
	
	public void testChecklist(){
		
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
	
	public void openChecklist(){
		checklist = new Checklist(ui.getPropertyLoader().getWorkspace(), this.getClass().getName());
		if(checklist.check()){
			logger.trace("Moved to recovering past Task");
			int userinput = ui.requiresUserYNI("There is an unfinished task, Details: "+checklist.getLast()+" Would you like to recover it (yes), delete it (no) or ignore it (cancel)?","Recovered Unfinished Task...");
			if(userinput == 1){
				if(!checklist.closeLastTask()){
					logger.error("Failed to delete last task");
				}
				else{
					logger.debug("Cleared Task");
				}
			}
			if(userinput == 0){
				args = checklist.getComment();
				if(args != null){
					super.parseArgs(args.split(" "));
					checklist.recoverLast();
				}
				else{
					logger.error("An error occured, Comment does not contain arguments");
				}
			}
			else{
				checklist = new Checklist(ui.getPropertyLoader().getWorkspace(), this.getClass().getName());
			}
		}
	}
}

