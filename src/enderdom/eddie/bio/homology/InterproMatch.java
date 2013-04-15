package enderdom.eddie.bio.homology;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.manager.DatabaseManager;

/*
 * Should use seqfeature and location, 
 * but I have not the time to implement that
 * :( Tempus Fugit
 * 
 */

public class InterproMatch{

	private String identifier;
	private String name;
	private String dbname;
	private LinkedList<InterproLocation> locations;
	Logger logger = Logger.getRootLogger();
	
	public InterproMatch(){
		locations =  new LinkedList<InterproLocation>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public LinkedList<InterproLocation> getLocations() {
		return locations;
	}

	public void setLocations(LinkedList<InterproLocation> locations) {
		this.locations = locations;
	}
	
	public void addLocation(InterproLocation loc){
		this.locations.add(loc);
	}

	public void upload(DatabaseManager manager, InterproObject parent) {
		int acc = manager.getBioSQL().getDBxRef(manager.getCon(), dbname, identifier);
		if(acc < 1){
			manager.getBioSQL().addDBxref(manager.getCon(), dbname, identifier, 1);
			acc = manager.getBioSQL().getDBxRef(manager.getCon(), dbname, identifier);
		}
		if(acc > 0){
			if(!manager.getBioSQL().addDbxrefTermPath(manager.getCon(), acc, parent.getTerm_id(), 0, null)){
				logger.error("Failed to pair term name "+parent.getName() +
						" (id:"+parent.getTerm_id()+") and dbxref for " + identifier +" id("+acc+")");
			}
			int bioen = manager.getBioSQL().getBioEntry(manager.getCon(), parent.getProtein(), parent.getProtein(), manager.getEddieDBID());
			if(bioen > 0){
				for(int i=0; i < locations.size(); i++){
					InterproLocation loc = locations.get(i);
					manager.getBioSQLXT().setBioentry2Dbxref(manager, bioen, acc, parent.getRuntID(), loc.getScore(), 0, loc.getStart(), loc.getEnd(),
							0, loc.getStart(), loc.getEnd(), 0, i, 1);
				}
			}
			else{
				logger.error("Could not retrieve bioentry for "+ parent.getProtein() 
						+ " with "+identifier+"no db link generated");
			}
		}
		else{
			logger.error("Could not retrieve accession for "+ identifier + " no db link generated");
		}
	}
}
