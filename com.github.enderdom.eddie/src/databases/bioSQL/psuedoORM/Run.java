package databases.bioSQL.psuedoORM;

import java.sql.Date;

import databases.manager.DatabaseManager;

public class Run {
/*
 * Table create here:
 * 
	String table = "CREATE TABLE IF NOT EXISTS run (" +
	"run_id INT(10) UNSIGNED NOT NULL auto_increment, " +
	"run_date date NOT NULL, " +
	"runtype VARCHAR(20) BINARY, " +
  	"program VARCHAR(40) BINARY, " +
  	"dbname VARCHAR(40) BINARY, " +
  	"params TEXT, " +
  	"comment TEXT, " +
*/
	
	private int run_id;
	private Date date;
	private String runtype;
	private String program;
	private String dbname;
	private String params;
	private String comment;
	
	public Run(int r, Date t,String rt, String p, String d, String a, String c){
		run_id = r;
		date = t;
		runtype = rt;
		program =p;
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
	
	public boolean uploadRun(DatabaseManager manager){
		if(manager.getBioSQLXT().setRun(manager, getDate(), 
				getRuntype(), getProgram(), getDbname(), getParams(), getComment())){
			this.run_id = manager.getLastInsert();
			return (this.run_id != -1);
		}
		else{
			return false;
		}
	}
	
	
}
