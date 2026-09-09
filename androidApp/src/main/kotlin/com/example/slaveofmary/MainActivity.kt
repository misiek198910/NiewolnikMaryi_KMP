package com.example.slaveofmary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.slaveofmary.billing.isPremiumUserNow
import com.example.slaveofmary.ui.screens.SplashScreen
import com.example.slaveofmary.util.AndroidAppContextHolder
import com.example.slaveofmary.util.LocaleManager
import kotlinx.coroutines.launch
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import mivs.niewolnik_maryi.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var showSplash by mutableStateOf(true)

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var consentInformation: ConsentInformation
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.w("InAppUpdate", "Aktualizacja anulowana lub nie powiodła się. Kod: ${result.resultCode}")
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Nakłada zapisany język (jeśli inny niż systemowy) na Context Activity
        // — dotyczy głównie API < 33, patrz LocaleManager.wrapContext().
        super.attachBaseContext(LocaleManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Musi być wywołane jak najwcześniej, zanim SettingsScreen zapyta o język
        // albo spróbuje go zmienić (LocaleManager potrzebuje Context/Activity).
        AndroidAppContextHolder.init(this)

        checkForUpdate()

        // Najpierw UMP (zgoda RODO/UOKiK), dopiero potem inicjalizacja SDK reklam.
        requestConsentAndInitializeAds()

        setContent {
            if (showSplash) {
                SplashScreen(onNavigateToMain = { showSplash = false })
            } else {
                App() // Wywołanie czystego Multiplatform bez parametrów!
            }
        }
    }

    private fun requestConsentAndInitializeAds() {
        val params = ConsentRequestParameters.Builder()
            // Do testów w Polsce (poza EOG) można wymusić geografię EOG:
            // .setConsentDebugSettings(
            //     ConsentDebugSettings.Builder(this)
            //         .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            //         .addTestDeviceHashedId("TWOJ_TEST_DEVICE_ID")
            //         .build()
            // )
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { formError ->
                    if (formError != null) {
                        Log.w(tag, "Błąd formularza zgody UMP: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                    }
                }
            },
            { requestConsentError ->
                Log.w(tag, "Błąd aktualizacji informacji o zgodzie: ${requestConsentError.message}")
                // Np. brak sieci — jeśli zgoda z poprzedniej sesji już pozwala, wciąż spróbuj załadować reklamy.
                if (consentInformation.canRequestAds()) {
                    initializeMobileAdsSdk()
                }
            }
        )

        // Zgoda z poprzedniego uruchomienia może już pozwalać na reklamy zanim
        // powyższe wywołanie asynchroniczne w ogóle wróci — startujemy od razu,
        // żeby nie opóźniać ładowania reklamy startowej bez potrzeby.
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return

        MobileAds.initialize(this) {
            Log.d(tag, "Mobile Ads SDK initialized.")
            lifecycleScope.launch {
                if (isPremiumUserNow()) {
                    Log.d(tag, "Użytkownik premium — pomijam reklamę startową.")
                    showSplash = false
                } else {
                    loadAppOpenAd()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
        }
    }

    private fun loadAppOpenAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            BuildConfig.AD_START_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.i(tag, "App Open Ad loaded.")
                    showAdIfAvailable()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(tag, "App Open Ad failed to load: ${loadAdError.message}")
                    appOpenAd = null
                    showSplash = false
                }
            }
        )
    }

    private fun showAdIfAvailable() {
        if (isShowingAd) { return }
        if (appOpenAd == null) {
            showSplash = false
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                showSplash = false
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                showSplash = false
            }

            override fun onAdShowedFullScreenContent() {}
        }

        isShowingAd = true
        appOpenAd?.show(this)
    }

    private fun checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}