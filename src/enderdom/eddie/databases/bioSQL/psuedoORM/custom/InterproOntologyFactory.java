package enderdom.eddie.databases.bioSQL.psuedoORM.custom;

import enderdom.eddie.databases.bioSQL.psuedoORM.Ontology;
import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.manager.DatabaseManager;

public class InterproOntologyFactory {

	private static String ontology = "InterPro";
	private static String version = "4.8";
	private int ipr_ont;
	
	private static String relatetype = "relationship type";
	private int relate;
	
	private IPRPredicates cates[];
	private int predicate_ids[];
	
	private IPRTypes[] IPRtypes;
	private int[] iprtype_id;
	
	
	public InterproOntologyFactory(DatabaseManager manager, String interproversion) throws Exception{
		if(interproversion != null)version=interproversion;
		setup(manager);
	}

	public void setup(DatabaseManager manager) throws Exception{
		//Interpro Term ontology
		ipr_ont = getOntology(manager, ontology,  "InterPro version "+version);
		//Basic Relationship Ontology
		relate = getOntology(manager, relatetype,  "Ontology for defining how terms relate, such as IS_A or CONTAINS");
		if(ipr_ont < 1 || relate < 1)throw new Exception("Cannot retrieve ontology ids, these are needed to upload terms.");
		
		/*
		 * Tries to mimics bioperl's way of
		 * uploading IPR terms as best as possible.
		 * Creates some predicate terms to
		 * use in term relationship table
		 */
		cates = new IPRPredicates[]{IPRPredicates.IS_A, IPRPredicates.PART_OF, IPRPredicates.CONTAINS, IPRPredicates.FOUND_IN};
		predicate_ids = new int[cates.length];
		String nfo = " relationship predicate (type)"; 
		for(int i=0;i<cates.length;i++){
			Term t = manager.getBioSQL().getTerm(manager.getCon(),cates[i].toString(), null, relate);
			if(t != null)predicate_ids[i]=t.getTerm_id(); 
			else {
				manager.getBioSQL().addTerm(manager.getCon(), cates[i].toString(),
						cates[i].toString()+nfo, null, "n", relate);
				predicate_ids[i] = manager.getBioSQL().getTerm(manager.getCon(),
						cates[i].toString(), null, relate).getTerm_id();
			}
		}	
		
		/*
		 * Create IPR types, as there is not
		 * column for them, so they become a 
		 * new  
		 */
		IPRtypes = new IPRTypes[]{IPRTypes.Family, IPRTypes.Domain, IPRTypes.Repeat, 
				IPRTypes.PTM, IPRTypes.Active_site, IPRTypes.Binding_site};
		iprtype_id = new int[IPRtypes.length];
		for(int i =0;i < IPRtypes.length; i++){
			String name = IPRtypes[i].toString();
			if(i==3)name="post-translational modification"; //PTM hack
			Term t = manager.getBioSQL().getTerm(manager.getCon(), null, "IPR:"+IPRtypes[i].toString(), ipr_ont);
			if(t != null) iprtype_id[i] = t.getTerm_id();
			else {
				manager.getBioSQL().addTerm(manager.getCon(), name, null, "IPR:"+IPRtypes[i].toString(), "n", ipr_ont);
				iprtype_id[i] =manager.getBioSQL().getTerm(manager.getCon(), null, "IPR:"+IPRtypes[i].toString(), ipr_ont).getTerm_id();
			}
		}
		
	}
	
	public int getPredicateTermId(IPRPredicates pred){
		for(int i=0; i < cates.length; i++){
			if(cates[i] == pred)return predicate_ids[i];
		}
		return -1;
	}

	public int getIPRType(IPRTypes t){
		for(int i=0; i < IPRtypes.length; i++){
			if(IPRtypes[i] == t)return iprtype_id[i];
		}
		return -1;
	}

	/**
	 * 
	 * 
	 * @return get integer value
	 * representing the database id
	 * for interpro ontology used for 
	 * adding Term records
	 */
	public int getIpr_ont() {
		return ipr_ont;
	}

	public void setIpr_ont(int ipr_ont) {
		this.ipr_ont = ipr_ont;
	}

	/**
	 * 
	 * @return relationship ontology
	 * for terms such as IS_A etc...
	 * Shouldn't need to be used as 
	 * they are all already uploaded in 
	 * theory
	 */
	public static String getRelatetype() {
		return relatetype;
	}

	public static void setRelatetype(String relatetype) {
		InterproOntologyFactory.relatetype = relatetype;
	}
	
	private int getOntology(DatabaseManager manager, String ont_name, String description){
		int ont_id =-1;
		Ontology o =  manager.getBioSQL().getOntology(manager.getCon(), ont_name);
		ont_id = o != null ? o.getOntology_id() : -1;
		if(ont_id < 1)manager.getBioSQL().addOntology(manager.getCon(), ont_name, description);
		o =  manager.getBioSQL().getOntology(manager.getCon(), ont_name);
		return o != null ? o.getOntology_id() : -1;
	}
	
}
