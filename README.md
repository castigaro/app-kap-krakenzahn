# Kap Krakenzahn

Ein klassisches Point-and-Click-Adventure im Stil der Piraten-Klassiker der
90er Jahre — seitliche 2D-Ansicht, eine Heldin, die der Spieler durch die
Spielwelt begleitet, Rätsel mit mehreren Lösungswegen und Spielfiguren, deren
Gespräche von einem Sprachmodell (Anthropic oder OpenAI) getragen werden.

Die App ist auf [appsonar.de](https://appsonar.de) gelistet (Kategorie
„Spiele & Geschichten"); die APK wird bei jedem Build automatisch als
GitHub-Release veröffentlicht und von der Website verlinkt.

## Worum geht es?

Die junge Möchtegern-Piratin **Jonna Klabauter** strandet am Kap Krakenzahn.
Das Leuchtfeuer ist erloschen, der alte Leuchtturmwärter **Ansgar Sturmvogel**
verschwunden, das Dorf verarmt zusehends. Der Legende nach speiste das
**Krakenauge** — ein leuchtender Edelstein aus dem Schlund der See — das
Leuchtfeuer, bis der Krake es sich zurückholte. Jonnas Ziel: das Rätsel des
alten Wärters lösen, das Krakenauge aus der Krakenhöhle bergen und das
Leuchtfeuer neu entzünden.

## Features

- **8 Schauplätze** (Hafen, Taverne, Markt, Strand, Dschungelpfad, Klippen,
  Krakenhöhle, Leuchtturm), programmatisch im 90er-**Pixel-Look mit Tiefe**
  gezeichnet (Perspektivboden, Lichtstimmung, Vignette) — **keine Bild-Assets**.
  Die Heldin läuft frei über den Boden und skaliert mit der Tiefe.
- **6 Rätsel mit je zwei bis drei Lösungswegen.** Fast alles lässt sich auf
  mehreren Wegen erreichen — Werkzeug, List, Tausch, Duell oder kluges Fragen.
- **KI-Gespräche:** Die Figuren kennen den Spielstand (was gelöst ist, was
  fehlt, das Inventar) und antworten in ihrer Rolle. Die KI erzeugt pro
  Gesprächszug **feste Antwortoptionen** — der Spieler tippt nie freien Text.
  Eine strenge **Effekt-Whitelist** stellt sicher, dass die KI Hinweise geben
  und freigeschaltete Übergaben auslösen, aber die Spiellogik nie korrumpieren
  kann.
- **Duell der spitzen Zungen:** ein KI-generiertes Wortgefecht im Stil der
  90er-Klassiker als einer der Lösungswege.
- **Wiederspielwert:** Wer welchen Hinweis kennt, wo Fundstücke liegen und
  welche Fragen der Wächter stellt, wird pro Durchlauf per Seed neu
  ausgewürfelt; dazu kommen **Zufallsereignisse** (Sturm, Springflut, der
  fahrende Händler Fielo).
- **Speichern jederzeit:** Autosave nach jeder Aktion, dazu beliebig viele
  benannte Sicherungen, zu denen man zurückkehren kann. Neues Spiel mit
  frischem Seed jederzeit möglich.
- **Sprachausgabe (TTS)** mit figurenspezifischen Stimmen sowie
  **synthetisierte Soundeffekte, Ambiente und sanfte Hintergrundmusik** —
  alles abschaltbar, Lautstärken einstellbar; während gesprochen wird,
  blenden Ambiente und Musik automatisch ab.
- **Offline spielbar:** Ohne API-Key laufen die Gespräche über feste
  Ersatz-Dialoge; das Abenteuer bleibt vollständig lösbar.

## Steuerung

Klassische SCUMM-Bedienung über die Leiste am unteren Rand: erst ein **Verb**
wählen, dann das **Ziel** antippen.

- In der Leiste ein Verb wählen — **Gehe zu**, **Schau an**, **Nimm**,
  **Benutze** oder **Rede mit** — und dann ein Ziel in der Szene antippen; die
  Heldin läuft hin und führt es aus. Die Satzzeile darüber zeigt die Handlung
  im Aufbau („Benutze Laterne mit …").
- **Gehe zu** ist voreingestellt: ein Tipp auf den Boden lässt die Heldin
  einfach dorthin laufen.
- Für **„Benutze X mit Y"** erst *Benutze* wählen, dann einen Gegenstand in der
  Inventarliste rechts antippen und schließlich das Ziel (in der Szene oder
  einen zweiten Gegenstand).
- Pfeile am Bildrand führen zu den Nachbarorten.

## Architektur

| Paket | Inhalt |
|-------|--------|
| `world/` | Spielinhalt als Kotlin-Code: `Ids`, `Model`, `GameWorld` (Orte, Items, Figuren, Rätsel, Regeln, Kombinationen, Dialog-Whitelist), `Hints`, `FallbackDialogs`, `FallbackDuel` |
| `game/`  | `GameEngine` (Regelanwendung), `Variation` (Seed-Randomisierung), `EventDirector` (Zufallsereignisse), `StateDigest`, `DuelController` |
| `render/`| `SceneView` (Canvas-Engine mit Laufen/Hotspots/Verben), `HeroSprite`, `PaintKit`, `ItemIconView`, `TitleView`, `scenes/` (8 Szenen-Painter) |
| `api/`   | `LlmClient` (Anthropic/OpenAI wie die anderen AppSonar-Apps), `DialogApi`, `DuelApi` |
| `data/`  | `GameState`, `SaveStore` (JSON-Datei), `AppSettings` |
| `audio/` | `Speaker` (TTS), `Sfx`, `Ambience` (synthetisiert) |

Die KI-Anbindung nutzt die gemeinsame Bibliothek
[`android-apps-common`](https://github.com/appsonar/android-apps-common)
(`ProviderSettings`, `ModelPricing`, `ProviderSettingsController`) — API-Keys
für Anthropic/OpenAI werden wie in den anderen AppSonar-Apps in den
Einstellungen hinterlegt.

## Build & Test

```bash
./gradlew testDebugUnitTest     # Unit-Tests (Spiellogik, Speicher, KI-Parsing)
./gradlew assembleDebug         # Debug-APK
```

Die Release-Variante wird von der CI (`.github/workflows/build.yml`)
signiert gebaut und als GitHub-Release `latest` (Pre-Release)
veröffentlicht. Zum Installieren auf dem Handy: APK herunterladen, öffnen und
die Installation aus unbekannten Quellen erlauben. Für lebendige KI-Dialoge in
den App-Einstellungen einen API-Key hinterlegen.

## Lizenz

Copyright © 2026 Torsten Klein

Dieses Projekt steht unter der **GNU Affero General Public License v3.0 oder
später** (AGPL-3.0-or-later), siehe [LICENSE](LICENSE): Wer den Code — auch als
Netzwerkdienst — weiterverwendet oder verändert, muss den Quellcode unter
derselben Lizenz offenlegen. Eine kommerzielle Lizenz ist auf Anfrage möglich.
