package enderdom.eddie.databases.bioSQL.psuedoORM;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_System;

import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
import enderdom.eddie.databases.manager.DatabaseManager;

public class Run {
/*

*/
	private Logger logger = Logger.getRootLogger();
	private int run_id;
	private int parent_id;
	private Date date;
	private String runtype;
	private String program;
	private String dbname;
	private String source;
	private String params;
	private String comment;
	private String version;
	private String[] validationErrors = {"","",""};
	public static String[] runfields = new String[]{"run_date", "runtype", "parent_id", "program", "version", "dbname", "source", "params", "comment"};
	public static String RUNTYPE_ASSEMBLY = "ASSEMBLY";
	public static String RUNTYPE_blast = "blast";
	public static String RUNTYPE_454 = "454";
	
	
	public Run(int r, Date time, String runtype, int parent, String program,
			String version, String dbname, String source, String params, String comment){
		this.run_id = r;
		this.date = time;
		this.runtype = runtype;
		this.parent_id = parent;
		this.program =program;
		this.version = version;
		this.dbname = dbname;
		this.source= source;
		this.params = params;
		this.comment =comment;
	}
	
	public Run(Date time, String runtype, int parent, String program,
			String version, String dbname, String source, String params, String comment){
		this.date = time;
		this.runtype = runtype;
		this.parent_id = parent;
		this.program =program;
		this.version = version;
		this.dbname = dbname;
		this.source= source;
		this.params = params;
		this.comment =comment;
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
	
	public String getSource(){
		return source;
	}
	
	public void setSource(String source){
		this.source = source;
	}

	public int getParent_id() {
		return parent_id;
	}

	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}

	//Note, converts util.Date to sql.Date, see http://stackoverflow.com/questions/530012/how-to-convert-java-util-date-to-java-sql-date
	public int uploadRun(DatabaseManager manager){
		if(manager.getBioSQLXT().setRun(manager, Tools_System.util2sql(getDate()),
				getRuntype(), getParent_id(), getProgram(), getVersion(), getDbname(), getSource(), getParams(), getComment())){
			this.run_id = manager.getLastInsert(); //This could get me into trouble.... :(
			return run_id;
		}
		else{
			return -1;
		}
	}

	public boolean validate() {
		if(this.getDate() == null){
			validationErrors[0] = "Date is not set, String inputs should be in format " + Tools_System.SQL_DATE_FORMAT;
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
		String[] columns = new String[]{"run_id","runtype", "program", "version", "dbname", "source"};
		String[][] fields = manager.getBioSQL().getGenericResults(manager.getCon(), columns, BioSQLExtended.runtable, null, null);
		StringBuffer ret = new StringBuffer();
		String newline = Tools_System.getNewline();
		for(int i =0; i < columns.length; i++){
			ret.append(columns[i]);
			ret.append("\t\t");
		}
		ret.append(newline);
		ret.append(newline);
		for(int i =0; i < fields.length; i++){
			for(int j = 0; j < fields[i].length; j++){
				ret.append(fields[i][j]);
				ret.append("\t\t");
			}
			ret.append(newline);
		}
		return ret.toString();
	}

	public String[] getValidationErrors() {
		return validationErrors;
	}

	//TODO move this to the mysql stuff
	public int getSimilarRun(DatabaseManager manager, int date_range) {
		int ret = -1;
		long range =date_range;
		try{
			ResultSet set = manager.runSQLQuery("SELECT run_id, run_date FROM run WHERE " +
				"runtype='"+this.runtype+"' AND program='"+this.program+"' AND version='"+
				this.version+"' AND dbname='"+this.dbname+"' AND params='"+this.params+"'");
			while(set.next()){
				java.sql.Date r = set.getDate("run_date");
				long l = Math.abs(r.getTime()-this.getDate().getTime());
				l /=86400; //(60*60*24)
				if(l < range){
					range = l;
					ret=set.getInt("run_id");
				}
			}
			return ret;
		}
		catch(SQLException sq){
			logger.error("Failed to conduct SQL for similar runs", sq);
			return -1;
		}
	}
	
	public static String[] getRunInsertTips(){
		return new String[]{"Date when run was started FORMAT MUST BE: DD-MM-YYYY","Runtype, ie assembly or blast",
				"Name of the program run ie Blastx","Version","Database name ie nr (Can be left blank)","Parameters used, ie -word_size 10"," Any comments you want to add"};
	}
	
}
