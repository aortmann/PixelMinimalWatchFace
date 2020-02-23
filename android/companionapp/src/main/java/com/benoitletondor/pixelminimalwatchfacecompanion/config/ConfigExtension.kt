package com.benoitletondor.pixelminimalwatchfacecompanion.config

import android.util.Log
import org.json.JSONArray

private const val PROMO_CODES_KEY = "promocodes"

fun Config.getVouchers(): List<String> {
    val promocodes: MutableList<String> = mutableListOf()

    try {
        val configValue = getString(PROMO_CODES_KEY)
        if( configValue != null ) {
            val codes = JSONArray(configValue)
            for(i in 0..codes.length()) {
                val code = codes.getString(i)
                promocodes.add(code)
            }
        }
    } catch (t: Throwable) {
        Log.e("Config", "Error getting promo codes from config", t)
    }

    return promocodes
}