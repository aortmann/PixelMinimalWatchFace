package com.benoitletondor.pixelminimalwatchfacecompanion.view.main

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.SingleLiveEvent
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumCheckStatus
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.PremiumPurchaseFlowResult
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import kotlinx.coroutines.*

class MainViewModel(private val billing: Billing,
                    private val sync: Sync) : ViewModel(), CoroutineScope by MainScope() {
    val errorSyncingEvent = SingleLiveEvent<Throwable>()
    val errorPayingEvent = SingleLiveEvent<Throwable>()
    val syncSucceedEvent = SingleLiveEvent<Unit>()
    val stateEventStream = MutableLiveData<State>(if( billing.isUserPremium() ) { State.Premium } else { State.Loading })

    private val userPremiumEventObserver: Observer<PremiumCheckStatus> = Observer { premiumCheckStatus ->
        if( (premiumCheckStatus == PremiumCheckStatus.Premium && stateEventStream.value == State.NotPremium) ||
            (premiumCheckStatus == PremiumCheckStatus.NotPremium && stateEventStream.value == State.Premium) ||
            (premiumCheckStatus == PremiumCheckStatus.Premium || premiumCheckStatus == PremiumCheckStatus.NotPremium) && stateEventStream.value == State.Loading ) {
            syncState(premiumCheckStatus == PremiumCheckStatus.Premium)
        }

        if( premiumCheckStatus is PremiumCheckStatus.Error && stateEventStream.value != State.Premium ) {
            stateEventStream.value = State.Error(premiumCheckStatus.error)
        }

        if( premiumCheckStatus == PremiumCheckStatus.Checking && stateEventStream.value is State.Error ) {
            stateEventStream.value = State.Loading
        }
    }

    init {
        billing.userPremiumEventStream.observeForever(userPremiumEventObserver)
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
                stateEventStream.value = if( userPremium ) { State.Premium } else { State.NotPremium }
            } catch (t: Throwable) {
                errorSyncingEvent.value = t
            }
        }
    }

    override fun onCleared() {
        billing.userPremiumEventStream.removeObserver(userPremiumEventObserver)
        cancel()

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
            try {
                stateEventStream.value = State.Loading

                val result = withContext(Dispatchers.IO) {
                    billing.launchPremiumPurchaseFlow(host)
                }

                // Success result will be handled automatically as notification to userPremiumEventObserver

                if( result is PremiumPurchaseFlowResult.Error ){
                    errorPayingEvent.value = Exception(result.reason)
                    stateEventStream.value = State.NotPremium
                }
            } catch (t: Throwable) {
                errorPayingEvent.value = t
            }
        }
    }

    sealed class State {
        object Loading : State()
        object NotPremium : State()
        object Syncing : State()
        object Premium : State()
        class Error(val error: Throwable) : State()
    }
}