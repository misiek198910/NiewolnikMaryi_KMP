import SwiftUI

@main
struct iOSApp: App {

    init() {
        // Fabryka banera musi istnieć zanim Compose po raz pierwszy narysuje AdBanner.
        IosAdBannerFactory.install()

        // Po zakończeniu UMP -> ATT -> inicjalizacji SDK, pokaż reklamę startową —
        // odpowiednik loadAppOpenAd() wywoływanego po MobileAds.initialize() na Androidzie.
        ConsentManager.shared.onAdsSdkReady = {
            IosAdBannerFactory.install()
            AppOpenAdHelper.shared.loadAndShow(completion: {})
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    ConsentManager.shared.start()
                }
        }
    }
}