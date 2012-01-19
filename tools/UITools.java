package tools;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JInternalFrame;

public class UITools {
	
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
			SwingUtilities.updateComponentTreeUI(frame);
			frame.pack();
			frame.repaint();
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
}
