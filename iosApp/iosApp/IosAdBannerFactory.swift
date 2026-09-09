import UIKit
import GoogleMobileAds
import Shared

/// Dostarcza wspólnemu kodowi Compose (AdBanner.ios.kt) natywny widok reklamy
/// banerowej AdMob. `install()` wołane po inicjalizacji Google Mobile Ads SDK
/// (patrz iOSApp.swift) — ustawia fabrykę, z której Compose tworzy baner przy
/// każdym wyświetleniu `AdBanner`.
enum IosAdBannerFactory {

    static func install() {
        AdBanner_iosKt.createIosAdBannerView = { makeBannerView() }
    }

    private static func makeBannerView() -> UIView {
        guard let adUnitID = Bundle.main.object(forInfoDictionaryKey: "AD_BANNER_ID") as? String,
              !adUnitID.isEmpty else {
            return UIView()
        }

        let width = UIScreen.main.bounds.width
        let bannerView = BannerView(adSize: currentOrientationAnchoredAdaptiveBanner(width: width))
        bannerView.adUnitID = adUnitID
        bannerView.rootViewController = currentRootViewController()
        bannerView.load(Request())
        return bannerView
    }

    private static func currentRootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows
            .first(where: { $0.isKeyWindow })?.rootViewController
    }
}
