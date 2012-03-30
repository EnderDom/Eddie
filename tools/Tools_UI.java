package tools;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JInternalFrame;

import org.apache.log4j.Logger;

import ui.TaskManager;

public abstract class Tools_UI {
	
	//Gets available look and feels
	public static String[] getInstalledLnFs(){
		UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		String[] str = new String[lafInfo.length];
		for(int i=0;i<str.length;i++)str[i] = lafInfo[i].getClassName();
		return str;
	}
		
	//Changes look and feel of current swing containers 
	public static boolean changeLnF(String lnf, JFrame frame){
		boolean changed = false;
		
		try {
			UIManager.setLookAndFeel(lnf);
			changed = true;
		} catch (ClassNotFoundException e) {
			changed = false;
		} catch (InstantiationException e) {
			changed = false;
		} catch (IllegalAccessException e) {
			changed = false;
		} catch (UnsupportedLookAndFeelException e) {
			changed = false;
		}

		if(changed){//If Changed repack Frame to make change visible
			if(frame.isVisible()){
				SwingUtilities.updateComponentTreeUI(frame);
				/*
				 * Changing from visible to invisible 
				 * stops weird graphics corruption that happens
				 * on the lab iMac / jre 1.5 when LNF changed
				 */
				frame.setVisible(false);
				frame.pack();
				frame.repaint();
				frame.setVisible(true);
			}
		}
		return changed;
	}
	
	public static String getLookAndFeel(){
		try{
			return UIManager.getLookAndFeel().getClass().toString().split(" ")[1];
		}
		catch(Exception e){
			return new String("");
		}
	}
	
	public static JInternalFrame getGenericPropertiesMenu(){
		JInternalFrame frame = new JInternalFrame("Properties", true, true, true, true);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.setSize(300, 500);
		return frame;
	}
	
	public static TaskManager buildTaskManager(int core, int auxil){
		Logger.getRootLogger().debug("Building Task Manager");
		if(core < 0)core = 1;
		if(auxil < 5)auxil = 5;
		return new TaskManager(core, auxil);
	}
	
	
}
