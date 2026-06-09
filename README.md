# Lecteur TTS — Android

Application Android de synthèse vocale (Text-to-Speech) complète, en Kotlin / Jetpack Compose.

## Fonctionnalités

- **Lecture** de texte avec l'API TTS native Android (voix françaises)
- **Contrôles** play / pause / stop + service foreground (notification persistante)
- **Paramètres** : vitesse (×0.25 → ×4.0), hauteur (pitch), sélection de voix, taille historique
- **Historique** : 200 dernières entrées (configurable), sans doublons consécutifs
- **Favoris** : mise en favori depuis l'historique, libellé personnalisé
- **Partage entrant** : ouvrir depuis n'importe quelle app via *Partager → Lecteur TTS*
- **Material You** : palette dynamique Android 12+, thème sombre natif

## Stack technique

| Couche | Bibliothèque |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| DB | Room + Kotlin Flow |
| Prefs | DataStore Preferences |
| Service | Foreground Service (mediaPlayback) |
| Build | AGP 8.5 / Kotlin 2.0 / KSP |

## Compiler localement

```bash
git clone https://github.com/<toi>/tts-app.git
cd tts-app
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

> **Android Studio** : ouvrir le dossier racine, *Run > app*.

## CI/CD

GitHub Actions compile automatiquement :

- **Debug APK** sur chaque push / PR
- **Release APK** sur chaque push `main`
- **GitHub Release** sur tag `v*` (ex. `git tag v1.0.0 && git push --tags`)

Les artefacts sont disponibles dans l'onglet **Actions** du repo.

## Notes

- `minSdk 26` (Android 8.0) — requis pour `LocalDateTime`.
- La voix TTS dépend des moteurs installés sur l'appareil (Google TTS recommandé).
- Le signing release utilise le keystore debug par défaut ; pour la production, configurer un keystore dédié et l'injecter via les secrets GitHub.
