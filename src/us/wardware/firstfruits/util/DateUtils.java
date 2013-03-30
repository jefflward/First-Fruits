package us.wardware.firstfruits.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    public static Date trim(Date date)
    {
        final Calendar cal = Calendar.getInstance();
        cal.clear(); // as per BalusC comment.
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean areEqualDateStrings(String first, String second)
    {
        final Date date1 = dateStringToDate(first);
        final Date date2 = dateStringToDate(second);
        
        if (date1 != null) {
            return date1.equals(date2);
        }
        
        return false;
    }
    
    public static Date dateStringToDate(String dateString)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
