# Lista poprawek — crashe z Crashlytics (wersja 2.0.1)

Kontekst: projekt Kotlin Multiplatform (KMP), moduły `androidApp` (Android host) i `shared`
(commonMain/androidMain/iosMain). Paczka: `mivs.niewolnik_maryi`.

---

## 1. [PRIORYTET WYSOKI] ClassCastException w migracji danych — 16 zdarzeń, 4 użytkowników

**Plik:** `shared/src/commonMain/kotlin/com/example/slaveofmary/App.kt`

**Błąd:**
```
java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Float
    at com.russhwolf.settings.SharedPreferencesSettings.getFloat(SharedPreferencesSettings.kt:141)
    at com.example.slaveofmary.AppKt.migrateOldAppData(App.kt:193)
```

**Przyczyna:** Funkcja `migrateOldAppData()` migruje stare klucze SharedPreferences
(format `"$idx$i"`, np. `"00"`, `"01"`, `"40"`) zapisane jako `Float` na nowe klucze
(`pref_day_${idx}_$i`, `pref_nowenna_0_$i`). Na części urządzeń (obserwowany przypadek:
Samsung po Smart Switch / przywróceniu z kopii zapasowej systemowej) wartość pod tym
samym kluczem zostaje zapisana jako `String` zamiast `Float` — `getFloat()` rzuca
`ClassCastException`. Ponieważ flaga `is_migrated_v2` ustawiana jest dopiero na końcu
funkcji, wyjątek w środku pętli powoduje, że migracja (i crash) powtarza się przy
KAŻDYM kolejnym uruchomieniu aplikacji — realny crash loop blokujący dostęp do apki.

**Zadanie:**
Zmień funkcję `migrateOldAppData` tak, by odczyt każdej starej wartości był
dwustopniowo zabezpieczony:
1. Spróbuj `settings.getFloat(key, 1.0f)`.
2. Jeśli rzuci wyjątek — spróbuj odzyskać wartość przez `settings.getStringOrNull(key)?.toFloatOrNull()`
   (bo w znanym przypadku Samsunga to wciąż ta sama liczba, tylko zapisana jako tekst — dzięki
   temu odzyskujemy realny postęp użytkownika zamiast go tracić).
3. Jeśli i to zawiedzie — zwróć bezpieczną wartość domyślną `1.0f`, bez wyjątku.

Owiń to w lokalną funkcję pomocniczą `safeGetFloat(key: String): Float` wewnątrz
`migrateOldAppData` i użyj jej we wszystkich miejscach, gdzie obecnie wywoływane jest
`settings.getFloat(...)` w tej funkcji (pętla migracji Traktatu i pętla migracji Nowenny).

Dodaj komentarz wyjaśniający przyczynę (Samsung backup/restore corruption) i dlaczego
flaga `is_migrated_v2` musi zostać ustawiona na końcu niezależnie od wyniku odczytu
poszczególnych kluczy.

**Kryterium akceptacji:** Build przechodzi, funkcja nie rzuca wyjątków dla żadnego typu
wartości pod starymi kluczami (Float, String, brak klucza), flaga migracji zawsze
zostaje ustawiona po jednorazowym przebiegu pętli.

---

## 2. [PRIORYTET ŚREDNI] NoSuchMethodError w WindowInsetsCompat — 1 zdarzenie

**Błąd:**
```
java.lang.NoSuchMethodError: No static method systemOverlays()...
    at androidx.core.view.WindowInsetsCompat$TypeImpl34.toPlatformType(WindowInsetsCompat.java:2880)
```

**Przyczyna:** Niezgodność wersji `androidx.core` z API systemowym urządzenia — metoda
używana przez zainstalowaną wersję `androidx-core` nie istnieje na tym urządzeniu/API.

**Zadanie:**
1. Sprawdź aktualną wersję `androidx-core` w `gradle/libs.versions.toml` (klucz `androidx-core`).
2. Sprawdź najnowszą stabilną wersję `androidx.core:core-ktx` (np. przez `./gradlew androidApp:dependencyUpdates`
   jeśli dostępny jest plugin Versions, albo ręcznie na maven central).
3. Zaktualizuj wersję w `libs.versions.toml`, zsynchronizuj gradle, zbuduj projekt i upewnij
   się że nic się nie wysypało (szczególnie kod związany z `enableEdgeToEdge()` w `MainActivity.kt`).

**Kryterium akceptacji:** Projekt buduje się z nową wersją `androidx-core`, `enableEdgeToEdge()`
w `MainActivity.kt` nadal działa bez regresji wizualnych.

---

## 3. [PRIORYTET ŚREDNI] NoSuchMethodError w Google Play Billing — 1 zdarzenie

**Błąd:**
```
java.lang.NoSuchMethodError: No virtual method setPendingIntentBackgroundActivityStartMode(I)...
    at com.android.billingclient.api.ProxyBillingActivity.onCreate
```

**Przyczyna:** Niezgodność wersji `com.android.billingclient:billing-ktx` z API urządzenia/
usługą Google Play na danym telefonie — metoda dodana w nowszej wersji API systemowego
Androida, a używana biblioteka Billing zakłada jej obecność bez odpowiedniego fallbacku.

**Zadanie:**
1. Sprawdź aktualną wersję `billing` w `gradle/libs.versions.toml`.
2. Sprawdź najnowszą stabilną wersję Google Play Billing Library (obecnie w projekcie:
   sprawdź `billing = "9.1.0"` jako punkt odniesienia — zweryfikuj czy istnieje nowsza).
3. Zaktualizuj, zsynchronizuj, przebuduj.
4. Po aktualizacji przetestuj ręcznie pełny flow zakupu subskrypcji (miesięcznej i rocznej)
   z `BillingManager.kt` / `AndroidBillingController.kt` — upgrade Billing Library czasem
   zmienia sygnatury API (`queryProductDetailsAsync`, `launchBillingFlow`), więc zweryfikuj
   że kod się nadal kompiluje bez zmian, a jeśli nie — dostosuj wywołania do nowego API.

**Kryterium akceptacji:** Projekt buduje się z nową wersją Billing Library, subskrypcja
miesięczna i roczna nadal poprawnie się uruchamiają na urządzeniu testowym.

---

## 4. [INFORMACYJNE — prawdopodobnie nie do naprawienia po naszej stronie] — 1 zdarzenie

**Błąd:**
```
java.lang.IllegalStateException: targetPackageName is null
    at com.google.android.play.core.hsdp.service.HsdpShimActivity.zzd
```

**Przyczyna:** To wnętrze biblioteki Google Play Core (mechanizm in-app update / deep-link
do Sklepu Play — prawdopodobnie powiązane z `AppUpdateManager` w `MainActivity.kt`), nie
nasz kod. To znany, sporadyczny błąd samej biblioteki Google, niezależny od logiki aplikacji.

**Zadanie:** Nie podejmuj zmian w kodzie. Sprawdź tylko, czy `com.google.android.play:app-update`
w `libs.versions.toml` jest na najnowszej wersji (klucz `playUpdate`) — jeśli nie, zaktualizuj,
ale nie oczekuj że to naprawi ten konkretny błąd. Jeśli częstotliwość tego crasha wzrośnie
w przyszłości, wróć do tego z osobnym śledztwem.

---

## Kolejność wykonania

Wykonaj **zadanie 1 jako pierwsze i samodzielnie** (to jedyny prawdziwy crash loop blokujący
użytkowników) — po nim zatrzymaj się i poczekaj na potwierdzenie przed przejściem do zadań
2 i 3, ponieważ każde z nich wymaga aktualizacji zależności zewnętrznych i osobnego,
ręcznego testu na urządzeniu (subskrypcje, edge-to-edge UI) zanim trafi do kolejnego wydania.