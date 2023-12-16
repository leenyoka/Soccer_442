package com.nyoka.soccer_442;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by linda.nyoka on 2015-04-14.
 */
public class DateTime {

    public DateTime()
    {

    }
    public OffSet OffSet()
    {
        try {


            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            String [] dateGMT = (dateFormatGmt.format(new Date())).split(":");
            String [] dateLocal = (dateFormat.format(Calendar.getInstance().getTime())).split(":");


            int hourDiff = Integer.parseInt(dateLocal[0]) - Integer.parseInt(dateGMT[0]);
            int minuteDiff = Integer.parseInt(dateLocal[1]) - Integer.parseInt(dateGMT[1]);

            return new OffSet(hourDiff,minuteDiff,0);
        }
        catch (Exception ex)
        {
            return new OffSet(0,0,0);
        }
    }

}
