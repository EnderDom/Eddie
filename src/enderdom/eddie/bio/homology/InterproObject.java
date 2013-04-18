package enderdom.eddie.bio.homology;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.IPRPredicates;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.IPRTermRelationship;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.IPRTypes;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.InterproOntologyFactory;
import enderdom.eddie.databases.manager.DatabaseManager;

/**
 * Represents a single interpro match
 * represented by <interpro> tag in 
 * the xml output
 * @author dominic
 *
 * 
 *
 */
public class InterproObject extends Term{

	private String protein;
	private String type;
	LinkedList<GOTermData> goterms;
	LinkedList<IPRTermRelationship> rels;
	LinkedList<InterproMatch> matches;
	private String interproversion;
	private InterproOntologyFactory factory;
	private int run_id;
	Logger logger = Logger.getRootLogger();
	
	public InterproObject(int run_id){
		this.run_id = run_id;
		setup();
	}
	
	public InterproObject(){
		setup();
	}
	
	private void setup(){
		goterms = new LinkedList<GOTermData>();
		rels = new LinkedList<IPRTermRelationship>();
		matches = new LinkedList<InterproMatch>();
	}

	public String getProtein() {
		return protein;
	}

	public void setProtein(String protein) {
		this.protein = protein;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void addGOTermData(GOTermData data){
		this.goterms.add(data);
	}
	
	public void addInterproMatch(InterproMatch match){
		this.matches.add(match);
	}
	
	public void addRelationship(IPRTermRelationship ship){
		this.rels.add(ship);
	}

	public void addParent(String attributeValue) {
		IPRTermRelationship ship = new IPRTermRelationship();
		ship.setIPRsubject_id(this.getIdentifier());
		ship.setIPRobject_id(attributeValue);
		ship.setIPRPredicate(IPRPredicates.IS_A);
	}
	
	public int upload(DatabaseManager manager){
		try{
			factory = new InterproOntologyFactory(manager, interproversion);
			this.setOntology_id(factory.getIpr_ont());
			this.setIs_obselete("n");
			super.upload(manager);
			this.uploadType(manager, factory);
			//TODO work out how to get names as well as identifiers
			//for(IPRTermRelationship ship : rels)ship.upload(manager, this);
			for(InterproMatch match : matches)match.upload(manager, this);			
			return this.getTerm_id();
		}
		catch(Exception e){
			logger.error("Failed to upload interpro", e);
			return -1;
		}
	}

	private void uploadType(DatabaseManager manager, InterproOntologyFactory factory) {
		IPRTermRelationship ship = new IPRTermRelationship(this.getOntology_id());
		ship.setIPRsubject_id(this.getIdentifier());
		IPRTypes t = factory.getIPRType(type);
		ship.setObject_id(factory.getIPRType(t));
		ship.setIPRPredicate(IPRPredicates.IS_A);
		if(t != null && t != IPRTypes.unintegrated)ship.upload(manager, this);
	}

	public InterproOntologyFactory getIPROntology() {
		return factory;
	}

	public int getRuntID() {
		return this.run_id;
	}
	
}
