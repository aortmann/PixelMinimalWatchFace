package com.benoitletondor.pixelminimalwatchfacecompanion.storage

interface Storage {
    fun isUserPremium(): Boolean
    fun setUserPremium(premium: Boolean)
    fun setOnboardingFinished(finished: Boolean)
    fun isOnboardingFinished(): Boolean
}