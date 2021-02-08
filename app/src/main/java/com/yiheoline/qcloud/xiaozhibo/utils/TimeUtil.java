package com.yiheoline.qcloud.xiaozhibo.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TimeUtil {

    @SuppressLint("SimpleDateFormat")
    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getMinAndSecond(long time) {
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getYearAndMonth(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getYearMonthAndDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getMonthAndDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM月dd");
        return format.format(new Date(time));
    }
    @SuppressLint("SimpleDateFormat")
    public static String getMonthAndDay2(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd日");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateFormat(long time) {
        String timeString = getYearMonthAndDay(time);
        return timeString.replaceAll("-", ".");
    }

    @SuppressLint("SimpleDateFormat")
    public static String getYearMonthAndDayWithHour(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getMonthAndDayWithHour(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
        return format.format(new Date(time));
    }

    public static String convertDateTime2DateStr(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        return df.format(date);
    }

    public static boolean startTimUtll(Long start) {
        Date dt = new Date();//获取当前时间
        Long time = dt.getTime();
        final int liangtian = 172800000;
        if (time + liangtian > start) {
            return false;
        } else {
            return true;

        }

    }

    /**
     * 传入毫秒数 返回HH:mm:ss格式的字符串
     *
     * @param time
     */
    public static String castLastDate(long time) {

        long duration = System.currentTimeMillis() - time;

        if (duration < 2592000000l) {
            long tempweek = duration / 604800000l;
            if (tempweek > 0) {
                return tempweek + "周前";
            }
            long tempday = duration / 86400000l;
            if (tempday > 0) {
                return tempday + "天前";
            }
            long temphour = duration / 3600000l;
            if (temphour > 0) {
                return temphour + "小时前";
            }
            long tempminutes = duration / 60000l;
            if (tempminutes > 0) {
                return tempminutes + "分钟前";
            }
            long tempseconds = duration / 1000l;
            if (tempseconds > 0) {
                return tempseconds + "秒前";
            }
        } else {
            return getYearMonthAndDayWithHour(time);
        }
        return "";
    }

    /**
     * 返回活动日期格式
     */
    public static String getDateFormat(Long start, Long end) {
        String dateFormat = TimeUtil.getYearMonthAndDay(start)
                + " ~ " + TimeUtil.getMonthAndDay(end);

        return dateFormat.replace("-",".");
    }

    /**
     * 返回活动时间格式
     * 如果时间一样  则只返回一个
     */
    public static String getTimeFormat(Long start, Long end) {

        String startTime = TimeUtil.getMonthAndDayWithHour(start);

        String endTime = TimeUtil.getHourAndMin(end);

        if(start == end){
            return startTime;
        }

        return startTime + " - " + endTime;
    }

    /**
     * 调此方法输入所要转换的时间输入例如（"2014-06-14-16-09-00"）返回时间戳
     *
     * @param time
     * @return
     */
    public static long getLongTime(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA);
        Date date;
        long times = 0;
        try {
            date = sdr.parse(time);
            times = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }
    /**
     * 当前时间是否Deadline
     * 服务器返回的deadline 格式 是 mm:ss
     * @return
     */
    public static boolean isLaterTime(String time){
        Date date = new Date();
        int hour = date.getHours();
        int min = date.getMinutes();
        String[] strings = time.split(":");
        boolean isLater = false;
        if(strings.length == 2){
            int deadlineHour = Integer.valueOf(strings[0]);
            int deadlineMin = Integer.valueOf(strings[1]);
            if((hour == deadlineHour && min > deadlineMin) || hour > deadlineHour){
                isLater = true;
            }
        }
        return isLater;
    }

    /**
     * 是否隔天
     * @return
     */
    public static boolean isAnotherDay(long millis){
        long dayMillis = 24 * 60 * 60 * 1000;
        boolean isLater = false;
        if(System.currentTimeMillis() - millis > dayMillis){
            isLater = true;
        }
        return isLater;
    }

    /**
     * 判断是否是明日
     */
    public static boolean isBeforeDay(long millis){
        long dayMillis = 24 * 60 * 60 * 1000;
        String currentDay = getYearMonthAndDay(System.currentTimeMillis() + dayMillis);
        String dayString = getYearMonthAndDay(millis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(dayString);
            long ls = date.getTime();
            dayString = getYearMonthAndDay(ls);
        } catch (ParseException e) {
            return false;
        }
        return dayString.equals(currentDay);
    }

    public static boolean isWorking(String workDay,String className){
//        String day = getYearMonthAndDay(System.currentTimeMillis());
        if(TextUtils.isEmpty(workDay)||TextUtils.isEmpty(className)){
            return true;
        }
        String startDate;
        Long classMill = 8 * 60 * 60 * 1000L;
        if(className.equals("A")){
            startDate = workDay + " 09:00:00";
            classMill = 9 * 60 * 60 * 1000L;
        }else if(className.equals("B")){
            startDate = workDay + " 17:45:00";
        }else{
            startDate = workDay + " 01:30:00";
        }
        long sys = System.currentTimeMillis();
        long start = getLongTime(startDate);
        if(sys < start+classMill){
            return true;
        }

        return false;
    }

    /**
     * 传入秒数  返回时常 HH:mm:ss
     * seconds int 秒数
     */
    public static String getTimeLen(int seconds){
        StringBuffer stringBuffer = new StringBuffer();
        int hour = seconds/3600;
        if(hour != 0){
            if(hour < 10){
                stringBuffer.append("0");
            }
            stringBuffer.append(hour);
            stringBuffer.append(":");
        }
        int min = (seconds%3600)/60;
        if(min != 0){
            if(min < 10){
                stringBuffer.append("0");
            }
            stringBuffer.append(min);
            stringBuffer.append(":");
        }
        int sec = (seconds%3600%60);
        if(sec != 0){
            if(sec < 10){
                stringBuffer.append("0");
            }
            stringBuffer.append(sec);
        }
        return stringBuffer.toString();

    }
}
