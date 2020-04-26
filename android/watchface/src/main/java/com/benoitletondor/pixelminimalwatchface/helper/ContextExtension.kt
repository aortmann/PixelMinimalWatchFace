package com.benoitletondor.pixelminimalwatchface.helper

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Context.isScreenRound() = resources.configuration.isScreenRound