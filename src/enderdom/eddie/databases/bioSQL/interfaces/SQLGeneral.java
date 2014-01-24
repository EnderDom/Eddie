package enderdom.eddie.databases.bioSQL.interfaces;

import java.sql.Connection;

public interface SQLGeneral {
	

	/**
	 * Builds and executes a general SQL query to update a table
	 * Very basic simplisitc method that will probably break if you
	 * try to do anything complicated
	 *  
	 * @param con
	 * @param fields
	 * @param fieldvalues
	 * @param table
	 * @param wheres
	 * @param wherevalues
	 * @return String matrix String[ReturnedResults.size()][fields.length]
	 */
	public String[][] getResults(Connection con, String[] fields,
			String table, String[] wheres, String[] wherevalues);

	/**
	 * Builds and executes a general SQL query and returns all results 
	 * as strings.
	 * Very basic simplisitc method that will probably break if you
	 * try to do anything complicated
	 *  
	 * @param con
	 * @param fields
	 * @param table
	 * @param wheres
	 * @param wherevalues
	 * @return String matrix String[fields.length][ReturnedResults.size()]
	 */
	public void update(Connection con, String[] fields,
			String[] fieldvalues, String table, String[] wheres,
			String[] wherevalues);
	
	/**
	 * 
	 * @param con
	 * @return integer count of the table
	 */
	public int getTableCount(Connection con);
	
}
