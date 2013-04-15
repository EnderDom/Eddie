package enderdom.eddie.bio.homology;

import enderdom.eddie.databases.bioSQL.psuedoORM.Term;
import enderdom.eddie.databases.manager.DatabaseManager;

public class GOTermData extends Term{
	
	public GOTermData(String go_id, String name, String description){
		this.identifier=go_id;
		this.name=name;
		this.definition=description;
	}
	
	public int upload(DatabaseManager manager){
		
		return super.upload(manager);
	}

}
