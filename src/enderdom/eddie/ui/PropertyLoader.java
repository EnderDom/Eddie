package enderdom.eddie.ui;

import java.io.File;
import java.util.Properties;

public interface PropertyLoader {
	
	//NOTE current used keys
	//FULLVERSION, VERSION, GUIVERSION

	public String getValue(String key);
	
	public void setValue(String key, String value);

	public String getValueOrSet(String key, String defaultvalue);
	
	public Properties getPropertyObject();
	
	public void setPropertyObject(Properties props);
	
	public Properties loadPropertyFile(File filename);
	
	public Properties loadPropertyFile(File filename, Properties props);
	
	public boolean savePropertyFile(File filename, Properties props);
	
	public boolean savePropertyFile(String filename, Properties props);
	
	public int parseArgs(String[] args);

	public boolean isLogging();
}
