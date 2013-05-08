package enderdom.eddie.databases.bioSQL.psuedoORM.custom;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.psuedoORM.Ontology;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.manager.DatabaseManager;

/**
 * 
 * @author dominic
 * Eddie Ontology
 * manages eddie specific terms
 * which are used to link stuff together
 *
 */
public class EddieOntologyFactory extends Ontology{
	
	public static String eddie_ontology= "EDDIE_ONTOLOGY";
	public static String eddie_definition =
			"A list of terms used by Eddie to link various parts of the database together";
	Term[] children;
	EddieTerm[] terms;
	Logger logger = Logger.getRootLogger();
	
	public EddieOntologyFactory(DatabaseManager manager){
		this.name = eddie_ontology;
		this.definition = eddie_definition;
		Ontology o = manager.getBioSQL().getOntology(manager.getCon(), this.getName());
		if(o == null)this.upload(manager);
		else this.setOntology_id(o.getOntology_id());
		logger.debug("Acquired ontology id as "+ ontology_id);
		populateTerms(manager);
	}
	
	private void populateTerms(DatabaseManager manager){
		terms = new EddieTerm[]{EddieTerm.CONTIG,
				EddieTerm.TRANSLATE};
		String[] defs = new String[]{"Contig (Object bioentry) is contains (Subject bioentry)",
				"Object bioentry is a protein translation of subject bioentry"};
		children = new Term[terms.length];
		for(int i=0; i < children.length; i++){
			children[i] = manager.getBioSQL().getTerm(manager.getCon(),terms[i].toString(), terms[i].toString(), this.getOntology_id());
			if(children[i] == null){
				manager.getBioSQL().addTerm(manager.getCon(), terms[i].toString(), defs[i], terms[i].toString(), "n", this.getOntology_id());
				children[i] = manager.getBioSQL().getTerm(manager.getCon(),terms[i].toString(), terms[i].toString(), this.getOntology_id());				
			}
			if(children[i] == null)logger.error("Failed to aquire term id for "+ terms[i].toString());
			else logger.debug("Acquired term for "+children[i].getTerm_id()+" for "+ terms[i].toString());
		}
	}
	
	public Term getTerm(EddieTerm term){
		for(int i=0;i< terms.length ; i++){
			if(term == terms[i])return children[i];
		}
		return null;
	}
	
}
