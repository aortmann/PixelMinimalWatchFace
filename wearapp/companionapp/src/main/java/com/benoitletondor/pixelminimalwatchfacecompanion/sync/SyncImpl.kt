package com.benoitletondor.pixelminimalwatchfacecompanion.sync

import android.content.Context
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


private const val KEY_PREMIUM = "premium"

class SyncImpl(context: Context) : Sync {
    private val dataClient = Wearable.getDataClient(context)

    override suspend fun sendPremiumStatus(isUserPremium: Boolean) {
        val putDataMapRequest = PutDataMapRequest.create("/premium")

        val dataMap = DataMap().apply {
            putBoolean(KEY_PREMIUM, isUserPremium)
        }

        putDataMapRequest.dataMap.putDataMap(BuildConfig.WATCH_FACE_BUNDLE_ID, dataMap)

        val putDataRequest = putDataMapRequest.asPutDataRequest()
        putDataMapRequest.setUrgent()

        dataClient.putDataItem(putDataRequest).await()
    }

}

private suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
    addOnSuccessListener { if( continuation.isActive ) { continuation.resume(it) } }
    addOnFailureListener { if( continuation.isActive ) { continuation.resumeWithException(it) } }
    addOnCanceledListener { continuation.cancel() }
}