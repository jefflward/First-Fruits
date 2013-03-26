package us.wardware.firstfruits.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    public static Date trim(Date date) 
    {
        final Calendar cal = Calendar.getInstance();
        cal.clear(); // as per BalusC comment.
        cal.setTime( date );
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
   }
}
