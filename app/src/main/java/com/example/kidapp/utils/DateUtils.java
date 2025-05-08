package com.example.kidapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static long getCurrentDate(){
        return new Date().getTime();
    }
    public static String dateToString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
}
