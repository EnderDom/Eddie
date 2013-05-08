package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.databases.manager.DatabaseManager;

public class Ontology {
	
	protected int ontology_id;
	protected String name;
	protected String definition;
	
	public Ontology(){
		
	}
	
	public Ontology(String name, String definition){
		this.name = name;
		this.definition =definition;  
	}
	
	public Ontology(int ontology_id, String name, String definition){
		this.ontology_id = ontology_id;
		this.name = name;
		this.definition =definition;
	}

	public int getOntology_id() {
		return ontology_id;
	}

	public void setOntology_id(int ontology_id) {
		this.ontology_id = ontology_id;
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
	
	//TODO update if necessary
	public int upload(DatabaseManager manager){
		Ontology o = manager.getBioSQL().getOntology(manager.getCon(), name);
		if(o == null){
			manager.getBioSQL().addOntology(manager.getCon(), name, definition);
			o = manager.getBioSQL().getOntology(manager.getCon(), name);
		}
		if(o!=null)setOntology_id(o.getOntology_id());
		return this.getOntology_id(); 
	}
}
