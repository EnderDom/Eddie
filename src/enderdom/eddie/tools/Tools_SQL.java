package enderdom.eddie.tools;

import org.apache.log4j.Logger;

public class Tools_SQL {

	public static String[] stripInsertFields(String sql, String dbtype){
		if(dbtype.equals("mysql") || dbtype.equals("postgresql")){
			int start = -1;
			int end = -1;
			if((start=sql.indexOf("INTO")) != -1){
				if((end = sql.indexOf("VALUES")) != -1){
					start = sql.indexOf('(', start) + 1; 
					String sub = sql.substring(start, end);
					String[] fields = sub.split(",");
					for(int i =0; i < fields.length; i++){
						fields[i] = fields[i].replaceAll("\\(", "");
						fields[i] = fields[i].replaceAll("\\)", "");
						fields[i] = fields[i].replaceAll("'", "");
						fields[i] = fields[i].replaceAll("\"", "");
						fields[i]=fields[i].trim();
					}
					return fields;
				}
				else{
					Logger.getRootLogger().error("Failed to recognise sql as SQL INSERT statement");
					return null;
				}
			}
			else{
				Logger.getRootLogger().error("Failed to recognise sql as SQL INSERT statement");
				return null;
			}
		}
		else{
			Logger.getRootLogger().error("Database type not set or not recognised");
			return null;
		}
	}
	
	public static String stripTableName(String sql, String dbtype){
		if(dbtype.equals("mysql") || dbtype.equals("postgresql")){
			int s = sql.indexOf("INTO")+4;
			String sho = sql.substring(s).trim();
			int st=0;
			while(sho.charAt(st) != ' ' && sho.charAt(st) != '(' && st < sho.length())st++;
			return sho.substring(0, st);
		}
		else{
			Logger.getRootLogger().error("Database type not set or not recognised");
			return null;
		}
	}
	
	
}
