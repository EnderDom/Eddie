package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class Tools_System{

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
    public static final String getDateNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    
    public static int getDeltaInDaysToNow(String source){
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	Calendar now = Calendar.getInstance();
    	try {
			Date then = sdf.parse(source);
			return (int)((now.getTime().getTime()-then.getTime())/(3600*24*1000));
		} 
    	catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
    	
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
