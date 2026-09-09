import Foundation
import UIKit
import UserMessagingPlatform
import AppTrackingTransparency
import GoogleMobileAds

/// Kolejność wymagana przez Google i Apple:
/// 1. UMP — zgoda na reklamy spersonalizowane (RODO/UOKiK, EOG + UK).
/// 2. ATT (App Tracking Transparency) — systemowy prompt Apple o śledzeniu.
/// 3. Dopiero po obu: inicjalizacja Google Mobile Ads SDK i ładowanie reklam.
final class ConsentManager {
    static let shared = ConsentManager()
    private init() {}

    private var isMobileAdsStarted = false

    /// Wywołaj raz, na starcie aplikacji (patrz iOSApp.swift).
    func start() {
        let parameters = RequestParameters()
        // Do testów spoza EOG można wymusić geografię EOG, analogicznie do Androida:
        // let debugSettings = DebugSettings()
        // debugSettings.testDeviceIdentifiers = ["TWOJE_ID_URZADZENIA_TESTOWEGO"]
        // debugSettings.geography = .EEA
        // parameters.debugSettings = debugSettings

        ConsentInformation.shared.requestConsentInfoUpdate(with: parameters) { [weak self] error in
            guard let self = self else { return }

            if let error = error {
                print("Błąd aktualizacji informacji o zgodzie UMP: \(error.localizedDescription)")
                self.proceedIfAllowedToRequestAds()
                return
            }

            guard let rootViewController = Self.currentRootViewController() else {
                print("Brak rootViewController — pomijam formularz UMP w tym uruchomieniu.")
                self.proceedIfAllowedToRequestAds()
                return
            }

            ConsentForm.loadAndPresentIfRequired(from: rootViewController) { [weak self] formError in
                if let formError = formError {
                    print("Błąd formularza zgody UMP: \(formError.localizedDescription)")
                }
                self?.proceedIfAllowedToRequestAds()
            }
        }
    }

    private func proceedIfAllowedToRequestAds() {
        guard ConsentInformation.shared.canRequestAds else {
            print("Brak zgody na reklamy — SDK reklam nie zostanie zainicjalizowany w tym uruchomieniu.")
            return
        }
        requestTrackingAuthorizationThenInitAds()
    }

    private func requestTrackingAuthorizationThenInitAds() {
        if #available(iOS 14, *) {
            // Apple wymaga wywołania na głównym wątku.
            DispatchQueue.main.async { [weak self] in
                ATTrackingManager.requestTrackingAuthorization { _ in
                    // Status (authorized/denied/restricted/notDetermined) nie blokuje inicjalizacji SDK —
                    // bez zgody AdMob sam ograniczy się do reklam nieprofilowanych (bez IDFA).
                    self?.initializeMobileAdsSdk()
                }
            }
        } else {
            initializeMobileAdsSdk()
        }
    }

    private func initializeMobileAdsSdk() {
        guard !isMobileAdsStarted else { return }
        isMobileAdsStarted = true

        MobileAds.shared.start { [weak self] _ in
            print("Google Mobile Ads SDK zainicjalizowany.")
            self?.onAdsSdkReady?()
        }
    }

    /// Wywoływane po zakończeniu całego łańcucha (UMP -> ATT -> init SDK).
    /// Ustaw np. na AppOpenAdHelper.shared.loadAndShow, jeśli chcesz od razu
    /// pokazać reklamę startową — patrz komentarz w iOSApp.swift.
    var onAdsSdkReady: (() -> Void)?

    private static func currentRootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .first?.windows
        .first(where: { $0.isKeyWindow })?.rootViewController
    }
}