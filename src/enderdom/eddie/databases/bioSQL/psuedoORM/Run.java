package enderdom.eddie.databases.bioSQL.psuedoORM;

import java.util.Date;
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
	private Integer parent_id;
	private Date date;
	private String runtype;
	private String program;
	private String dbname;
	private String source;
	private String params;
	private String comment;
	private String version;
	private String[] validationErrors = {"","",""};
	public static String[] runfields = new String[]{"run_date", "runtype",
		"parent_id", "program", "version", "dbname", "source", "params", "comment"};
	public static String RUNTYPE_ASSEMBLY = "ASSEMBLY";
	public static String RUNTYPE_blast = "blast";
	public static String RUNTYPE_454 = "454";
	public static String RUNTYPE_INTERPRO = "INTERPRO";
	public static String RUNTYPE_TRANSLATE = "TRANSLATE";
	public static String RUNTYPE_ASSEMBLY_META = "ASSEMBLY_META";
	
	
	public Run(int r, Date time, String runtype, Integer parent, String program,
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
	
	public Run(Date time, String runtype, Integer parent, String program,
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

	public Integer getParent_id() {
		return parent_id;
	}

	public void setParent_id(Integer parent_id) {
		this.parent_id = parent_id;
	}

	//Note, converts util.Date to sql.Date, see http://stackoverflow.com/questions/530012/how-to-convert-java-util-date-to-java-sql-date
	public int uploadRun(DatabaseManager manager){
		if(manager.getBioSQLXT().setRun(manager, Tools_System.util2sql(getDate()),
				getRuntype(), getParent_id(), getProgram(), getVersion(),
				getDbname(), getSource(), getParams(), getComment())){
			return manager.getBioSQLXT().getRunIdFromInfo(manager, this);
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
		String[][] fields = manager.getSQLGeneral().getResults(manager.getCon(), 
				columns, BioSQLExtended.runtable, null, null);
		StringBuffer ret = new StringBuffer();
		String newline = Tools_System.getNewline();
		for(int i =0; i < columns.length; i++){
			ret.append(columns[i]);
			ret.append(", ");
		}
		ret.append(newline);
		ret.append(newline);
		
		for(int i =0; i < fields[0].length; i++){
			for(int j = 0; j < fields.length; j++){
				ret.append(fields[j][i]);
				ret.append(", ");
			}
			ret.append(newline);
		}
		return ret.toString();
	}

	public String[] getValidationErrors() {
		return validationErrors;
	}

	public int getSimilarRun(DatabaseManager manager, int date_range){
		return manager.getBioSQLXT().getSimilarRun(manager, this, date_range);
	}
	
	public static String[] getRunInsertTips(){
		return new String[]{"Date when run was started FORMAT MUST BE: DD-MM-YYYY",
				"Runtype, ie assembly or blast",
				"Name of the program run ie Blastx",
				"Version","Database name ie nr (Can be left blank)",
				"Parameters used, ie -word_size 10"," Any comments you want to add"};
	}

	public Run getParent(DatabaseManager manager){
		return manager.getBioSQLXT().getRun(manager, this.getParent_id());
	}
	
	public Run getGrandParent(DatabaseManager manager) {
		Run p = this.getParent(manager);
		return p == null ? null : p.getParent(manager);
	}

}
