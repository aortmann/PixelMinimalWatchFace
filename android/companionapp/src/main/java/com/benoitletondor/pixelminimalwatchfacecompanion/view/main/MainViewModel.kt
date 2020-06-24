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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.main

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.SingleLiveEvent
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumCheckStatus
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumPurchaseFlowResult
import com.benoitletondor.pixelminimalwatchfacecompanion.config.Config
import com.benoitletondor.pixelminimalwatchfacecompanion.config.getVouchers
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import kotlinx.coroutines.*

class MainViewModel(private val billing: Billing,
                    private val sync: Sync,
                    private val config: Config,
                    private val storage: Storage) : ViewModel(), CoroutineScope by MainScope(), CapabilityClient.OnCapabilityChangedListener {
    val launchOnboardingEvent = SingleLiveEvent<Unit>()
    val errorSyncingEvent = SingleLiveEvent<Throwable>()
    val errorPayingEvent = SingleLiveEvent<Throwable>()
    val syncSucceedEvent = SingleLiveEvent<Unit>()
    val stateEventStream = MutableLiveData(if( billing.isUserPremium() ) { State.Premium(AppInstalledStatus.Verifying) } else { State.Loading })
    val voucherFlowLaunchEvent = SingleLiveEvent<String>()
    val openPlayStoreStatusEvent = SingleLiveEvent<Boolean>()

    private val userPremiumEventObserver: Observer<PremiumCheckStatus> = Observer { premiumCheckStatus ->
        if( (premiumCheckStatus == PremiumCheckStatus.Premium && stateEventStream.value is State.NotPremium) ||
            (premiumCheckStatus == PremiumCheckStatus.NotPremium && stateEventStream.value is State.Premium) ||
            (premiumCheckStatus == PremiumCheckStatus.Premium || premiumCheckStatus == PremiumCheckStatus.NotPremium) && stateEventStream.value == State.Loading ) {
            syncState(premiumCheckStatus == PremiumCheckStatus.Premium)
        }

        if( premiumCheckStatus is PremiumCheckStatus.Error && stateEventStream.value !is State.Premium ) {
            stateEventStream.value = State.Error(premiumCheckStatus.error)
        }

        if( premiumCheckStatus == PremiumCheckStatus.Checking && stateEventStream.value is State.Error ) {
            stateEventStream.value = State.Loading
        }
    }

    init {
        billing.userPremiumEventStream.observeForever(userPremiumEventObserver)

        if( !storage.isOnboardingFinished() ) {
            launchOnboardingEvent.value = Unit
        }

        syncAppInstalledStatus()
        sync.subscribeToCapabilityChanges(this)
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        syncAppInstalledStatus()
    }

    private fun syncState(userPremium: Boolean) {
        launch {
            try {
                stateEventStream.value = State.Syncing

                withContext(Dispatchers.IO) {
                    sync.sendPremiumStatus(userPremium)
                }

                if( userPremium ) {
                    syncSucceedEvent.value = Unit
                }
            } catch (t: Throwable) {
                errorSyncingEvent.value = t
            }

            val appInstalledStatus = fetchAppInstalledStatus()
            stateEventStream.value = if( userPremium ) { State.Premium(appInstalledStatus) } else { State.NotPremium(appInstalledStatus) }
        }
    }

    override fun onCleared() {
        billing.userPremiumEventStream.removeObserver(userPremiumEventObserver)
        cancel()
        sync.unsubscribeToCapabilityChanges(this)

        super.onCleared()
    }

    fun triggerSync() {
        syncState(billing.isUserPremium())
    }

    fun retryPremiumStatusCheck() {
        billing.updatePremiumStatusIfNeeded()
    }

    fun launchPremiumBuyFlow(host: Activity) {
        launch {
            val previousState = stateEventStream.value

            try {
                stateEventStream.value = State.Loading

                val result = withContext(Dispatchers.IO) {
                    billing.launchPremiumPurchaseFlow(host)
                }

                // Success result will be handled automatically as notification to userPremiumEventObserver

                if( result is PremiumPurchaseFlowResult.Error ){
                    errorPayingEvent.value = Exception(result.reason)
                    stateEventStream.value = previousState
                }
            } catch (t: Throwable) {
                errorPayingEvent.value = t
                stateEventStream.value = previousState
            }
        }
    }

    fun onVoucherInput(voucher: String) {
        val vouchers = config.getVouchers()
        if( vouchers.contains(voucher) ) {
            storage.setUserPremium(true)
            syncState(true)

            return
        }

        voucherFlowLaunchEvent.value = voucher
    }

    fun onInstallWatchFaceButtonPressed() {
        launch {
            try {
                openPlayStoreStatusEvent.value = sync.openPlayStoreOnWatch()
            } catch (t: Throwable) {
                Log.e("MainViewModel", "Error opening PlayStore", t)
                openPlayStoreStatusEvent.value = false
            }
        }
    }

    private suspend fun fetchAppInstalledStatus(): AppInstalledStatus {
        return AppInstalledStatus.Result(sync.getWearableStatus())
    }

    private fun syncAppInstalledStatus() {
        launch {
            val appInstalledStatus = fetchAppInstalledStatus()
            if( stateEventStream.value is State.Premium ) {
                stateEventStream.value = State.Premium(appInstalledStatus)
            } else if( stateEventStream.value is State.NotPremium ) {
                stateEventStream.value = State.NotPremium(appInstalledStatus)
            }
        }
    }

    sealed class State {
        object Loading : State()
        class NotPremium(val appInstalledStatus: AppInstalledStatus) : State()
        object Syncing : State()
        class Premium(val appInstalledStatus: AppInstalledStatus) : State()
        class Error(val error: Throwable) : State()
    }

    sealed class AppInstalledStatus {
        object Verifying : AppInstalledStatus()
        class Result(val wearableStatus: Sync.WearableStatus) : AppInstalledStatus()
    }
}