package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.databases.manager.DatabaseManager;

public class Term {

	protected int ontology_id;
	protected int term_id;
	protected String name;
	protected String definition;
	protected String identifier;
	protected String is_obselete;
	
	public Term(){};
	
	public Term(int ontology_id, int term_id, String name, String definition,
			String identifier, String is_obselete) {
		this.ontology_id = ontology_id;
		this.term_id = term_id;
		this.name = name;
		this.definition = definition;
		this.identifier = identifier;
		this.is_obselete = is_obselete;
	}
	
	public Term(int ontology_id, String name, String definition,
			String identifier, String is_obselete) {
		this.ontology_id = ontology_id;
		this.name = name;
		this.definition = definition;
		this.identifier = identifier;
		this.is_obselete = is_obselete;
	}

	public int getOntology_id() {
		return ontology_id;
	}

	public void setOntology_id(int ontology_id) {
		this.ontology_id = ontology_id;
	}

	public int getTerm_id() {
		return term_id;
	}

	public void setTerm_id(int term_id) {
		this.term_id = term_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIs_obselete() {
		return is_obselete;
	}

	public void setIs_obselete(String is_obselete) {
		this.is_obselete = is_obselete;
	}
	
	//TODO update if necessary
	public int upload(DatabaseManager manager){
		Term t = manager.getBioSQL().getTerm(manager.getCon(),this.getName(), this.getIdentifier(), this.getOntology_id());
		if(t==null){
			manager.getBioSQL().addTerm(manager.getCon(), this.getName(), this.getDefinition(), 
					this.getIdentifier(), this.getIs_obselete(), this.getOntology_id());
			t = manager.getBioSQL().getTerm(manager.getCon(),this.getName(), this.getIdentifier(), this.getOntology_id());
		}
		if (t!= null)this.setTerm_id(t.getTerm_id());
		return this.getTerm_id();
	}

}
