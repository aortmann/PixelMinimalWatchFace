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

interface Config {
    fun getBoolean(configKey: String): Boolean
    fun getDouble(configKey: String): Double
    fun getLong(configKey: String): Long
    fun getString(configKey: String): String?

// -------------------------------------->

    /**
     * Get the last fetch timestamp, or null if no fetch has been made yet
     */
    fun getLastFetchTimestamp(): Long?

    /**
     * Fetch the updated configuration
     *
     * @return a Single containing true if a new config has been fetched, false if nothing has changed
     */
    suspend fun fetch(): Boolean
}