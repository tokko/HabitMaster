package com.tokko.Util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.Date;

public class TimeUtils {
    private static DateTime currentTime;

    public static void setCurrentTime(DateTime dateTime){
        currentTime = dateTime;
    }
    public static DateTime getCurrentTime(){
        if(currentTime == null) return new DateTime();
        return currentTime;
    }

    public static int extractHours(long time) {
        return (int) (time / DateTimeConstants.MILLIS_PER_HOUR);
    }

    public static int extractMinutes(long time) {
        return (int) ((time % DateTimeConstants.MILLIS_PER_HOUR) / DateTimeConstants.MILLIS_PER_MINUTE);
    }

    public static long timeToLong(String time){
        String[] split = time.split(":");
        return Integer.valueOf(split[0])*DateTimeConstants.MILLIS_PER_HOUR + Integer.valueOf(split[1])*DateTimeConstants.MILLIS_PER_MINUTE;
    }
}
