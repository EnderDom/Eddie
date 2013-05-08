package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.databases.manager.DatabaseManager;

public class TermRelationship {

	private int term_rel_id;
	private int subject_id;
	private int predicate_id;
	private int object_id;
	private int ontology_id;
	

	public TermRelationship(){
		
	}

	public int getTermRel_id() {
		return term_rel_id;
	}

	public void setTermRel_id(int term_id) {
		this.term_rel_id = term_id;
	}

	public int getSubject_id() {
		return subject_id;
	}

	public void setSubject_id(int subject_id) {
		this.subject_id = subject_id;
	}

	public int getPredicate_id() {
		return predicate_id;
	}

	public void setPredicate_id(int predicate_id) {
		this.predicate_id = predicate_id;
	}

	public int getObject_id() {
		return object_id;
	}

	public void setObject_id(int object_id) {
		this.object_id = object_id;
	}

	public int getOntology_id() {
		return ontology_id;
	}

	public void setOntology_id(int ontology_id) {
		this.ontology_id = ontology_id;
	}

	public int upload(DatabaseManager manager){
		TermRelationship ship = manager.getBioSQL().getTermRelationship(manager.getCon(), this.getSubject_id(), 
				this.getObject_id(), this.getPredicate_id(), this.getOntology_id());
		if(ship == null){
			manager.getBioSQL().addTermRelationship(manager.getCon(), this.getSubject_id(), 
				this.getObject_id(), this.getPredicate_id(), this.getOntology_id());
			ship = manager.getBioSQL().getTermRelationship(manager.getCon(), this.getSubject_id(), 
					this.getObject_id(), this.getPredicate_id(), this.getOntology_id());
		}
		this.setTermRel_id(ship.getTermRel_id());
		return this.getTermRel_id();
	}
	
}
