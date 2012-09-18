package enderdom.eddie.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.sun.java.swing.Painter;

public class DesktopPane extends JDesktopPane {

	Color back = Color.darkGray;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DesktopPane(){
		setBackground(back);
	}
	 
	public void resetBackground(){
		this.setVisible(false);
		this.setBackground(back);
		this.setVisible(true);
	}
	
	 public void updateUI() {
	        if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
	            UIDefaults map = new UIDefaults();
	            Painter<JComponent> painter = new Painter<JComponent>() {	
	                public void paint(Graphics2D g, JComponent c, int w, int h) {
	                    // file using normal desktop color
	                    g.setColor(back);
	                    g.fillRect(0, 0, w, h);
	                }
	            };
	            map.put("DesktopPane[Enabled].backgroundPainter", painter);
	            putClientProperty("Nimbus.Overrides", map);
	        }
	        super.updateUI();
	    }

}