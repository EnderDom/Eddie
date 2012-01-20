package gui;

import gui.utilities.SpringUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import tools.stringTools;

public class FileAdderer extends JInternalFrame implements ActionListener{

	/*
	 * Used to add files to something
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinkedList<JTextField> inputs = new LinkedList<JTextField>();
	EddieGUI parent;
	int row =2;
	int rowinit = row+1;
	JPanel p;
	private static FileNameExtensionFilter filter;
	private static String normalLabel = new String("Input File/Folder:");
	//private static FileNameExtensionFilter filter = new FileNameExtensionFilter( "fasta", "fna", "fast*", "qual", "fastq");
	JButton button1;
	JButton button2;
	JButton addFile;
	JLabel spacer;
	JLabel spacer2;
	JLabel spacer3;
	private int filesanddirectories = JFileChooser.FILES_AND_DIRECTORIES;

	public FileAdderer(EddieGUI parent){
		//Generic stuff
		super("Add Files", true, true, true, true);
		this.parent = parent;
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		setSize(300, 500);
		p = new JPanel(new SpringLayout());
		
		//First Line
		JLabel lable = new JLabel(normalLabel);
		JTextField field = getNewTextField();
		JButton browse = getBrowseButton();
		browse.setActionCommand("FILE_"+(row-rowinit));
		browse.addActionListener(this);
			//Add to panel
			p.add(lable);
			p.add(field);
			p.add(browse);
		
		//Mid
		addFile = new JButton("Add Another File...");
		addFile.setActionCommand("ADD");
		addFile.addActionListener(this);
		spacer2 = new JLabel("");
		spacer3 = new JLabel("");
		//Add to panel
			p.add(spacer2);
			p.add(addFile);
			p.add(spacer3);
		
		//Bottom
		button1 = new JButton("Save");
		button1.setActionCommand("LOAD");
		button2 = new JButton("Cancel");
		button2.setActionCommand("CANCEL");
		button1.addActionListener(this);
		button2.addActionListener(this);
		spacer = new JLabel("");
			//Add to panel
			p.add(button1);
			p.add(spacer);
			p.add(button2);
		
		/*
		 * Layout
		 */
		SpringUtilities.makeCompactGrid(p,
				row, 3, //rows, cols
				6, 6,        //initX, initY
	            10, 6);       //xPad, yPad
		p.setOpaque(true);
		
		/*
		 * Pack
		 */
		
		this.add(p);
		this.pack();
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().contentEquals("SAVE")){
			
		}
		else if(arg0.getActionCommand().contentEquals("CANCEL")){
			this.dispose();
		}
		else if(arg0.getActionCommand().contentEquals("ADD")){
			addRow(normalLabel);
		}
		else if(arg0.getActionCommand().indexOf("FILE_") != -1){
			JFileChooser chooser =new JFileChooser(parent.load.getWorkspace());
			chooser.setMultiSelectionEnabled(false);//TODO support this
			if(filter != null){
				chooser.setFileFilter(filter);
			}
			chooser.setFileSelectionMode( getFilesanddirectories() );
			int returnVal = chooser.showOpenDialog(parent);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				Logger.getRootLogger().debug("User selected file: " +   chooser.getSelectedFile().getName());
				int value = stringTools.parseString2Int(arg0.getActionCommand().substring(arg0.getActionCommand().indexOf("FILE_") + 5, arg0.getActionCommand().length()));
				if(value == -1) Logger.getRootLogger().error("Button Action Command is named illegally, should be File_{integer_value} ie 'File_1', but is " + arg0.getActionCommand());
				else inputs.get(value).setText(chooser.getSelectedFile().getPath());
			}
		}
		else{
			Logger.getRootLogger().warn("Action not picked up cmd: " + arg0.getActionCommand());
		}
	}
	
	public void addRow(String newlabel){
		
		/*
		 * Create new row to add an additional file
		 * 
		 * Gagh, this so much harder than js
		 */
		JLabel lable = new JLabel(newlabel);
		JTextField field = getNewTextField();
		JButton browse = getBrowseButton();
		browse.setActionCommand("FILE_"+(row-rowinit));
		browse.addActionListener(this);
		
		/*
		 * Temporary remove bottom row
		 * 
		 * Undoubtably a poor way to do it
		 * but to lazy to go into how SpringUtilities works
		 */
		for(int i =p.getComponentCount()-1; i > -1 ; i--){
			if(p.getComponent(i).equals(button1)){
				p.remove(i);
			}
			else if(p.getComponent(i).equals(button2)){
				p.remove(i);
			}
			else if(p.getComponent(i).equals(addFile)){
				p.remove(i);
			}
			else if(p.getComponent(i).equals(spacer)){
				p.remove(i);
			}
			else if(p.getComponent(i).equals(spacer2)){
				p.remove(i);
			}
			else if(p.getComponent(i).equals(spacer3)){
				p.remove(i);
			}
		}
		//Add new row
		p.add(lable);
		p.add(field);
		p.add(browse);
		
		//Re-add middle row
		p.add(spacer2);
		p.add(addFile);
		p.add(spacer3);
		
		//Re-add bottom row
		p.add(button1);
		p.add(spacer);
		p.add(button2);
		
		//Regrid
		SpringUtilities.makeCompactGrid(p,
				row, 3, //rows, cols
				6, 6,        //initX, initY
	            10, 6);       //xPad, yPad

		
		this.pack();
	}
	
	public static JButton getBrowseButton(){
		JButton browse = new JButton();
		try{
			browse.setIcon(UIManager.getIcon("FileChooser.directoryIcon"));
		}
		catch(Exception e){
			Logger.getRootLogger().trace("Missing File Chooser Icon..??");
			browse.setText("->");
		}
		return browse;
	}
	
	public JTextField getNewTextField(){
		JTextField field = new JTextField(30);
		field.setText(parent.load.getWorkspace());
		inputs.add(field);
		this.row++;
		return field;
	}

	public int getFilesanddirectories() {
		return filesanddirectories;
	}

	public void setFilesanddirectories(int filesanddirectories) {
		this.filesanddirectories = filesanddirectories;
	}
	
	
	
}
