package com.example.slaveofmary.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.slaveofmary.billing.LocalIsPremium

@Composable
expect fun AdBanner(modifier: Modifier = Modifier, isPremium: Boolean = LocalIsPremium.current)