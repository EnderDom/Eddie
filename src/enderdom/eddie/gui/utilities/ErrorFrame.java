package enderdom.eddie.gui.utilities;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import enderdom.eddie.tools.Tools_System;

public class ErrorFrame extends JInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1287884461722430565L;
	private Throwable t;
	private String[] altDetails;
	private String message;
	private JTextArea textArea;
	private JLabel mess;
	private JScrollPane pane1;
	
	public ErrorFrame(String message, Throwable t){
		super("An Error has occured", true, true, true, true);
		this.message = message;
		this.t = t;
		build();
	}
	
	public ErrorFrame(String message, String[] details){
		super("An Error has occured", true, true, true, true);
		this.message = message;
		this.altDetails = details;
		build();
	}
	
	public ErrorFrame(String message){
		super("An Error has occured", true, true, true, true);
		this.message = message;
		build();
	}
	
	public void build(){
		textArea = new JTextArea();
        mess = new JLabel();
        this.setPreferredSize(new Dimension(600,600));
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        textArea.setColumns(50);
        textArea.setLineWrap(true);
        textArea.setRows(20);
        textArea.setWrapStyleWord(false);
        textArea.setEditable(false);
        populateTextArea();
        pane1 = new JScrollPane(textArea);
        pane1.setPreferredSize(new Dimension(600,500));
        mess.setText("Error Message: " + this.message);
        
        this.setLayout(new BorderLayout());        
        this.add(mess, BorderLayout.PAGE_START);
        this.add(pane1, BorderLayout.CENTER);
        
		pack();
	}
	
	public void populateTextArea(){
		if(t != null){
			String s= Tools_System.getNewline();
			textArea.setText("----ERROR OUTPUT----"+s+s);
			textArea.append(t.getClass().getName()+ ": " + t.getMessage() + s + s);
			for(StackTraceElement e : t.getStackTrace()){
				textArea.append(e.toString() + s);
			}
		}
		else if(altDetails != null){
			String s= Tools_System.getNewline();
			textArea.setText("----ERROR INFORMATION----"+s+s);
			for(String e : altDetails){
				textArea.append(e.toString() + s);
			}
		}
		else{
			textArea.setText("No stack Trace, Non-JVM error");
		}
	}
	
}
