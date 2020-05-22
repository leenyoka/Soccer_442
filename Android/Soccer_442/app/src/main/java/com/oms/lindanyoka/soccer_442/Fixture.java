package com.oms.lindanyoka.soccer_442;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class Fixture {

    public String HomeTeamName;
    public String AwayTeamName;
    public String FixtureDate;
    public String Stadium;
    public String Time;


    public Fixture(String home, String away, String date, String stadium, String time) {
        HomeTeamName = home;
        AwayTeamName = away;
        FixtureDate = date;
        Stadium = stadium;
        Time = time;
        FixtureDate = FixTimeToLocal(FixtureDate, Time);
    }

    public Fixture(String value) {
        String[] values = value.split("_");

        HomeTeamName = values[0];
        AwayTeamName = values[1];
        FixtureDate = values[2];
        Stadium = values[3];
        Time = values[4];
    }

    public String Compress() {
        String value = String.valueOf(HomeTeamName);
        value += "_" + String.valueOf(AwayTeamName);
        value += "_" + String.valueOf(FixtureDate);
        value += "_" + String.valueOf(Stadium);
        value += "_" + String.valueOf(Time);
        return value;
    }

    private String FixTimeToLocal(String date, String time) {
        OffSet offSet = new DateTime().OffSet();
        date = (date.substring(date.indexOf(" "))).trim();
        Calendar cal = Calendar.getInstance();

        int year = Integer.parseInt(date.substring(date.lastIndexOf(" ") + 1));
        int day = Integer.parseInt(date.substring(0, date.indexOf(" ")));
        int mont = GetMonth(date);
        int hour = Integer.parseInt(time.split(":")[0]);
        int min = Integer.parseInt(time.split(":")[1]);

        cal.set(year, mont, day);
        cal.set(Calendar.HOUR_OF_DAY, (hour - 2) + offSet.Hour);
        cal.set(Calendar.MINUTE, (min) + offSet.Minute);
        cal.set(Calendar.SECOND, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("E dd-MMM HH:mm");
        String answer = sdf.format(cal.getTime());
        return answer;
    }

    private int GetMonth(String value) {
        value = value.substring(0, value.lastIndexOf(" "));
        value = value.substring(value.indexOf(" "));
        value = value.trim().toLowerCase();

        String[] months = new String[]{"january", "february", "march", "april", "may",
                "june", "july", "august", "september", "october", "november", "december"};

        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(value))
                return i;
        }
        return -1;
    }
}
