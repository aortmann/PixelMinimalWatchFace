package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import java.util.*

object ComplicationColorsProvider {

    fun getDefaultComplicationColors(context: Context): ComplicationColors {
        val leftColor = ContextCompat.getColor(context, R.color.complication_default_left_color)
        val rightColor = ContextCompat.getColor(context, R.color.complication_default_right_color)

        return ComplicationColors(leftColor, rightColor, true)
    }

    fun getColorOptionsDataSet(context: Context): List<ComplicationColors> {
        return listOf(
            getDefaultComplicationColors(context),
            ComplicationColors(Color.parseColor("#FFFFFF")), // White
            ComplicationColors(Color.parseColor("#FFEB3B")), // Yellow
            ComplicationColors(Color.parseColor("#FFC107")), // Amber
            ComplicationColors(Color.parseColor("#FF9800")), // Orange
            ComplicationColors(Color.parseColor("#FF5722")), // Deep Orange
            ComplicationColors(Color.parseColor("#F44336")), // Red
            ComplicationColors(Color.parseColor("#E91E63")), // Pink
            ComplicationColors(Color.parseColor("#9C27B0")), // Purple
            ComplicationColors(Color.parseColor("#673AB7")), // Deep Purple
            ComplicationColors(Color.parseColor("#3F51B5")), // Indigo
            ComplicationColors(Color.parseColor("#2196F3")), // Blue
            ComplicationColors(Color.parseColor("#03A9F4")), // Light Blue
            ComplicationColors(Color.parseColor("#00BCD4")), // Cyan
            ComplicationColors(Color.parseColor("#009688")), // Teal
            ComplicationColors(Color.parseColor("#4CAF50")), // Green
            ComplicationColors(Color.parseColor("#8BC34A")), // Lime Green
            ComplicationColors(Color.parseColor("#CDDC39")), // Lime
            ComplicationColors(Color.parseColor("#607D8B")), // Blue Grey
            ComplicationColors(Color.parseColor("#9E9E9E")), // Grey
            ComplicationColors(Color.parseColor("#795548")) // Brown
        )
    }
}