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