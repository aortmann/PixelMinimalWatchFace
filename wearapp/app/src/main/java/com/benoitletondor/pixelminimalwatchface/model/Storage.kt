package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.content.SharedPreferences

private const val SHARED_PREFERENCES_NAME = "pixelMinimalSharedPref"

private const val DEFAULT_COMPLICATION_COLOR = -147282
private const val KEY_COMPLICATION_COLORS = "complicationColors"

interface Storage {
    fun init(context: Context)
    fun getComplicationColors(): ComplicationColors
    fun setComplicationColors(complicationColors: ComplicationColors)
}

class StorageImpl : Storage {
    private lateinit var appContext: Context
    private lateinit var sharedPreferences: SharedPreferences

    override fun init(context: Context) {
        appContext = context.applicationContext
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun getComplicationColors(): ComplicationColors {
        val color = sharedPreferences.getInt(
            KEY_COMPLICATION_COLORS,
            DEFAULT_COMPLICATION_COLOR
        )

        if( color == DEFAULT_COMPLICATION_COLOR) {
            return ComplicationColorsProvider.getDefaultComplicationColors(appContext)
        }

        return ComplicationColors(
            color,
            color,
            false
        )
    }

    override fun setComplicationColors(complicationColors: ComplicationColors) {
        sharedPreferences.edit().putInt(
            KEY_COMPLICATION_COLORS,
            if( complicationColors.isDefault ) {
                DEFAULT_COMPLICATION_COLOR
            } else { complicationColors.leftColor }
        ).apply()
    }
}