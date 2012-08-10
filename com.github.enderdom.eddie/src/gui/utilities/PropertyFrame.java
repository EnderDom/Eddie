package gui.utilities;

import gui.EddieGUI;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

import tools.Tools_String;

public class PropertyFrame extends JInternalFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6366855734337595394L;
	private static String title;
	private JTextField[] fields;
	public static String props_save = "_PROPS_SAVE";
	public static String props_close = "_PROPS_CLOSE";
	public static int fieldswidth = 50;
	
	/**
	 * 
	 */
	public PropertyFrame(){super("General Properties", true, true, true, true);}
	
	/**
	 * 
	 * 
	 * @param modulename Action command name for indentification of actions
	 * @param gui EddieGUI 
	 * @param labels a 3*X matrix, with [0][x] holding label, [1][x] holding the intial
	 * test to go in the JTextField and [2][x] the tooltip
	 * @return a array of 2 strings the first is the action command for saving, the second for closing
	 */
	public String[] build(String modulename, EddieGUI gui, String[][] labels){
		this.setTitle(title);
		int num = 0;
		JPanel p = new JPanel(new SpringLayout());
		fields = new JTextField[labels[0].length];
		for(int i= 0 ; i< labels[0].length; i++){
			JLabel l = new JLabel(Tools_String.capitalize(labels[0][i]), JLabel.TRAILING);
            p.add(l);
            JTextField textField = new JTextField(fieldswidth);
            textField.setText(labels[1][i]);
            textField.setToolTipText(labels[2][i]);
            l.setLabelFor(textField);
            p.add(textField);
            fields[i] = textField;
            Logger.getRootLogger().trace("Added Properties " + labels[0][i] + " to Panel as No. " + num);
            num++;
		}
		JButton button1 = new JButton("Save");
		JButton button2 = new JButton("Cancel");
		button1.setActionCommand(modulename+props_save);
		gui.addAction(modulename+props_save,modulename);
		gui.addAction(modulename+props_close,modulename);
		button2.setActionCommand(modulename+props_close);
		button1.addActionListener(gui);
		button2.addActionListener(gui);
		
		p.add(button1);
		p.add(button2);
		num++;
		//Lay out the panel.
		
		SpringUtilities.makeCompactGrid(p,
				num, 2, //rows, cols
				6, 6,        //initX, initY
	            6, 6);       //xPad, yPad
		p.setOpaque(true);
		this.setContentPane(p);
		this.pack();
		return new String[]{modulename+props_save,modulename+props_close};
	}
	
	public String getInput(int i){
		return this.fields[i].getText();
	}

}
