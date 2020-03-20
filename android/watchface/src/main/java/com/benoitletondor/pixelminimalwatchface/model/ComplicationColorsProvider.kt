/*
 *   Copyright 2020 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.benoitletondor.pixelminimalwatchface.R

object ComplicationColorsProvider {

    fun getDefaultComplicationColors(context: Context): ComplicationColors {
        val leftColor = ContextCompat.getColor(context, R.color.complication_default_left_color)
        val middleColor = ContextCompat.getColor(context, R.color.complication_default_middle_color)
        val rightColor = ContextCompat.getColor(context, R.color.complication_default_right_color)
        val bottomColor = ContextCompat.getColor(context, R.color.complication_default_bottom_color)

        return ComplicationColors(
            leftColor,
            middleColor,
            rightColor,
            bottomColor,
            context.getString(R.string.color_default),
            true
        )
    }

    fun getAllComplicationColors(context: Context): List<ComplicationColors> {
        return listOf(
            getDefaultComplicationColors(
                context
            ),
            ComplicationColors(
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.color_white)
            ), // White
            ComplicationColors(
                Color.parseColor("#FFEB3B"),
                context.getString(R.string.color_yellow)
            ), // Yellow
            ComplicationColors(
                Color.parseColor("#FFC107"),
                context.getString(R.string.color_amber)
            ), // Amber
            ComplicationColors(
                Color.parseColor("#FF9800"),
                context.getString(R.string.color_orange)
            ), // Orange
            ComplicationColors(
                Color.parseColor("#FF5722"),
                context.getString(R.string.color_deep_orange)
            ), // Deep Orange
            ComplicationColors(
                Color.parseColor("#F44336"),
                context.getString(R.string.color_red)
            ), // Red
            ComplicationColors(
                Color.parseColor("#E91E63"),
                context.getString(R.string.color_pink)
            ), // Pink
            ComplicationColors(
                Color.parseColor("#9C27B0"),
                context.getString(R.string.color_purple)
            ), // Purple
            ComplicationColors(
                Color.parseColor("#673AB7"),
                context.getString(R.string.color_deep_purple)
            ), // Deep Purple
            ComplicationColors(
                Color.parseColor("#3F51B5"),
                context.getString(R.string.color_indigo)
            ), // Indigo
            ComplicationColors(
                Color.parseColor("#2196F3"),
                context.getString(R.string.color_blue)
            ), // Blue
            ComplicationColors(
                Color.parseColor("#03A9F4"),
                context.getString(R.string.color_light_blue)
            ), // Light Blue
            ComplicationColors(
                Color.parseColor("#00BCD4"),
                context.getString(R.string.color_cyan)
            ), // Cyan
            ComplicationColors(
                Color.parseColor("#009688"),
                context.getString(R.string.color_teal)
            ), // Teal
            ComplicationColors(
                Color.parseColor("#4CAF50"),
                context.getString(R.string.color_green)
            ), // Green
            ComplicationColors(
                Color.parseColor("#8BC34A"),
                context.getString(R.string.color_lime_green)
            ), // Lime Green
            ComplicationColors(
                Color.parseColor("#CDDC39"),
                context.getString(R.string.color_lime)
            ), // Lime
            ComplicationColors(
                Color.parseColor("#607D8B"),
                context.getString(R.string.color_blue_grey)
            ), // Blue Grey
            ComplicationColors(
                Color.parseColor("#9E9E9E"),
                context.getString(R.string.color_grey)
            ), // Grey
            ComplicationColors(
                Color.parseColor("#795548"),
                context.getString(R.string.color_brown)
            ) // Brown
        )
    }
}