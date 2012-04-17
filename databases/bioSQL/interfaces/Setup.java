package databases.bioSQL.interfaces;

import java.sql.Connection;

public interface Setup {
	
	public boolean addEddie2Database(Connection con);

	public boolean addLegacyVersionTable(Connection con, String version, String dbversion);
	
}
