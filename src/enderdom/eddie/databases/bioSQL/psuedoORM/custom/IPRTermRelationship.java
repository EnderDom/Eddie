package enderdom.eddie.databases.bioSQL.psuedoORM.custom;

import enderdom.eddie.bio.homology.InterproObject;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.bioSQL.psuedoORM.TermRelationship;
import enderdom.eddie.databases.manager.DatabaseManager;

public class IPRTermRelationship extends TermRelationship{
	
	private String IPRsubject_id;
	private IPRPredicates IPRPredicate;
	private String IPRobject_id;
	
	public IPRTermRelationship(){
		init();
	}
	
	public IPRTermRelationship(int ontology){
		init();
		this.setObject_id(ontology);
	}
	
	private void init(){
		this.setSubject_id(-1);
		this.setObject_id(-1);
		this.setPredicate_id(-1);
	}
	
	public String getIPRsubject_id() {
		return IPRsubject_id;
	}

	public void setIPRsubject_id(String iPRsubject_id) {
		IPRsubject_id = iPRsubject_id;
	}

	public IPRPredicates getIPRPredicate() {
		return IPRPredicate;
	}

	public void setIPRPredicate(IPRPredicates iPRPredicate) {
		IPRPredicate = iPRPredicate;
	}

	public String getIPRobject_id() {
		return IPRobject_id;
	}

	public void setIPRobject_id(String iPRobject_id) {
		IPRobject_id = iPRobject_id;
	}

	public void upload(DatabaseManager manager, InterproObject parent) {
		this.setOntology_id(parent.getOntology_id());
		if(this.getSubject_id() <0){
			this.setSubject_id(downloadTermId(manager, IPRsubject_id));
		}
		if(this.getObject_id() <0){
			this.setObject_id(downloadTermId(manager, IPRobject_id));
		}
		if(this.getPredicate_id() <0){
			this.setPredicate_id(parent.getIPROntology().getPredicateTermId(IPRPredicate));
		}
		super.upload(manager);
	}
	
	private int downloadTermId(DatabaseManager manager, String term){
		Term t = manager.getBioSQL().getTerm(manager.getCon(), null, term, this.getOntology_id());
		if(t == null) {
			manager.getBioSQL().addTerm(manager.getCon(), null, null, term, "n", this.getOntology_id());
			t = manager.getBioSQL().getTerm(manager.getCon(), null, term, this.getOntology_id());
		}
		
		return t!= null ? t.getTerm_id() : -1;
	}
	
}
