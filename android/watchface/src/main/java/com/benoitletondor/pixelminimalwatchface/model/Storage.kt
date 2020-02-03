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
import android.content.SharedPreferences

private const val SHARED_PREFERENCES_NAME = "pixelMinimalSharedPref"

private const val DEFAULT_COMPLICATION_COLOR = -147282
private const val KEY_COMPLICATION_COLORS = "complicationColors"
private const val KEY_USER_PREMIUM = "user_premium"
private const val KEY_USE_24H_TIME_FORMAT = "use24hTimeFormat"
private const val KEY_INSTALL_TIMESTAMP = "installTS"
private const val KEY_RATING_NOTIFICATION_SENT = "ratingNotificationSent"

interface Storage {
    fun init(context: Context)
    fun getComplicationColors(): ComplicationColors
    fun setComplicationColors(complicationColors: ComplicationColors)
    fun isUserPremium(): Boolean
    fun setUserPremium(premium: Boolean)
    fun setUse24hTimeFormat(use: Boolean)
    fun getUse24hTimeFormat(): Boolean
    fun getInstallTimestamp(): Long
    fun hasRatingNotificationBeenSend(): Boolean
    fun setRatingNotificationSent(sent: Boolean)
}

class StorageImpl : Storage {
    private lateinit var appContext: Context
    private lateinit var sharedPreferences: SharedPreferences

    override fun init(context: Context) {
        appContext = context.applicationContext
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        if( getInstallTimestamp() < 0 ) {
            sharedPreferences.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
        }
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
            "TODO", // FIXME
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

    override fun isUserPremium(): Boolean {
        return sharedPreferences.getBoolean(KEY_USER_PREMIUM, false)
    }

    override fun setUserPremium(premium: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USER_PREMIUM, premium).apply()
    }

    override fun setUse24hTimeFormat(use: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USE_24H_TIME_FORMAT, use).apply()
    }

    override fun getUse24hTimeFormat(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_24H_TIME_FORMAT, true)
    }

    override fun getInstallTimestamp(): Long {
        return sharedPreferences.getLong(KEY_INSTALL_TIMESTAMP, -1)
    }

    override fun hasRatingNotificationBeenSend(): Boolean {
        return sharedPreferences.getBoolean(KEY_RATING_NOTIFICATION_SENT, false)
    }

    override fun setRatingNotificationSent(sent: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_RATING_NOTIFICATION_SENT, sent).apply()
    }
}