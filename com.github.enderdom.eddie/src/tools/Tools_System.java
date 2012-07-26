package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.SystemUtils;

public abstract class Tools_System{

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_DATE_FORMAT = "dd-MM-yyyy";
    
	
    public static final String getDateNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    
    public static final String getDate(String format, Date d){
    	return new SimpleDateFormat(format).format(d);
    }
    
    public static final Date getDateFromString(String date, String format) throws ParseException{
    	return new SimpleDateFormat(format).parse(date);
    }
    
    public static final String getDateNow(String format){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
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
    	return SystemUtils.IS_OS_WINDOWS;
    }
    
    /**
     * Because I can never remeber what the System key is
     * @return the filepath separator character
     */
    public static String getFilepathSeparator(){
    	return SystemUtils.FILE_SEPARATOR;
    }
    
    public static String getNewline(){
    	return SystemUtils.LINE_SEPARATOR;
    }
    
}
