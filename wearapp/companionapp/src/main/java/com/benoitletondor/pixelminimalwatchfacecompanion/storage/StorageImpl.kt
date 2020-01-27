package com.benoitletondor.pixelminimalwatchfacecompanion.storage

import android.content.Context
import androidx.core.content.edit

private const val SHARED_PREFERENCES_FILE_NAME = "sharedPref"

private const val PREMIUM_KEY = "premium"

class StorageImpl(context: Context) : Storage {
    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    override fun isUserPremium(): Boolean
        = sharedPreferences.getBoolean(PREMIUM_KEY, false)

    override fun setUserPremium(premium: Boolean) {
        sharedPreferences.edit {
            putBoolean(PREMIUM_KEY, premium)
        }
    }

}