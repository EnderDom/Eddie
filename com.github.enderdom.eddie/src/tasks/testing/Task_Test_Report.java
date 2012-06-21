package tasks.testing;

import java.io.File;
import java.util.LinkedList;
import java.util.Properties;

import modules.Module_Test;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import output.generic.Report;

import tasks.Task;
import tools.Tools_File;
import tools.Tools_System;
import tools.Tools_Web;
import ui.UI;

public class Task_Test_Report extends Task{

	String testfolder;
	public static String defaultout = "out"; 
	public static String defaultdata = "data";
	private boolean alice;
	private UI ui;
	Logger logger= Logger.getRootLogger();
	Properties testprops;
	
	public Task_Test_Report(){
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("alice"))alice=true;
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("alice",false, "Tests by retrieving alice in wonderland and outputing it"));
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		testprops = Module_Test.getProperties(this.ui);
		File testfold = new File(testprops.getProperty(Module_Test.TestOutFolder));
		if(!testfold.isDirectory()){
			testfold.mkdir();
		}
		checksubfolders();
		if(alice){
			printAlice();
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void checksubfolders(){
		String[] subs = new String[]{defaultdata, defaultout};
		for(int i =0 ;i < subs.length ; i++){
			File temp = new File(testfolder +subs[i]);
			if(!temp.exists()){
				temp.mkdir();
			}
		}
	}
	
	
	//TODO finish
	public void printAlice(){
		logger.debug("Starting Alice...");
		String root = "http://www.cs.cmu.edu/~rgs/";
		String s = Tools_Web.urlReader(root+"alice-ftitle.html");
		Tools_File.quickWrite(s, new File(testfolder+defaultout+Tools_System.getFilepathSeparator()+"out.html"), false);
		logger.debug("Stripping URLS...");
		String[] urls = Tools_Web.stripHrefs(s);
		LinkedList<String> list = new LinkedList<String>();
		logger.debug("Getting Page URLS...");
		for(int i =0 ; i < urls.length ; i ++){
			if(urls[i].contains("alice-")){
				list.add(urls[i]);
			}		
		}
		logger.debug("Got hrefs");
		for(int i = 0 ; i < list.size(); i++){
			logger.debug("Page "+i);
			String s1 = Tools_Web.urlReader(root+list.get(i));
			//String[] imgs = Tools_Web.stripImages(s1);
			int start = -1;
			int end = -1;
			if((start=s1.indexOf("<BODY>")+6)!= -1 && (end=s1.indexOf("</BODY>")) !=-1){
				s1 = s1.substring(start,end);
			}
			String[] s11 = s1.split("<P>");
			for(int j =0;j < s11.length; j++)s11[j]=s11[j].replaceAll("<(.*?)>", "");

			//HTML
			logger.debug("Writing to HTML File");
			Report report = new Report();
			report.setFileAndType(testfolder+defaultout+Tools_System.getFilepathSeparator()+"alice"+i+".html", Report.OUT_HTML);
			report.addParagraphs(s11);
			report.saveAndClose();
			
			//TXT
//			logger.debug("Writing to Text File");
//			Report report = new Report();
//			report.setFileAndType(testfolder+defaultout+Tools_System.getFilepathSeparator()+"alice"+i+".txt", Report.OUT_TEXT);
//			report.addParagraphs(s11);
//			report.saveAndClose();
			
			//PDF
//			logger.debug("Writing to PDF File, err... expect errors");
//			report = new Report();
//			report.setFileAndType(testfolder+defaultout+Tools_System.getFilepathSeparator()+"alice"+i+".pdf", Report.OUT_PDF);
//			report.addParagraphs(s11);
//			report.saveAndClose();
		}
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
}
