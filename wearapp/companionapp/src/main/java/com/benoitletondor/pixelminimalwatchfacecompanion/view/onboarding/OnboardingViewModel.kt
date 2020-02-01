package com.benoitletondor.pixelminimalwatchfacecompanion.view.onboarding

import androidx.lifecycle.ViewModel
import com.benoitletondor.pixelminimalwatchfacecompanion.SingleLiveEvent
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage

class OnboardingViewModel(private val storage: Storage) : ViewModel() {
    val finishEventStream = SingleLiveEvent<Unit>()

    fun onOnboardingFinishButtonPressed() {
        storage.setOnboardingFinished(true)
        finishEventStream.value = Unit
    }
}