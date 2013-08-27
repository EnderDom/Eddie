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
	
	public boolean checkPropertiesAndLoadFile(String path);
	
	public boolean checkPropertiesAndLoadFile(File file);
	
	public Properties loadPropertyFile(File file, Properties prop, boolean isLogging);
	
	public Properties loadPropertyFile(String filepath, boolean isLogging);
	
	public boolean savePropertyFile(File filename, Properties props);
	
	public boolean savePropertyFile(String filename, Properties props);
	
	public String getPropertyFilePath();
	
	public void parseArgs(String[] args);

	public boolean isLogging();
}
