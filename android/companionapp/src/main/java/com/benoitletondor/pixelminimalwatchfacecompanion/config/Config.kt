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