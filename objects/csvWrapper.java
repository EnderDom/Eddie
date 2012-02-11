package objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import tools.Tools_File;

public class csvWrapper {

	File file;
	String[][] cells;	
	String delimiter;
	int rows;
	int columns;
	
	public csvWrapper(){

	}

	public int load(File file, String delimiter){
		this.file = file;
		this.delimiter = delimiter;
		return loadFile();
	}
	
	public void setDelimiter(String delimiter){
		this.delimiter = delimiter;
	}
	
	
	public int loadFile(){
		int err = 0;
		if(delimiter == null){
			System.out.println("Warning, Delimiter not set, setting to ','");
			delimiter = ",";
		}
		if(this.file.isFile()){
			try {
				System.out.println("Parsing CSV File");
				this.rows = Tools_File.countLines(file);
				System.out.println(this.rows+" Rows found in CSV");
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader in = new InputStreamReader(fis, "UTF-8");
				BufferedReader reader = new BufferedReader(in);
				String line = "";
				int count = 0;
				while((line = reader.readLine()) != null){
					if(count == rows){ //Count should always be < rows, else will get a index out of range error 
						System.out.println("An error has occured, cannot fit rows into matrix");
						err = -1;
						break;
					}
					String[] splits = line.split(delimiter);
					if(cells == null){
						this.columns = splits.length;
						cells = new String[columns][rows];
					}
					if(splits.length == columns){
						for(int i =0; i < splits.length; i++){
							cells[i][count] = splits[i];
						}
						
					}
					else if(splits.length == 1){
						System.out.println("Warning: No delimiter in CSV");
					}
					else if(line.length() == 0){
						System.out.println("Warning: Empty Line in CSV");
					}
					else{
						System.out.println("Error: Malformed CSV, offending Line: "+line);
						break;
					}
					
					count++;
				}
				System.out.println("Completed Parse");
				err = 1;
				reader.close();
				in.close();
				fis.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			System.out.println("Error, CSV file "+file.getName()+" not a file ");
			err = -1;
		}
		return err;
	}
	
	public String getCell(int x, int y){
		return cells[x][y];
	}
	public String[] getRow(int x){
		return cells[x];
	}
	public String[] getColumn(int x){
		String[] column = new String[rows];
		for(int i =0 ; i < rows;i++){
			column[i] = cells[x][i];
		}
		return column;
	}
	
	public int getColumnInt(String columnheader){
		int columni = -1;
		for(int i =0; i < columns; i++){
			if(cells[i][0].toLowerCase().contentEquals(columnheader.toLowerCase())){
				columni = i;
				break;
			}
		}
		return columni;
	}
	
	public String[] getColumn(String columnheader){
		String[] column = new String[rows-1];
		int columni = getColumnInt(columnheader);
		if(columni > -1){
			for(int i =0 ; i < rows;i++){
				column[i] = cells[columni][i];
			}
		}
		else{
			System.out.println("Error, could not find columnheader "+ columnheader + " in csv file");
		}
		return column;
	}
	
}
