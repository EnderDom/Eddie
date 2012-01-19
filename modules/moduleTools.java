package modules;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;


public class moduleTools {

	public static String menudivider = ">";
	
	public static JMenuBar add2JMenuBar(JMenuBar bar, JMenuItem item, String location){
		String[] split = location.split(menudivider);
		JMenu menu = null;
		boolean topmenu = false;
		for(int i =0; i < bar.getMenuCount(); i++){
			if(bar.getMenu(i).getText().contentEquals(split[0])){
				menu = bar.getMenu(i);
				topmenu = true;
			}
		}
		if(!topmenu){
			menu = new JMenu(split[0]);
			bar.add(menu);
			Logger.getRootLogger().debug("Built and Added Menu "+ split[0]);
		}
		for(int i =1; i < split.length; i++){
			boolean found = false;
			for(int j = 0; j < menu.getItemCount();j++){
				if(menu.getItem(j).getText().contentEquals(split[i])){
					found = true;
					menu = (JMenu)menu.getItem(j);
				}
			}
			if(!found){
				JMenu menu2 = new JMenu(split[i]);
				menu.add(menu2);
				menu = menu2;
			}
		}
		menu.add(item);
		Logger.getRootLogger().debug("Built and Added MenuItem "+ item.getText());
		return bar;
	}
	
	public static boolean ownsThisAction(String[] actions, String s) {
		boolean owner = false;
		for(int i =0 ; i < actions.length; i++){
			if(actions[i].startsWith(s)){
				owner = true;
				Logger.getRootLogger().debug("Action "+ s + " has owner");
			}
		}
		return owner;
	}
}
