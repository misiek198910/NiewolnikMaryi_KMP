import Foundation
import UIKit
import GoogleMobileAds
import Shared

/// Reklama typu App Open pokazywana przy starcie aplikacji.
///
/// Warunek: NIE pokazujemy jej przy pierwszym uruchomieniu (mniej agresywne
/// wejście i bezpieczniejsze dla review Apple). Reklama startowa pojawia się
/// dopiero od drugiego otwarcia aplikacji.
final class AppOpenAdHelper: NSObject {

    static let shared = AppOpenAdHelper()
    private override init() { super.init() }

    private var appOpenAd: AppOpenAd?
    private var isShowingAd = false
    private var loadTime: Date?

    /// Reklamy App Open wygasają po 4 godzinach od załadowania.
    private let adValidityDuration: TimeInterval = 4 * 3600

    private let launchCountKey = "app_open_launch_count"

    private var pendingCompletion: (() -> Void)?

    /// Wywoływane po zakończeniu łańcucha UMP -> ATT -> init SDK (patrz iOSApp.swift).
    func loadAndShow(completion: @escaping () -> Void) {
        // Użytkownik premium — bez reklamy startowej.
        guard !PremiumStatus_iosKt.isPremiumUserBlocking() else {
            print("Użytkownik premium — pomijam reklamę startową.")
            completion()
            return
        }

        let defaults = UserDefaults.standard
        let launchCount = defaults.integer(forKey: launchCountKey) + 1
        defaults.set(launchCount, forKey: launchCountKey)

        // Pierwsze uruchomienie: bez reklamy startowej.
        guard launchCount >= 2 else {
            completion()
            return
        }

        loadAd { [weak self] in
            DispatchQueue.main.async {
                self?.showAdIfAvailable(completion: completion)
            }
        }
    }

    private func loadAd(completion: @escaping () -> Void) {
        guard let adUnitID = Bundle.main.object(forInfoDictionaryKey: "AD_START_ID") as? String,
              !adUnitID.isEmpty else {
            print("Brak AD_START_ID — pomijam reklamę startową.")
            completion()
            return
        }

        AppOpenAd.load(with: adUnitID, request: Request()) { [weak self] ad, error in
            guard let self = self else { return }

            if let error = error {
                print("Błąd ładowania reklamy startowej: \(error.localizedDescription)")
                completion()
                return
            }

            self.appOpenAd = ad
            self.appOpenAd?.fullScreenContentDelegate = self
            self.loadTime = Date()
            completion()
        }
    }

    private func showAdIfAvailable(completion: @escaping () -> Void) {
        guard !isShowingAd,
              isAdAvailable(),
              let ad = appOpenAd,
              let rootViewController = Self.currentRootViewController() else {
            completion()
            return
        }

        pendingCompletion = completion
        isShowingAd = true
        ad.present(from: rootViewController)
    }

    private func isAdAvailable() -> Bool {
        guard let loadTime = loadTime else { return false }
        return Date().timeIntervalSince(loadTime) < adValidityDuration
    }

    private func finish() {
        appOpenAd = nil
        loadTime = nil
        isShowingAd = false
        pendingCompletion?()
        pendingCompletion = nil
    }

    private static func currentRootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows
            .first(where: { $0.isKeyWindow })?.rootViewController
    }
}

extension AppOpenAdHelper: FullScreenContentDelegate {

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        finish()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Nie udało się pokazać reklamy startowej: \(error.localizedDescription)")
        finish()
    }
}
