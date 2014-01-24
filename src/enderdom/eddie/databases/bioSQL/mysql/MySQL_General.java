package enderdom.eddie.databases.bioSQL.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.bioSQL.interfaces.SQLGeneral;

public class MySQL_General implements SQLGeneral{

	private static Logger logger = Logger.getRootLogger();
	
	public int getTableCount(Connection con){
		try{
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery("SHOW TABLES");
			int i=0;
			while(set.next()){
				i++;
			}
			st.close();
			set.close();
			return i;
		}
		catch(SQLException sq){
			Logger.getRootLogger().warn("Failed to get table count" + sq);
			return -1;
		}
	}

	public String[][] getResults(Connection con, String[] fields,
			String table, String[] wheres, String[] wherevalues) {
		StringBuffer sql = new StringBuffer("SELECT ");
		for (int i = 0; i < fields.length; i++) {
			sql.append(fields[i]);
			if (i != fields.length - 1)
				sql.append(",");
			sql.append(" ");
		}
		sql.append("FROM ");
		sql.append(table);
		sql.append(" ");
		if (wheres != null) {
			if (wheres.length != wherevalues.length) {
				logger.warn("Where filter values array and filter array should be same but is not!");
			}
			sql.append("WHERE ");
			for (int i = 0; i < wheres.length; i++) {
				sql.append(wheres[i]);
				sql.append("=\"");
				sql.append(wherevalues[i]);
				sql.append("\" ");
				if (i != wheres.length - 1)
					sql.append("AND ");
			}
		}
		try {
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(sql.toString());
			logger.trace(sql.toString());
			int size = 0;
			while (set.next()) {
				size++;
			}
			String[][] result = new String[fields.length][size];
			set.first();
			int i = 0;
			do {
				for (int j = 0; j < fields.length; j++) {
					result[j][i] = (set.getObject(fields[j]) != null) ? set
							.getObject(fields[j]).toString() : "NULL";
				}
				i++;
			} while (set.next());
			set.close();
			st.close();
			return result;
		} catch (SQLException sq) {
			logger.error("Failed to run generic BioSQL query with " + sql, sq);
			return null;
		}
	}

	public void update(Connection con, String[] fields,
			String[] fieldvalues, String table, String[] wheres,
			String[] wherevalues) {
		StringBuffer sql = new StringBuffer("UPDATE " + table + " SET ");
		for (int i = 0; i < fields.length; i++) {
			sql.append(fields[i] + "=\"" + fieldvalues[i] + "\"");
			if (i != fields.length - 1) {
				sql.append(",");
			}
			sql.append(" ");
		}
		if (wheres != null) {
			sql.append("WHERE ");
			for (int i = 0; i < wheres.length; i++) {
				sql.append(wheres[i]);
				sql.append("=\"");
				sql.append(wherevalues[i]);
				sql.append("\" ");
				if (i != wheres.length - 1)
					sql.append("AND ");
			}
		}
		try {
			Statement st = con.createStatement();
			logger.trace(sql.toString());
			st.execute(sql.toString());
			st.close();
		} catch (SQLException sq) {
			logger.error(
					"Failed to run generic BioSQL update with "
							+ sql.toString(), sq);
		}
	}
}
