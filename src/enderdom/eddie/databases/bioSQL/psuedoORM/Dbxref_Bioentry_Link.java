package enderdom.eddie.databases.bioSQL.psuedoORM;

public class Dbxref_Bioentry_Link {
	
	private int bioentry_id;
	private int dbxref_id;
	private int rank;
	private int hit_no;
	private int run_id;
	private double evalue;
	private int score;
	private int dbxref_startpos;
	private int dbxref_endpos;
	private int dbxref_frame;
	private int bioentry_startpos;
	private int bioentry_endpos;
	private int bioentry_frame;
	
	public Dbxref_Bioentry_Link(){
		
	}

	public int getBioentry_id() {
		return bioentry_id;
	}

	public void setBioentry_id(int bioentry_id) {
		this.bioentry_id = bioentry_id;
	}

	public int getDbxref_id() {
		return dbxref_id;
	}

	public void setDbxref_id(int dbxref_id) {
		this.dbxref_id = dbxref_id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getHit_no() {
		return hit_no;
	}

	public void setHit_no(int hit_no) {
		this.hit_no = hit_no;
	}

	public int getRun_id() {
		return run_id;
	}

	public void setRun_id(int run_id) {
		this.run_id = run_id;
	}

	public double getEvalue() {
		return evalue;
	}

	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getDbxref_startpos() {
		return dbxref_startpos;
	}

	public void setDbxref_startpos(int dbxref_startpos) {
		this.dbxref_startpos = dbxref_startpos;
	}

	public int getDbxref_endpos() {
		return dbxref_endpos;
	}

	public void setDbxref_endpos(int dbxref_endpos) {
		this.dbxref_endpos = dbxref_endpos;
	}

	public int getDbxref_frame() {
		return dbxref_frame;
	}

	public void setDbxref_frame(int dbxref_frame) {
		this.dbxref_frame = dbxref_frame;
	}

	public int getBioentry_startpos() {
		return bioentry_startpos;
	}

	public void setBioentry_startpos(int bioentry_startpos) {
		this.bioentry_startpos = bioentry_startpos;
	}

	public int getBioentry_endpos() {
		return bioentry_endpos;
	}

	public void setBioentry_endpos(int bioentry_endpos) {
		this.bioentry_endpos = bioentry_endpos;
	}

	public int getBioentry_frame() {
		return bioentry_frame;
	}

	public void setBioentry_frame(int bioentry_frame) {
		this.bioentry_frame = bioentry_frame;
	}
	
	
	
}
