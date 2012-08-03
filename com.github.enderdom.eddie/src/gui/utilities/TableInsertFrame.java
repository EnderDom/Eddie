package gui.utilities;

import gui.EddieGUI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import tools.Tools_SQL;
import tools.Tools_String;

import databases.manager.DatabaseManager;


public class TableInsertFrame extends JInternalFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3476543126812850836L;
	private DatabaseManager manager;
	private LinkedList<JTextField> field_values;
	private String[] fields;
	private Logger logger = Logger.getRootLogger();
	
	public TableInsertFrame(String insertMessage, EddieGUI gui, String preparedQuery, boolean[] autopopulate, String[] tooltips){
		super(insertMessage, true,true,true,true);
		manager = gui.getDatabaseManager();
		try{
			PreparedStatement st = manager.getCon().prepareStatement(preparedQuery);
			//int count = Tools_String.count(preparedQuery, '?');
			fields = Tools_SQL.stripInsertFields(preparedQuery, manager.getDBTYPE());
			String table = Tools_SQL.stripTableName(preparedQuery, manager.getDBTYPE());
			for(String v : fields )System.out.print(v +" " );
			System.out.println("\nIN: "+table);
			for(int i =0; i < fields.length; i++){
				JLabel label = new JLabel(Tools_String.capitalize(fields[i]));
				if(autopopulate.length < i && autopopulate[i]){
					populate(fields[i], table);
				}
				else{
					JTextField field = new JTextField(30);
					
					field_values.add(field);
				}
			}
		}
		catch(SQLException exe){
			gui.error("Failed to setup Database Table Insert Frame", exe);
		}
	}
	
	private void populate(String field, String table){
		String[] test = new String[]{"test 2","test 3","test 1","test 4","test 5",};
		ArrayList<String> list = new ArrayList<String>(30);
		try{
			Statement st = manager.getCon().createStatement();
			ResultSet set = st.execute("SELECT DISTINCT("+ field+") FROM "+table+ " LIMIT 0,30");
			while(set.next()){
				
			}
			Java2sAutoTextField field = new Java2sAutoTextField();
		}
		catch(SQLException sq){
			logger.error("Failed to gather autocomplete data");
		}
	}
	
}
