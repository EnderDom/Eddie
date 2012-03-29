package tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Tools_System{

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
    public static final String getDateNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    public static boolean isWindows(){
    	String osName = System.getProperty("os.name" );
    	if(osName.indexOf("Windows") != -1){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    /**
     * Because I can never remeber what the System key is
     * @return the filepath separator character
     */
    public static String getFilepathSeparator(){
    	return System.getProperty("file.separator");
    }
    
    public static String getNewline(){
    	return System.getProperty("line.separator");
    }
    

}
