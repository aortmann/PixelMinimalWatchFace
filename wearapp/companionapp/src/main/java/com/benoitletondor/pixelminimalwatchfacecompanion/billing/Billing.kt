package com.benoitletondor.pixelminimalwatchfacecompanion.billing

import android.app.Activity
import androidx.lifecycle.LiveData

interface Billing {
    val userPremiumEventStream: LiveData<PremiumCheckStatus>

    fun isUserPremium(): Boolean
    fun updatePremiumStatusIfNeeded()
    suspend fun launchPremiumPurchaseFlow(activity: Activity): PremiumPurchaseFlowResult
}

sealed class PremiumPurchaseFlowResult {
    object Cancelled : PremiumPurchaseFlowResult()
    object Success : PremiumPurchaseFlowResult()
    class Error(val reason: String): PremiumPurchaseFlowResult()
}

sealed class PremiumCheckStatus {
    object Initializing : PremiumCheckStatus()
    object Checking : PremiumCheckStatus()
    class Error(val error: Throwable) : PremiumCheckStatus()
    object NotPremium : PremiumCheckStatus()
    object Premium : PremiumCheckStatus()
}