package com.example.slaveofmary.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.example.slaveofmary.shared.BuildConfig

@SuppressLint("MissingPermission")
@Composable
actual fun AdBanner(modifier: Modifier, isPremium: Boolean) {
    if (isPremium) return

    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // Rozmiar bannera wyliczamy synchronicznie na pierwszej kompozycji, więc
    // slot ma docelową wysokość jeszcze zanim reklama się załaduje – dzięki temu
    // sąsiednie kontrolki nie "przeskakują", gdy baner pojawi się z opóźnieniem.
    val adSize = remember(configuration.orientation, configuration.screenWidthDp) {
        val adWidth = configuration.screenWidthDp
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(context, adWidth)
        } else {
            AdSize.getPortraitAnchoredAdaptiveBannerAdSize(context, adWidth)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)

                    adUnitId = BuildConfig.AD_BANNER_ID

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {}
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdMobBanner", "BŁĄD: Baner nie załadował się! Kod: ${error.code}")
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
