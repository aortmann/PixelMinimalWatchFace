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
package com.benoitletondor.pixelminimalwatchfacecompanion.sync

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val KEY_PREMIUM = "premium"

class SyncImpl(context: Context) : Sync {
    private val dataClient = Wearable.getDataClient(context)

    override suspend fun sendPremiumStatus(isUserPremium: Boolean) {
        val putDataRequest = PutDataMapRequest.create("/premium").run {
            dataMap.putBoolean(KEY_PREMIUM, isUserPremium)
            asPutDataRequest()
        }

        putDataRequest.setUrgent()

        dataClient.putDataItem(putDataRequest).await()
    }

}

private suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
    addOnSuccessListener { if( continuation.isActive ) { continuation.resume(it) } }
    addOnFailureListener { if( continuation.isActive ) { continuation.resumeWithException(it) } }
    addOnCanceledListener { continuation.cancel() }
}