package com.example.slaveofmary.billing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberBillingController(): BillingController? = remember { IosBillingController() }