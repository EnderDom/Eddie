package tasks;



import org.apache.commons.cli.CommandLine;

import ui.PropertyLoader;
import ui.UI;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileHeader.SortOrder;
import gui.EddieGUI;

public class Task_Test extends Task{

	UI ui;
	PropertyLoader load;
	CommandLine cmd;
	
	public Task_Test(){
		complete = -1;
		this.testmode = true;
	}
	
	
	
	public void runTest(){
		/*
		 * Testing long print
		 */
		try{
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
		this.ui = ui;
		this.load = ui.getPropertyLoader();
	}
	
}

