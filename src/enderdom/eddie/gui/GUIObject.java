package enderdom.eddie.gui;

import java.awt.event.ActionEvent;

public interface GUIObject {

	public boolean hasAction(ActionEvent e);
	
	public boolean runAction(ActionEvent e);
	
	public String getObjectName();
	
}
