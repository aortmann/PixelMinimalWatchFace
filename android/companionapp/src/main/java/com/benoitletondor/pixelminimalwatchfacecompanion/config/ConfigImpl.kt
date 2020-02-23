package com.benoitletondor.pixelminimalwatchfacecompanion.config

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ConfigImpl(private val firebaseRemoteConfig: FirebaseRemoteConfig) : Config {

    override fun getBoolean(configKey: String): Boolean
        = firebaseRemoteConfig.getBoolean(configKey)

    override fun getDouble(configKey: String): Double
        = firebaseRemoteConfig.getDouble(configKey)

    override fun getLong(configKey: String): Long
        = firebaseRemoteConfig.getLong(configKey)

    override fun getString(configKey: String): String? {
        val configValue = firebaseRemoteConfig.getString(configKey)
        if( configValue.isEmpty() ) {
            return null
        }

        return configValue
    }

// ---------------------------------->

    override fun getLastFetchTimestamp(): Long? {
        val lastFetchTs = firebaseRemoteConfig.info.fetchTimeMillis
        if( lastFetchTs <= 0 ) {
            return null
        }

        return lastFetchTs
    }

    override suspend fun fetch(): Boolean {
        try {
            firebaseRemoteConfig.fetch().await()
        } catch (e: FirebaseRemoteConfigFetchThrottledException) {
            return false
        }

        return firebaseRemoteConfig.activate().await()
    }

    private suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
        addOnSuccessListener { if( continuation.isActive ) { continuation.resume(it) } }
        addOnFailureListener { if( continuation.isActive ) { continuation.resumeWithException(it) } }
        addOnCanceledListener { continuation.cancel() }
    }
}