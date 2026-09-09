package com.example.slaveofmary.billing

import com.example.slaveofmary.data.db.AppDatabaseProvider
import com.example.slaveofmary.data.entity.SubscriptionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSURL
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.billing_offer_monthly
import slaveofmary.shared.generated.resources.billing_offer_yearly

/** Identyfikatory subskrypcji utworzone w App Store Connect (ta sama grupa subskrypcji, bez okresu próbnego). */
private const val PRODUCT_ID_MONTHLY = "mivs.niewolnikmaryi.premium.monthly"
private const val PRODUCT_ID_YEARLY = "mivs.niewolnikmaryi.premium.yearly"

class IosBillingController : BillingController {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val dao = AppDatabaseProvider.database.subscriptionDao()

    override val monthlyPrice = MutableStateFlow("")
    override val yearlyPrice = MutableStateFlow("")

    private var monthlyProduct: SKProduct? = null
    private var yearlyProduct: SKProduct? = null
    private var premiumGranted = false

    // Klasy Objective-C (NSObject + protokoły StoreKit) nie mogą być mieszane z interfejsem
    // Kotlin BillingController w jednej klasie, więc obserwator/delegat żyje osobno i
    // przekazuje zdarzenia z powrotem do tego kontrolera.
    private val storeKitDelegate = StoreKitDelegate(
        onProductsReceived = ::onProductsReceived,
        onTransactionsUpdated = ::onTransactionsUpdated,
        onRestoreFinished = ::onRestoreFinished
    )

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(storeKitDelegate)
        val request = SKProductsRequest(productIdentifiers = setOf(PRODUCT_ID_MONTHLY, PRODUCT_ID_YEARLY))
        request.delegate = storeKitDelegate
        request.start()
        // Cichy restore przy starcie — bez tego status w Room zostaje `null` na zawsze
        // dla użytkownika bez wcześniejszego zakupu, a UI wisi na "Sprawdzanie...".
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
    }

    override fun buyMonthly() {
        monthlyProduct?.let { SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(it)) }
    }

    override fun buyYearly() {
        yearlyProduct?.let { SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(it)) }
    }

    override fun restore() {
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
    }

    override fun manage() {
        NSURL.URLWithString("https://apps.apple.com/account/subscriptions")?.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }

    private fun onProductsReceived(products: List<SKProduct>) {
        products.forEach { product ->
            when (product.productIdentifier) {
                PRODUCT_ID_MONTHLY -> monthlyProduct = product
                PRODUCT_ID_YEARLY -> yearlyProduct = product
            }
        }
        updatePrices()
    }

    private fun onTransactionsUpdated(transactions: List<SKPaymentTransaction>) {
        transactions.forEach { transaction ->
            when (transaction.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    grantPremium()
                    SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                }
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                }
                else -> Unit
            }
        }
    }

    private fun grantPremium() {
        premiumGranted = true
        scope.launch {
            dao.insertStatus(SubscriptionEntity(id = 1, isPremium = true, purchaseToken = null))
        }
    }

    private fun onRestoreFinished() {
        if (!premiumGranted) {
            scope.launch {
                dao.insertStatus(SubscriptionEntity(id = 1, isPremium = false, purchaseToken = null))
            }
        }
    }

    private fun updatePrices() {
        scope.launch {
            monthlyProduct?.let { product ->
                monthlyPrice.value = getString(Res.string.billing_offer_monthly, formattedPrice(product))
            }
            yearlyProduct?.let { product ->
                yearlyPrice.value = getString(Res.string.billing_offer_yearly, formattedPrice(product))
            }
        }
    }

    private fun formattedPrice(product: SKProduct): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.locale = product.priceLocale
        return formatter.stringFromNumber(product.price) ?: product.price.stringValue
    }
}

private class StoreKitDelegate(
    private val onProductsReceived: (List<SKProduct>) -> Unit,
    private val onTransactionsUpdated: (List<SKPaymentTransaction>) -> Unit,
    private val onRestoreFinished: () -> Unit
) : NSObject(), SKProductsRequestDelegateProtocol, SKPaymentTransactionObserverProtocol {

    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        @Suppress("UNCHECKED_CAST")
        onProductsReceived(didReceiveResponse.products as List<SKProduct>)
    }

    override fun request(request: SKRequest, didFailWithError: NSError) = Unit

    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        @Suppress("UNCHECKED_CAST")
        onTransactionsUpdated(updatedTransactions as List<SKPaymentTransaction>)
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        onRestoreFinished()
    }

    override fun paymentQueue(queue: SKPaymentQueue, restoreCompletedTransactionsFailedWithError: NSError) {
        onRestoreFinished()
    }
}
