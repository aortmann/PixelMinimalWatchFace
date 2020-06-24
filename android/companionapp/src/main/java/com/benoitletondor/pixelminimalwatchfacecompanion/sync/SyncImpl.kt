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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val KEY_PREMIUM = "premium"
private const val KEY_TIMESTAMP = "ts"

class SyncImpl(private val context: Context) : Sync {
    private val dataClient = Wearable.getDataClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    override suspend fun sendPremiumStatus(isUserPremium: Boolean) {
        val putDataRequest = PutDataMapRequest.create("/premium").run {
            dataMap.putBoolean(KEY_PREMIUM, isUserPremium)
            dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            asPutDataRequest()
        }

        putDataRequest.setUrgent()

        dataClient.putDataItem(putDataRequest).await()
    }

    override suspend fun getWearableStatus(): Sync.WearableStatus {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if( nodes.isEmpty() ) {
                return Sync.WearableStatus.NotAvailable
            }

            val capabilityInfoTask = capabilityClient.getCapability(BuildConfig.WATCH_CAPABILITY, CapabilityClient.FILTER_ALL)
            val result = capabilityInfoTask.await()
            return if( result.nodes.isEmpty() ) {
                Sync.WearableStatus.AvailableAppNotInstalled
            } else {
                Sync.WearableStatus.AvailableAppInstalled
            }
        } catch (t: Throwable) {
            return Sync.WearableStatus.Error(t)
        }
    }

    override suspend fun openPlayStoreOnWatch() = suspendCancellableCoroutine<Boolean> { continuation ->
        try {
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(BuildConfig.WATCH_FACE_APP_PLAYSTORE_URL))

            RemoteIntent.startRemoteActivity(
                context,
                intentAndroid,
                object : ResultReceiver(Handler()) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RemoteIntent.RESULT_OK) {
                            if( continuation.isActive ) {
                                continuation.resume(true)
                            }
                        } else {
                            if( continuation.isActive ) {
                                continuation.resumeWithException(RuntimeException("Error opening PlayStore on watch (result code: $resultCode)"))
                            }
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            continuation.resumeWithException(t)
        }
    }

    override fun subscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener) {
        capabilityClient.addListener(listener, BuildConfig.WATCH_CAPABILITY)
    }

    override fun unsubscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener) {
        capabilityClient.removeListener(listener)
    }

}

private suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
    addOnSuccessListener { if( continuation.isActive ) { continuation.resume(it) } }
    addOnFailureListener { if( continuation.isActive ) { continuation.resumeWithException(it) } }
    addOnCanceledListener { continuation.cancel() }
}