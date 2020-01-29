package com.benoitletondor.pixelminimalwatchfacecompanion.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import java.io.IOException
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * SKU premium
 */
private const val SKU_PREMIUM = "premium"

class BillingImpl(context: Context,
                  private val storage: Storage) : Billing, PurchasesUpdatedListener, BillingClientStateListener,
    PurchaseHistoryResponseListener, AcknowledgePurchaseResponseListener {

    private val appContext = context.applicationContext
    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    /**
     * iab check status
     */
    private var iabStatus: PremiumCheckStatus = PremiumCheckStatus.Initializing

    private var premiumFlowContinuation: Continuation<PremiumPurchaseFlowResult>? = null

    override val userPremiumEventStream: LiveData<PremiumCheckStatus>
        get() = userPremiumEventSteamInternal

    private val userPremiumEventSteamInternal = MutableLiveData<PremiumCheckStatus>()

    init {
        startBillingClient()
    }

    private fun startBillingClient() {
        try {
            setIabStatusAndNotify(PremiumCheckStatus.Initializing)

            billingClient.startConnection(this)
        } catch (e: Exception) {
            Log.e("BillingImpl", "Error while checking iab status", e)
            setIabStatusAndNotify(PremiumCheckStatus.Error(e))
        }
    }

    /**
     * Set the new iab status and notify the app by an event
     *
     * @param status the new status
     */
    private fun setIabStatusAndNotify(status: PremiumCheckStatus) {
        iabStatus = status

        if (status == PremiumCheckStatus.Premium || status == PremiumCheckStatus.NotPremium) {
            storage.setUserPremium(iabStatus == PremiumCheckStatus.Premium)
        }

        userPremiumEventSteamInternal.postValue(status)
    }

    /**
     * Is the user a premium user
     *
     * @return true if the user if premium, false otherwise
     */
    override fun isUserPremium(): Boolean
    {
        return storage.isUserPremium() || iabStatus == PremiumCheckStatus.Premium
    }

    /**
     * Update the current IAP status if already checked
     */
    override fun updatePremiumStatusIfNeeded() {
        Log.d("BillingImpl", "updateIAPStatusIfNeeded: $iabStatus")

        if ( iabStatus == PremiumCheckStatus.NotPremium ) {
            setIabStatusAndNotify(PremiumCheckStatus.Checking)
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this)
        } else if ( iabStatus is PremiumCheckStatus.Error) {
            startBillingClient()
        }
    }

    /**
     * Launch the premium purchase flow
     *
     * @param activity activity that started this purchase
     */
    override suspend fun launchPremiumPurchaseFlow(activity: Activity): PremiumPurchaseFlowResult {
        if ( iabStatus != PremiumCheckStatus.NotPremium ) {
            return when (iabStatus) {
                is PremiumCheckStatus.Error -> PremiumPurchaseFlowResult.Error("Unable to connect to your Google account. Please restart the app and try again")
                PremiumCheckStatus.Premium -> PremiumPurchaseFlowResult.Error("You already bought Premium with that Google account. Restart the app if you don't have access to premium features.")
                else -> PremiumPurchaseFlowResult.Error("Runtime error: $iabStatus")
            }
        }

        val skuList = ArrayList<String>(1)
        skuList.add(SKU_PREMIUM)

        val (billingResult, skuDetailsList) = querySkuDetails(
            SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        )

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                setIabStatusAndNotify(PremiumCheckStatus.Premium)
                return PremiumPurchaseFlowResult.Success
            }

            return PremiumPurchaseFlowResult.Error("Unable to connect to reach PlayStore (response code: " + billingResult.responseCode + "). Please restart the app and try again")
        }

        if (skuDetailsList.isEmpty()) {
            return PremiumPurchaseFlowResult.Error("Unable to fetch content from PlayStore (response code: skuDetailsList is empty). Please restart the app and try again")
        }

        return suspendCoroutine { continuation ->
            premiumFlowContinuation = continuation

            billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList[0])
                .build()
            )
        }
    }

    data class SkuDetailsResponse(val billingResult: BillingResult, val skuDetailsList: List<SkuDetails>)

    private suspend fun querySkuDetails(params: SkuDetailsParams): SkuDetailsResponse = suspendCoroutine { continuation ->
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            continuation.resumeWith(Result.success(SkuDetailsResponse(billingResult, skuDetailsList)))
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d("BillingImpl", "iab setup finished.")

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            // Oh noes, there was a problem.
            setIabStatusAndNotify(PremiumCheckStatus.Error(Exception("Error while setting-up iab: " + billingResult.responseCode)))
            Log.e("BillingImpl","Error while setting-up iab: " + billingResult.responseCode)
            return
        }

        setIabStatusAndNotify(PremiumCheckStatus.Checking)

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this)
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingImpl", "onBillingServiceDisconnected")

        premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Error("Lost connection with Google Play")))
        setIabStatusAndNotify(PremiumCheckStatus.Error(IOException("Lost connection with Google Play")))
    }

    override fun onPurchaseHistoryResponse(billingResult: BillingResult, purchaseHistoryRecordList: List<PurchaseHistoryRecord>?) {
        Log.d("BillingImpl", "iab query inventory finished.")

        // Is it a failure?
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e("BillingImpl", "Error while querying iab inventory: " + billingResult.responseCode)
            setIabStatusAndNotify(PremiumCheckStatus.Error(Exception("Error while querying iab inventory: " + billingResult.responseCode)))
            return
        }

        var premium = false
        if (purchaseHistoryRecordList != null) {
            for (purchase in purchaseHistoryRecordList) {
                if (SKU_PREMIUM == purchase.sku) {
                    premium = true
                }
            }
        }

        Log.d("BillingImpl", "iab query inventory was successful: $premium")

        setIabStatusAndNotify(if (premium) PremiumCheckStatus.Premium else PremiumCheckStatus.NotPremium)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d("BillingImpl", "Purchase finished: " + billingResult.responseCode)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e("BillingImpl", "Error while purchasing premium: " + billingResult.responseCode)
            when {
                billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Cancelled))
                billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    setIabStatusAndNotify(PremiumCheckStatus.Premium)
                    premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Success))
                    return
                }
                else -> premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Error("An error occurred (status code: " + billingResult.responseCode + ")")))
            }

            premiumFlowContinuation = null
            return
        }


        if ( purchases.isNullOrEmpty() ) {
            premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Error("No purchased item found")))
            premiumFlowContinuation = null
            return
        }

        Log.d("BillingImpl", "Purchase successful.")

        for (purchase in purchases) {
            if (SKU_PREMIUM == purchase.sku) {
                billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(), this)
                return
            }
        }

        premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Error("No purchased item found")))
        premiumFlowContinuation = null
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        Log.d("BillingImpl", "Acknowledge successful.")

        if( billingResult.responseCode != BillingClient.BillingResponseCode.OK ) {
            premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Error("Error when acknowledging purchase with Google (${billingResult.responseCode}, ${billingResult.debugMessage}). Please try again")))
            premiumFlowContinuation = null
            return
        }

        setIabStatusAndNotify(PremiumCheckStatus.Premium)
        premiumFlowContinuation?.resumeWith(Result.success(PremiumPurchaseFlowResult.Success))
        premiumFlowContinuation = null
    }
}