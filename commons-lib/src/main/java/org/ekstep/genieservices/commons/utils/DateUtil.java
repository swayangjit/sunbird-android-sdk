package org.ekstep.genieservices.commons.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    public static final int MILLISECONDS_IN_AN_HOUR = 3600000;
    public static final int MILLISECONDS_IN_A_MINUTE = 60000;
    public static final String DATE_TIME_AM_PM_FORMAT = "dd/MM/yyyy, hh:mma";
    public static final String TIME_ZONE_GMT = "GMT";
    public static final String DATE_FORMAT_EXPORT_CONTENT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
    public static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SZZZZZ";
    public static final String DATETIME_FORMAT_WITHOUTTIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String getCurrentTimestamp() {
        return format(getEpochTime(), DATETIME_FORMAT);
    }

    public static Date now() {
        return new Date();
    }

    public static Long getEpochTime() {
        return System.currentTimeMillis();
    }

    public static DateTime parse(String dateInString, String format) {
        return DateTimeFormat.forPattern(format).parseDateTime(dateInString);
    }

    public static Long getTime(String date) {
        DateTime dateTime = DateTime.parse(date);
        return dateTime.getMillis();
    }

    public static String format(Date date, String format) {
        return DateTimeFormat.forPattern(format).print(date.getTime());
    }

    public static String format(long dateTime, String format) {
        return format(new Date(dateTime), format);
    }

    public static Integer elapsedTimeTillNow(long time) {
        DateTime parseDateTime = new DateTime(time);
        DateTime now = DateTime.now();
        Seconds seconds = Seconds.secondsBetween(parseDateTime, now);
        return seconds.getSeconds();
    }

    public static Integer elapsedTimeTillNow(String dateInString) {
        DateTime parseDateTime = parse(dateInString, DATETIME_FORMAT);
        DateTime now = DateTime.now();
        Seconds seconds = Seconds.secondsBetween(parseDateTime, now);
        return seconds.getSeconds();
    }

    public static Long getMidnightEpochTime(int offsetDays) {
        DateTime today = new DateTime().withTimeAtStartOfDay();
        DateTime tomorrow = today.plusDays(offsetDays + 1).withTimeAtStartOfDay();
        return tomorrow.getMillis();
    }

    public static Long getTodayMidnightEpochTime() {
        return getMidnightEpochTime(0);
    }

    public static boolean isTodayWithin(String startDate, String endDate) {
        String today = format(new Date().getTime(), DATE_FORMAT);
        return isDateBetween(startDate, endDate, today);
    }

    public static boolean isDateBetween(String startDate, String endDate, String dateToCompare) {
        LocalDate date = parse(dateToCompare, DATE_FORMAT).toLocalDate();
        LocalDate startDateTime = (startDate == null) ? date : parse(startDate, DATE_FORMAT).toLocalDate();
        LocalDate endDateTime = (endDate == null) ? date : parse(endDate, DATE_FORMAT).toLocalDate();
        if (startDateTime.compareTo(LocalDate.now()) == 0 || endDateTime.compareTo(LocalDate.now()) == 0) {
            return true;
        }
        return date.isAfter(startDateTime) && date.isBefore(endDateTime);
    }

    public static Long getEpochDiff(long time) {
        Long length = 0L;
        if (time != 0L) {
            length = getEpochTime() - time;
        }
        return length;
    }

    public static String formatSecond(double totalSeconds) {
        long totalTimeSpent = Math.round(totalSeconds);
        long hours = totalTimeSpent / 3600;
        long minutes = (totalTimeSpent % 3600) / 60;
        long seconds = totalTimeSpent % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long getTimeDifferenceInHours(long startDate, long stopDate) {
        long diff = stopDate - startDate;
        long diffHours = diff / (60 * 60 * 1000);
        return diffHours;
    }

    public static long getTimeDifferenceInDays(long startDate, long stopDate) {
        long diff = stopDate - startDate;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return diffDays;
    }

    public static Long dateToEpoch(String dateInString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            sdf.parse(dateInString).getTime();
            return sdf.parse(dateInString).getTime();
        } catch (Exception ex) {
            return getEpochTime();
        }
    }

    public static String getFormattedDateWithTimeZone(String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_EXPORT_CONTENT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf.format(now());
    }


    public static long convertLocalTimeMillis(String dateTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT_WITHOUTTIMEZONE, Locale.US);
        Date date = dateFormat.parse(dateTime);
        return date.getTime();
    }

}
