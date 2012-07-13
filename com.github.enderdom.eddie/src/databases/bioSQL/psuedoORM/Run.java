package databases.bioSQL.psuedoORM;

import java.sql.Timestamp;

public class Run {
/*
	String table = "CREATE TABLE IF NOT EXISTS run (" +
	"run_id INT(10) UNSIGNED NOT NULL auto_increment, " +
	"run_date date NOT NULL, " +
	"runtype VARCHAR(20) BINARY, " +
  	"program VARCHAR(40) BINARY, " +
  	"dbname VARCHAR(40) BINARY, " +
  	"params TEXT, " +
  	"comment TEXT, " +
	"PRIMARY KEY (bioentry_id), " +
 	"UNIQUE (run_id)" +
*/
	
	private int run_id;
	private Timestamp datetime;
	private String runtype;
	private String program;
	private String dbname;
	private String params;
	private String comment;
	
	public Run(int r, Timestamp t,String rt, String p, String d, String a, String c){
		run_id = r;
		datetime = t;
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
	public Timestamp getDatetime() {
		return datetime;
	}
	public void setDatetime(Timestamp datetime) {
		this.datetime = datetime;
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
	
//	public boolean uploadRun(){
//		return false;
//	}
}
