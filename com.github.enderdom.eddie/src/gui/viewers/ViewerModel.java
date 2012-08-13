package gui.viewers;

import javax.swing.table.AbstractTableModel;


import gui.EddieGUI;


public abstract class ViewerModel extends AbstractTableModel{

	protected static final long serialVersionUID = 1L;
	protected String[] tableheadings;
	protected String[] actualheadings;
	protected boolean[] shown;
	protected static String whitespace = "___";
	protected EddieGUI gui;
	protected int colcount=0;
	protected int rowcount=0;
	protected String[][] cols;
	protected Viewer view;
	
	public ViewerModel(EddieGUI gui, Viewer view){
		this.gui = gui;
		this.view = view;
	}
	
	public void build(String data){
		
	}
	
	protected void load(){
		//Load data into model here
	}
	
	public int getColumnCount() {
		if(cols == null){
			return 0;
		}
		else{
			return cols.length;
		}
	}

	public int getRowCount() {
		if(cols == null){
			return 0;
		}
		else{
			return cols[0].length;
		}
	}
	
	public void addRow(String[] data){
		String[][] data2 = new String[cols.length][cols[0].length+1];
		int add =0;
		for(int i =0; i < cols.length; i++){
			for(int j = 0 ; j < cols[0].length; j++){
				data2[i][j] = cols[i][j];
			}
		}
		for(int i =0; i < cols.length; i++){
			if(!shown[i])add++;
			data2[i][cols[0].length] = data[i+add];
		}
		this.cols = data2;
	}
	
	public void removeRow(int rem){
		String[][] data2 = new String[cols.length][cols[0].length-1];
		int add =0;
		for(int i =0; i < cols.length; i++){
			for(int j = 0 ; j < cols[0].length-1; j++){
				if(j == rem){
					add=1;
				}
				data2[i][j] = cols[i][j+add];
			}
		}
		this.cols = data2;
	}

	public abstract void removeRows(int[] roz);
	
	
	public Object getValueAt(int row, int col) {
		if(cols == null){
			return "PLACE HOLDER";
		}
		else{
			return cols[col][row];
		}
	}
	

	public String getColumnName(int column){
		return actualheadings[column];
	}
	
	public boolean isCellEditable(int row, int col){ 
		 return false; 
	}

	public void saveFile(){
		
	}
	
}
