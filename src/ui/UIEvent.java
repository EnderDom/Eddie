package ui;

import java.util.EventObject;

public class UIEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6370129674298096704L;
	
	private String destinationclass;
	private String classdata;

	public UIEvent(Object source, String classname, String data) {
		super(source);
		addClassAndData(classname, data);
	}
	
	public void addClassAndData(String classname, String data){
		this.destinationclass = classname;
		this.classdata = data;
	}
	
	public String getDestination(){
		return this.destinationclass;
	}
	
	public String getData(){
		return this.classdata;
	}
}
