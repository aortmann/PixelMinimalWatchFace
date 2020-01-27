package com.benoitletondor.pixelminimalwatchfacecompanion.sync

interface Sync {
    suspend fun sendPremiumStatus(isUserPremium: Boolean)
}