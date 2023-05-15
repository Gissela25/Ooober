package com.ooober.driver.utils

import android.app.Application
import android.content.Context
import com.ooober.driver.R
import java.text.SimpleDateFormat
import java.util.*


object RelativeTime : Application() {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(time: Long, ctx: Context?): String {
        var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return "Hace un momento"
        }

        // TODO: localize
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "${ctx!!.getString(R.string.txtOneMomentAgo)}"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "${ctx!!.getString(R.string.txtOneMinuteAgo)}"
        } else if (diff < 50 * MINUTE_MILLIS) {
            "" + diff / MINUTE_MILLIS + " ${ctx!!.getString(R.string.txtXMinutesAgo)}"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "${ctx!!.getString(R.string.txtOneHourAgo)}"
        } else if (diff < 24 * HOUR_MILLIS) {
            "" + diff / HOUR_MILLIS + " ${ctx!!.getString(R.string.txtHoursAgo)}"
        } else if (diff < 48 * HOUR_MILLIS) {
            ""+  ctx!!.getString(R.string.txtYesterday)
        } else {
            "" + diff / DAY_MILLIS +  " ${ctx!!.getString(R.string.txtDaysAgo)}"
        }
    }

    fun timeFormatAMPM(time: Long, ctx: Context?): String {
        var time = time
        val formatter = SimpleDateFormat("hh:mm a")
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return formatter.format(Date(time))
        }

        // TODO: localize
        val diff = now - time
        return if (diff < 24 * HOUR_MILLIS) {
            formatter.format(Date(time))
        } else if (diff < 48 * HOUR_MILLIS) {
            ""+  ctx!!.getString(R.string.txtYesterday)
        } else {
            "" + diff / DAY_MILLIS + " ${ctx!!.getString(R.string.txtDaysAgo)}"
        }
    }
}