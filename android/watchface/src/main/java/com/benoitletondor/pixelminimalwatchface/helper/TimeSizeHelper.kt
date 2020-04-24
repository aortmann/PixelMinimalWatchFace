package com.benoitletondor.pixelminimalwatchface.helper

import android.content.Context
import com.benoitletondor.pixelminimalwatchface.R

const val DEFAULT_TIME_SIZE = 50

fun timeSizeToScaleFactor(timeSize: Int): Float {
    return when(timeSize) {
        0 -> 0.80f
        25 -> 0.90f
        50 -> 1f
        75 -> 1.10f
        100 -> 1.20f
        else -> 1f
    }
}

fun Context.timeSizeToHumanReadableString(timeSize: Int): String {
    return when(timeSize) {
        0 -> getString(R.string.time_size_0)
        25 -> getString(R.string.time_size_25)
        50 -> getString(R.string.time_size_50)
        75 -> getString(R.string.time_size_75)
        100 -> getString(R.string.time_size_100)
        else -> getString(R.string.time_size_50)
    }
}