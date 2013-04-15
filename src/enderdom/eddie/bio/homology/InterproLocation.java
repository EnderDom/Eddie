package enderdom.eddie.bio.homology;

import enderdom.eddie.tools.Tools_String;

public class InterproLocation {

	private int start;
	private int end;
	private double score;
	private String status;
	private String evidence;
	
	public InterproLocation(){}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}
	
	public void setStart(String s){
		Integer i = Tools_String.parseString2Int(s);
		if(i!=null)this.start=i;
		else this.start=-1;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public void setEnd(String s){
		Integer i = Tools_String.parseString2Int(s);
		if(i!=null)this.end=i;
		else this.end=-1;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public void setScore(String s){
		Double i = Tools_String.parseString2Double(s);
		if(i!=null)this.score=i;
		else this.score=-1;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	
	
	
}
