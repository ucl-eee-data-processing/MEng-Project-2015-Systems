package com.cloudera.oryx.lazarus.speed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class TimeProcessor {

    int hour_of_day;
    int minute_of_day;

    public TimeProcessor(int hour, int minute) {

        this.hour_of_day = 11;
        this.minute_of_day = minute;

    }

    public void setTime(int hour, int minute) {
        this.hour_of_day = hour;
        this.minute_of_day = minute;
    }

    /**
     * empty constructor
     */
    public TimeProcessor() {

    }

    static Calendar cal = new GregorianCalendar();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-HH:mm");

    public static double timeToDouble(String time) {

        // need to check if it's in the true format
        double timeInDouble = 0;

        try {
            Date timeOfData = formatter.parse(time);
            timeInDouble = timeOfData.getTime() / 1000;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return timeInDouble;
    }

    public static String longToString(long timeInLong) {

        @SuppressWarnings("deprecation")
        Date date = new Date(timeInLong);

        String timeInString = formatter.format(date);

        return timeInString;
    }

    public static String doubleToString(double timeInDouble) {

        long timeInLong = (long) timeInDouble * 1000; // as we divided earlier
        // by 1000 to convert to
        // double
        @SuppressWarnings("deprecation")
        Date date = new Date(timeInLong);

        String timeInString = formatter.format(date);

        return timeInString;

    }

    public static List<Double> timeListToDouble(List<String> stringList) { // need to
        // make
        // these
        // more
        // clear

        List<Double> timeList = new ArrayList<Double>();

        for (Iterator<String> iter = stringList.iterator(); iter.hasNext();) {

            timeList.add(timeToDouble(iter.next()));
        }

        return timeList;
    }

    public boolean isDayOfWeek(String time, int dayOfWeek) {

        boolean day = false;

        try {
            Date timeOfData = formatter.parse(time);
            cal.setTime(timeOfData);

            if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                day = true;
            } else {
                day = false;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return day;

    }

    public static boolean isWeekday(String time) {

        boolean weekday = true;

        try {
            Date timeOfData = formatter.parse(time);
            cal.setTime(timeOfData);

            if (cal.get(Calendar.DAY_OF_WEEK) == 1 || (cal.get(Calendar.DAY_OF_WEEK) == 7)) {

                weekday = false;
            } else {
                weekday = true;

            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return weekday;
    }

    public static boolean isTimeOfDay(String time, int hour, int minute) {

        boolean hhmm = false;

        try {
            Date timeOfData = formatter.parse(time);
            cal.setTime(timeOfData);

            if (cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.MINUTE) == minute) {
                hhmm = true;
            } else {
                hhmm = false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return hhmm;

    }

    public Double getDayofWeek(String time) {
        double timeInDouble = 0;
        try {

            Date timeOfData = formatter.parse(time);
            cal.setTime(timeOfData);
            int x = cal.get(Calendar.DAY_OF_WEEK);

            timeInDouble = (double) x;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return timeInDouble;
    }

    public Double timeToMinutes(String time) {
        double result = 0;
        try {

            Date timeOfData = formatter.parse(time);
            cal.setTime(timeOfData);

            double x1 = (double) cal.get(Calendar.MINUTE);
            double x2 = (double) cal.get(Calendar.HOUR) * 60;

            result = x1 + x2;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getTimeOfDay(double time) {

        String timeOfDay = null;
        try {
            Date timeOfData = formatter.parse(doubleToString(time));
            cal.setTime(timeOfData);
            String hours = new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString();
            String minutes = new Integer(cal.get(Calendar.MINUTE)).toString();
            if (hours.length() != 2){
                hours = "0" + hours;
            }
            if (minutes.length() != 2){
                minutes = minutes + "0";
            }
            
            timeOfDay = hours + ":" + minutes;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return timeOfDay;
    }

}
