package com.example.slaveofmary.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.slaveofmary.data.dao.SubscriptionDao
import com.example.slaveofmary.data.entity.SubscriptionEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.billing_auth_error
import slaveofmary.shared.generated.resources.billing_error
import slaveofmary.shared.generated.resources.billing_offer_monthly
import slaveofmary.shared.generated.resources.billing_offer_not_found
import slaveofmary.shared.generated.resources.billing_offer_trial
import slaveofmary.shared.generated.resources.billing_offer_yearly
import slaveofmary.shared.generated.resources.billing_purchase_cancelled
import slaveofmary.shared.generated.resources.subscription_buy

class BillingManager(
    private val context: Context,
    private val dao: SubscriptionDao
) {
    private val billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)

    private val _subscriptionStatus = MutableStateFlow<Boolean?>(null)
    val subscriptionStatus = _subscriptionStatus.asStateFlow()

    val productDetails = _productDetails.asStateFlow()

    interface BillingManagerListener {
        fun onPurchaseAcknowledged()
        fun onPurchaseError(error: String?)
    }

    var listener: BillingManagerListener? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) { handlePurchase(purchase) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            scope.launch(Dispatchers.Main) {
                listener?.onPurchaseError(getString(Res.string.billing_purchase_cancelled))
            }
        } else {
            scope.launch(Dispatchers.Main) {
                listener?.onPurchaseError(getString(Res.string.billing_error, billingResult.debugMessage))
            }
        }
    }

    init {
        scope.launch {
            val savedStatus = dao.getStatus()
            _subscriptionStatus.value = savedStatus?.isPremium == true
        }
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchasesAsync()
                    queryProductDetails()
                }
            }
            override fun onBillingServiceDisconnected() { connectToGooglePlay() }
        })
    }

    fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params)
        { billingResult, queryProductDetailsResult ->
            val productDetailsList = queryProductDetailsResult.productDetailsList

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                _productDetails.value = productDetailsList[0]
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, basePlanId: String) {
        val offerDetails = productDetails.subscriptionOfferDetails?.find { it.basePlanId == basePlanId }

        if (offerDetails == null) {
            scope.launch(Dispatchers.Main) {
                listener?.onPurchaseError(getString(Res.string.billing_offer_not_found, basePlanId))
            }
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerDetails.offerToken)
                    .build()
            )).build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun queryPurchasesAsync() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var hasPremium = false
                var token: String? = null
                purchases.forEach { purchase ->
                    if (purchase.products.contains(SUBSCRIPTION_PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasPremium = true
                        token = purchase.purchaseToken
                        if (!purchase.isAcknowledged) handlePurchase(purchase)
                    }
                }
                updateLocalStatus(hasPremium, token)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    updateLocalStatus(true, purchase.purchaseToken)
                    scope.launch(Dispatchers.Main) {
                        listener?.onPurchaseAcknowledged()
                    }
                } else {
                    scope.launch(Dispatchers.Main) {
                        listener?.onPurchaseError(getString(Res.string.billing_auth_error, result.debugMessage))
                    }
                }
            }
        } else if (purchase.isAcknowledged) {
            updateLocalStatus(true, purchase.purchaseToken)
        }
    }

    private fun updateLocalStatus(hasPremium: Boolean, token: String?) {
        _subscriptionStatus.value = hasPremium
        scope.launch {
            dao.insertStatus(
                SubscriptionEntity(
                    id = 1,
                    isPremium = hasPremium,
                    purchaseToken = token
                )
            )
        }
    }
    suspend fun getPlanOfferInfo(productDetails: ProductDetails?, basePlanId: String): String {
        val offer = productDetails?.subscriptionOfferDetails?.find { it.basePlanId == basePlanId }
        val trialPhase = offer?.pricingPhases?.pricingPhaseList?.find { it.priceAmountMicros == 0L }
        val basePhase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
        val price = basePhase?.formattedPrice ?: ""

        return when {
            basePlanId == BASE_PLAN_YEARLY_TRIAL && trialPhase != null && basePhase != null ->
                getString(Res.string.billing_offer_trial, price)
            basePlanId == BASE_PLAN_MONTHLY && basePhase != null ->
                getString(Res.string.billing_offer_monthly, price)
            basePlanId == BASE_PLAN_YEARLY_TRIAL && basePhase != null ->
                getString(Res.string.billing_offer_yearly, price)
            else -> getString(Res.string.subscription_buy)
        }
    }

    companion object {
        const val SUBSCRIPTION_PRODUCT_ID = "slaveofmary_premium"
        const val BASE_PLAN_MONTHLY = "premium-monthy"
        const val BASE_PLAN_YEARLY_TRIAL = "premium-yearly"
    }
}