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