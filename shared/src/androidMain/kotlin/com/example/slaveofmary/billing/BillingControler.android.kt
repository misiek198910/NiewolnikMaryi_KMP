package com.example.slaveofmary.billing

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.slaveofmary.data.db.AppDatabaseProvider

@Composable
actual fun rememberBillingController(): BillingController? {
    val context = LocalContext.current
    val activity = context as? Activity ?: return null

    val billingManager = remember {
        BillingManager(
            context = context.applicationContext,
            dao = AppDatabaseProvider.database.subscriptionDao()
        )
    }

    return remember(activity) {
        AndroidBillingController(activity, billingManager)
    }
}