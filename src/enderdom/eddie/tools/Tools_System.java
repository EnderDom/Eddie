package enderdom.eddie.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.SystemUtils;

public abstract class Tools_System{

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_DATE_FORMAT = "dd-MM-yyyy";
    public static final String SQL_DATE_FORMAT2 = "yyyy-MM-dd";
    
	
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
    
    //lulz
    public static final Date getDateNowAsDate(){
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }
    
    public static int getDeltaInDaysToNow(String source){
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	Calendar now = Calendar.getInstance();
    	try {
			Date then = sdf.parse(source);
			return (int)((now.getTime().getTime()-then.getTime())/(1000*3600*24));//millis*minutes*days
		} 
    	catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
    }
    
    public static String long2DayHourMin(long l){
    	return String.format("%ddays %dhours %dmins",l/86400000, (l%86400000)/3600000, ((l%86400000)%3600000)/60000);
    }

    public static int getDeltaInHoursToNow(String source){
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	Calendar now = Calendar.getInstance();
    	try {
			Date then = sdf.parse(source);
			return (int)((now.getTime().getTime()-then.getTime())/(1000*3600));
		} 
    	catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
    }
    
    public static int getDeltaInMinutesToNow(String source){
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	Calendar now = Calendar.getInstance();
    	try {
			Date then = sdf.parse(source);
			return (int)((now.getTime().getTime()-then.getTime())/(1000*60));
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
    
    public static String getPathSeparator(){
    	return SystemUtils.FILE_SEPARATOR;
    }
    
    public static String getNewline(){
    	return SystemUtils.LINE_SEPARATOR;
    }
    
	public static int getCPUs(){
		return Runtime.getRuntime().availableProcessors();
	}
    
    /**
     * 
     * As both SQL date and util date can be 
     * initialised with a long representing time
     * from epoch. This should technically work and
     * long is signed so in theory there should be no 
     * issues about dates before 1970 (though I can't imagine
     * there being much biology data from that period)
     * 
     * 
     * @param d SQL Date object
     * @return java.util.Date object
     */
    public static java.util.Date sql2util(java.sql.Date d){
    	return new java.util.Date(d.getTime());
    }
    
    /**
     * @see util2sql
     * @param d
     * @return
     */
    public static java.sql.Date util2sql(java.util.Date d){
    	return new java.sql.Date(d.getTime());
    }
    
}
