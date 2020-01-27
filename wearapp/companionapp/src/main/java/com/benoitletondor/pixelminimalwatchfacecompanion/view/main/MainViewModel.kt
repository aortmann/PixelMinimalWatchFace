package com.benoitletondor.pixelminimalwatchfacecompanion.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import kotlinx.coroutines.*

class MainViewModel(private val billing: Billing,
                    private val sync: Sync) : ViewModel(), CoroutineScope by MainScope() {
    val stateEventStream = MutableLiveData<State>()

    private val userPremiumEventObserver: Observer<Boolean> = Observer { isUserPremium ->
        if( (isUserPremium && stateEventStream.value ==  State.NotPremium) ||
            !isUserPremium && stateEventStream.value == State.Premium) {
            syncState(isUserPremium)
        }
    }

    init {
        stateEventStream.value = if( billing.isUserPremium() ) { State.Premium } else { State.NotPremium }
        billing.userPremiumEventStream.observeForever(userPremiumEventObserver)
    }

    private fun syncState(userPremium: Boolean) {
        launch {
            try {
                stateEventStream.value = State.Syncing

                withContext(Dispatchers.IO) {
                    sync.sendPremiumStatus(userPremium)
                }

                stateEventStream.value = if( userPremium ) { State.Premium } else { State.NotPremium }
            } catch (t: Throwable) {
                stateEventStream.value = State.ErrorSyncing(userPremium)
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

    sealed class State {
        object NotPremium : State()
        object Syncing : State()
        class ErrorSyncing(val isUserPremium: Boolean) : State()
        object Premium : State()
    }
}