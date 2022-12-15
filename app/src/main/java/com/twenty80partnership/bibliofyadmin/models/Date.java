package com.twenty80partnership.bibliofyadmin.models;

import android.util.Log;

import java.util.ArrayList;

public class Date {
    ArrayList<Integer> monthsNumber;
    private ArrayList<String>months;
    Long longDate;
    String ampm;
    int day, month, year,hours,minutes, newDay,newMonth,newYear , oldDay, oldMonth,oldYear;
    String sDay,sMinutes,sHours,sDate;

    public ArrayList<Integer> getMonthsNumber() {
        return monthsNumber;
    }

    public void setMonthsNumber(ArrayList<Integer> monthsNumber) {
        this.monthsNumber = monthsNumber;
    }

    public ArrayList<String> getMonths() {
        return months;
    }

    public void setMonths(ArrayList<String> months) {
        this.months = months;
    }

    public Long getLongDate() {
        return longDate;
    }

    public void setLongDate(Long longDate) {
        this.longDate = longDate;
    }

    public String getAmpm() {
        return ampm;
    }

    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getNewDay() {
        return newDay;
    }

    public void setNewDay(int newDay) {
        this.newDay = newDay;
    }

    public int getNewMonth() {
        return newMonth;
    }

    public void setNewMonth(int newMonth) {
        this.newMonth = newMonth;
    }

    public int getNewYear() {
        return newYear;
    }

    public void setNewYear(int newYear) {
        this.newYear = newYear;
    }

    public int getOldDay() {
        return oldDay;
    }

    public void setOldDay(int oldDay) {
        this.oldDay = oldDay;
    }

    public int getOldMonth() {
        return oldMonth;
    }

    public void setOldMonth(int oldMonth) {
        this.oldMonth = oldMonth;
    }

    public int getOldYear() {
        return oldYear;
    }

    public void setOldYear(int oldYear) {
        this.oldYear = oldYear;
    }

    public String getsDay() {
        return sDay;
    }

    public void setsDay(String sDay) {
        this.sDay = sDay;
    }

    public String getsMinutes() {
        return sMinutes;
    }

    public void setsMinutes(String sMinutes) {
        this.sMinutes = sMinutes;
    }

    public String getsHours() {
        return sHours;
    }

    public void setsHours(String sHours) {
        this.sHours = sHours;
    }

    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public  Date(){
        months = new ArrayList<>();

        months.add("Jan");
        months.add("Feb");
        months.add("Mar");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("Aug");
        months.add("Sept");
        months.add("Oct");
        months.add("Nov");
        months.add("Dec");

        monthsNumber = new ArrayList<>();

        monthsNumber.add(31);
        monthsNumber.add(29);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);

    }

    public Long createAbsoluteDate(Long year, Long month, Long day) {
        year = year*10000;
        month = month*100;

        return year+month+day;
    }

    public String incrementDate(Long userTimeAdded,int incr){
        userTimeAdded = userTimeAdded/1000000000L;
        day = (int)(userTimeAdded  % 100);
        userTimeAdded = userTimeAdded/100;

        month = (int)(userTimeAdded%100);

        year = (int)(userTimeAdded/100);

        if ( (day + incr) > monthsNumber.get(month-1)){

            if (month==12){
                newYear = year+1;
                newMonth = 1;

                newDay = day - (31 - incr);
            }
            else {
                newYear = year;
                newMonth = month+1;
                newDay = incr -  (monthsNumber.get(month-1) - day) ;
            }
        }
        else{
            newDay = day + incr;
            newMonth = month;
            newYear = year;
        }

         return newDay+"-"+newMonth+"-"+newYear;
    }


    public int differnceBetweenDates(Long time1,Long time2){
        int day1,day2,month1,month2,year1,year2,diff;

        //for time1
        day1 = (int)(time1  % 100);

        time1 = time1/100;
        month1 = (int)(time1%100);

        year1 = (int)(time1/100);

        //for time2
        day2 = (int)(time2  % 100);
        time2 = time2/100;

        month2 = (int)(time2%100);

        year2 = (int)(time2/100);




        if (year2>year1){
            Log.d("difflog","y2>y1");
            //in different years
            diff = day2+ (31-day1);
        }
        else{
            //in same year
            if (month2>month1){
                Log.d("difflog","m2>m1");

                diff = day2+ (monthsNumber.get(month1-1)-day1);
            }
            else{
                Log.d("difflog","same month"+" d2:"+day2+" d1:"+day1);

                diff = day2 - day1;
            }
        }

        return diff;
    }

    public String decrementDate(Long userTimeAdded,int decr){
        userTimeAdded = userTimeAdded/1000000000L;
        day = (int)(userTimeAdded  % 100);
        userTimeAdded = userTimeAdded/100;

        month = (int)(userTimeAdded%100);

        year = (int)(userTimeAdded/100);

        if ( (day - decr) < 1){

            if (month==1){
                oldMonth = 12;

                oldDay = 31 - (decr-day);
            }
            else {
                oldYear = year;
                oldMonth = month-1;
                oldDay = monthsNumber.get(month-2) -  (decr - day) ;

            }
        }
        else{
            oldDay = day - decr;
            oldMonth = month;
            oldYear = year;
        }

        return newDay+"-"+newMonth+"-"+newYear;
    }

    public String convertLongDateIntoSplit(Long userTimeAdded) {

        userTimeAdded = userTimeAdded/100000L;

        minutes = (int)(userTimeAdded%100);
        userTimeAdded = userTimeAdded/100;

        hours =  (int) (userTimeAdded%100);
        userTimeAdded = userTimeAdded/100;

        ampm = "AM";
        if (hours>12L){
            ampm = "PM";
            hours = hours - 12;
        }
        day =  (int)(userTimeAdded  % 100);
        userTimeAdded = userTimeAdded/100;

        month = (int)(userTimeAdded%100);

        year = (int)(userTimeAdded/100);

        String sDay,sMinutes,sHours;

        if (day>=10) {
            sDay = "" + day;
        }
        else{
            sDay = "0" + day;
        }

        if (minutes>=10) {
            sMinutes = "" + minutes;
        }
        else{
            sMinutes = "0" + minutes;
        }

        if(hours==0){
            sHours = "00";
        }
        else{
            sHours = "" + hours;
        }

        sDate = months.get(month-1)+" "+sDay+", "+sHours+":"+sMinutes+" "+ampm;

        return sDate;
    }
}
