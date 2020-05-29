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

import com.google.android.gms.wearable.CapabilityClient

interface Sync {
    suspend fun sendPremiumStatus(isUserPremium: Boolean)
    suspend fun getWearableStatus(): WearableStatus
    suspend fun openPlayStoreOnWatch(): Boolean
    fun subscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener)
    fun unsubscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener)

    sealed class WearableStatus {
        object AvailableAppNotInstalled : WearableStatus()
        object AvailableAppInstalled: WearableStatus()
        object NotAvailable: WearableStatus()
        class Error(val error: Throwable): WearableStatus()
    }
}