package com.singapore.trafficcamera.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateTimeUtils {

    val formatNowYear = SimpleDateFormat("yyyy")
    val formatNowMonth = SimpleDateFormat("MM")
    val formatNowDay = SimpleDateFormat("dd")
    val formatHourlyAndMinutes = SimpleDateFormat("HH:mm:ss")

    @SuppressLint("SimpleDateFormat")
    fun getApiDateTimeFormat(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return sdf.format(getCurrentTimeInMillis())
    }

    fun getSearchDateTimeFormatForApi(year: Int, month: Int, date: Int): String {
        var searchMonth = month.toString()
        var searchDate = date.toString()

        if (month < 10) {
            searchMonth = "0$searchMonth"
        }
        if (date < 10) {
            searchDate = "0$searchDate"
        }
        val dateTimeFormat = StringBuilder()
            .append(year)
            .append("-")
            .append(searchMonth)
            .append("-")
            .append(searchDate)
            .append("T")
            .append(getCurrentTime())

        return dateTimeFormat.toString()
    }

    fun getCurrentTimeInMillis(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT")).timeInMillis
    }

    fun getCurrentYear(): Int {
        formatNowYear.timeZone = TimeZone.getTimeZone("UTC")
        return formatNowYear.format(getCurrentTimeInMillis()).toInt()
    }

    fun getCurrentMonth(): Int {
        formatNowMonth.timeZone = TimeZone.getTimeZone("UTC")
        return formatNowMonth.format(getCurrentTimeInMillis()).toInt()
    }

    fun getCurrentDate(): Int {
        formatNowDay.timeZone = TimeZone.getTimeZone("UTC")
        return formatNowDay.format(getCurrentTimeInMillis()).toInt()
    }

    fun checkingIsTodayDate(year: Int, month: Int, date: Int): Boolean {
        if (year == getCurrentYear() && month == getCurrentMonth() && date == getCurrentDate()) {
            return true
        }
        return false
    }

    private fun getCurrentTime(): String {
        return formatHourlyAndMinutes.format(getCurrentTimeInMillis())
    }
}