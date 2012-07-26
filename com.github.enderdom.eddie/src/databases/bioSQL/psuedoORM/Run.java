package databases.bioSQL.psuedoORM;

import java.util.Date;
import java.text.ParseException;

import org.apache.log4j.Logger;

import tools.Tools_System;

import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;

public class Run {
/*

*/
	private Logger logger = Logger.getRootLogger();
	private int run_id;
	private Date date;
	private String runtype;
	private String program;
	private String dbname;
	private String params;
	private String comment;
	private String version;
	private String[] validationErrors = {"","",""};
	
	public Run(int r, Date t,String rt, String p, String v, String d, String a, String c){
		run_id = r;
		date = t;
		runtype = rt;
		program =p;
		version = v;
		dbname = d;
		params = a;
		comment =c;
	}
	
	public Run(Date t,String rt, String p, String v, String d, String a, String c){
		date = t;
		runtype = rt;
		program =p;
		version = v;
		dbname = d;
		params = a;
		comment =c;
	}
	
	public Run(){
		
	}
	
	public int getRun_id() {
		return run_id;
	}
	public void setRun_id(int run_id) {
		this.run_id = run_id;
	}
	public Date getDate() {
		return date;
	}
	
	public boolean setDateValue(String date, String expectedformat){
		try{
			this.date = Tools_System.getDateFromString(date, expectedformat);
			return true;
		}
		catch(ParseException p ){
			logger.error("Failed to parse date " + date + " with format " + expectedformat);
			return false;
		}
	}
	
	public String getDateValue(String format){
		return Tools_System.getDate(format, getDate());
	}
	
	public void setDate(Date datetime) {
		this.date = datetime;
	}
	public String getRuntype() {
		return runtype;
	}
	public void setRuntype(String runtype) {
		this.runtype = runtype;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	//Note, converts util.Date to sql.Date, see http://stackoverflow.com/questions/530012/how-to-convert-java-util-date-to-java-sql-date
	public int uploadRun(DatabaseManager manager){
		if(manager.getBioSQLXT().setRun(manager, new java.sql.Date(getDate().getTime()),
				getRuntype(), getProgram(), getVersion(), getDbname(), getParams(), getComment())){
			this.run_id = manager.getLastInsert(); //This could get me into trouble.... :(
			return run_id;
		}
		else{
			return run_id;
		}
	}

	public boolean validate() {
		if(this.getDate() == null){
			validationErrors[0] = "Date is not set";
			return false;
		}
		if(this.getProgram() == null){
			validationErrors[1] = "Program name is not defined";
			return false;
		}
		if(this.getRuntype() == null){
			validationErrors[2] = "Run type is not defined";
		}
		return true;
	}

	public String list(DatabaseManager manager) {
		String[] columns = new String[]{"runtype", "program", "version", "dbname"};
		String[][] fields = manager.getBioSQLXT().getUniqueStringFields(manager, columns, BioSQLExtended.runtable);
		StringBuffer ret = new StringBuffer();
		String newline = Tools_System.getNewline();
		for(int i =0; i < fields.length; i++){
			ret.append(" -- ");
			ret.append(columns[i]);
			ret.append(" -- ");
			ret.append(newline);
			ret.append(newline);
			for(int j = 0; j < fields[i].length; j++){
				ret.append(fields);
				ret.append(newline);
			}
		}
		return ret.toString();
	}

	public String[] getValidationErrors() {
		return validationErrors;
	}
	
}
