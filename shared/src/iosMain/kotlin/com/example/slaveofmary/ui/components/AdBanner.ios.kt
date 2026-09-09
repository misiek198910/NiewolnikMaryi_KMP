package com.example.slaveofmary.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIView

/**
 * Fabryka natywnego widoku reklamy banerowej AdMob. Ustawiana z kodu Swift
 * (IosAdBannerFactory) po inicjalizacji Google Mobile Ads SDK.
 */
var createIosAdBannerView: (() -> UIView)? = null

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier, isPremium: Boolean) {
    if (isPremium) return

    // Stała wysokość slotu (jak na Androidzie) — pojawienie się reklamy z
    // opóźnieniem nie przesuwa sąsiednich elementów.
    val slot = modifier.fillMaxWidth().height(50.dp)

    val factory = createIosAdBannerView
    if (factory == null) {
        Box(modifier = slot)
        return
    }

    UIKitView(
        modifier = slot,
        factory = {
            val container = UIView()
            container.backgroundColor = UIColor.clearColor
            val banner = factory()
            banner.backgroundColor = UIColor.clearColor
            container.addSubview(banner)
            container
        },
        update = { container ->
            (container.subviews.firstOrNull() as? UIView)?.setFrame(container.bounds)
        }
    )
}
