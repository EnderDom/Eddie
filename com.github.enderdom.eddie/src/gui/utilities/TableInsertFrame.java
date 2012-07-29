package gui.utilities;

import gui.EddieGUI;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JInternalFrame;

import tools.Tools_SQL;
import tools.Tools_String;

import databases.manager.DatabaseManager;


public class TableInsertFrame extends JInternalFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3476543126812850836L;
	private DatabaseManager manager;
	
	
	public TableInsertFrame(String insertMessage, EddieGUI gui, String preparedQuery, String[] autopopulate){
		super(insertMessage, true,true,true,true);
		manager = gui.getDatabaseManager();
		try{
			PreparedStatement st = manager.getCon().prepareStatement(preparedQuery);
			int count = Tools_String.count(preparedQuery, '?');
			String[] values = Tools_SQL.stripInsertFields(preparedQuery, manager.getDBTYPE());
			String table = Tools_SQL.stripTableName(preparedQuery, manager.getDBTYPE());
			for(String v : values )System.out.print(v +" " );
			System.out.println("\nIN: "+table);
		}
		catch(SQLException exe){
			gui.error("Failed to setup Database Table Insert Frame", exe);
		}
	}
}
