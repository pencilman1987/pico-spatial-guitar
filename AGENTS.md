# Spatial Guitar project guidance

## Current product

This is a native PICO OS 6 Spatial SDK 0.13.3 guitar simulator. It launches as
a Volumetric `DefaultWindowContainer` in Shared Space and preserves the legacy
Web simulator's six strings, frets 0–15, press-and-drag traversal, dark
fretboard, metallic strings, fret markers, and electric-blue strike feedback.
The default play mode is now guided sing-along. Two redistributable original
practice songs show current/next lyrics and chords; every four strums advances
the lyric and chord automatically, and transpose changes both labels and audio
from -5 to +6 semitones. Free accompaniment keeps seven one-tap chords, and one
broad sound-hole collider behaves like a stringless guitar strum paddle. First
contact always sounds; low-speed motion, direction reversal, and deliberate
re-entry retrigger the selected chord. The previous 96-target per-note
interaction remains available as Solo mode.
The compact status attachment provides runtime A/B timbre selection: A is the
default CC0 FreePats nylon set and B is the optional CC0 Martin HD28 steel set.
The same attachment has an explicit placement mode: while enabled, dragging the
guitar moves its artwork, playable ECS root, and status panel as one bounded
group; Center restores the authored origin. Placement input and performance
input are mutually exclusive so repositioning cannot sound notes.
The current art direction uses an image-generated, transparent orthographic
acoustic-guitar artwork (`drawable-nodpi/acoustic_guitar_front_v1.png`) for an
accurate silhouette and wood finish. ECS renders only the six playable strings,
their hit boxes, and the brief blue string/sound-hole resonance feedback.

The legacy Web app in `../frontend` and the sibling `../pico-spatial-drum`
project are source references only. Never overwrite or merge them into this
project.

## Architecture

- `Main.kt`: thin Shared Space launcher.
- `ui/home/`: MVI-lite state, events, ViewModel, screen and UI components.
- `domain/model/GuitarSong.kt`: original lyric/chord cues and measure lengths.
- `ui/home/GuitarPlacement.kt`: accumulated meter offsets, recoverable bounds,
  and the centered placement baseline.
- `ui/home/GuitarStrikeMotionTracker.kt`: X-axis rejection, 48 ms directional
  smoothing, and speed hysteresis for world-pose input.
- `ui/home/EasyStrumDetector.kt`: forgiving accompaniment-pad contact,
  low-speed stroke, reversal, re-entry, and repeat state machine.
- `ui/home/GuitarPointerStrikeDetector.kt`: per-pointer target hysteresis and
  tap fallback, independent across hands/controllers.
- `scene/GuitarRuntime.kt`: ECS geometry, colliders, target mapping and feedback.
- `scene/GuitarFretLayout.kt`: physical 12-tone fret spacing aligned from nut to bridge.
- `scene/GuitarSpatialLayout.kt`: the single source of truth for string order,
  hit-box spacing, and artwork/string depth.
- `audio/GuitarAudioEngine.kt`: licensed multisample-first playback, telemetry, and per-string voices.
- `audio/GuitarSamplePool.kt`: asynchronously predecodes both low-latency
  SoundPool timbres and reports per-timbre ready time.
- `audio/GuitarSampleMap.kt`: E2-G5 mappings and measured A/B output gains for
  FreePats nylon and Martin HD28 steel.
- `audio/GuitarStringSynthesizer.kt`: naturally decaying emergency fallback PCM generation.
- `domain/`: fret targeting and note calculation rules.
- `data/repository/`: standard-tuning source.

## Spatial rules

- Keep the default experience in Shared Space; do not add raw
  `HandTrackingProvider` calls to this WindowContainer flow.
- All 2D UI must use `com.pico.spatial.ui.design.*` and `PicoTheme`; never add
  Material or Material3.
- The launcher intentionally sets
  `pico.spatial.windowcontainer.materialbackground=0` so the 3D instrument root
  is transparent. Glass belongs only to attachment/control panels.
- Every playable hit target requires `CollisionComponent`,
  `InteractableComponent`, and `HoverEffectComponent`.
- Keep accompaniment as the default. Its sound-hole target is intentionally a
  broad `0.25 x 0.19 x 0.03 m` local box and uses `0.055/0.022 m/s`
  enter/exit thresholds plus a guaranteed first-contact chord. Do not shrink it
  to visual string thickness or require a precise fret hit.
- Keep the explicit placement toggle. In placement mode, route drag gestures to
  the artwork, strings, and invisible body/neck move surface; in performance
  mode, restore the original per-string recognizer. Convert Compose drag pixels
  through `LocalDensity` and `LocalPhysicalLengthConverter` before applying ECS
  meter transforms, and invert view-space Y for ECS Y.
- Derive strike velocity from `inputDevicePose.rawPosition` world motion in
  meters; `SpatialPointerInfo.x/y` are Compose pixels and must not feed the
  meters-per-second curve. Keep Poke and ray/pinch input supported through the
  single low-level pointer recognizer.
- Since the current guitar root is unrotated and strings run along X, project
  velocity onto Y/Z before calculating gain. Keep the 48 ms smoothing window,
  `0.10/0.045 m/s` enter/exit hysteresis, and 32 ms target-exit grace together
  unless a physical-device calibration deliberately changes them.
- Invoke audio synchronously from the hit callback before submitting UI state.
- Keep six continuous visible strings, while the 96 invisible fret targets
  share one collision shape and one physics material.
- Keep ECS transforms in meters, use simple collision boxes, and destroy all
  entities/resources when the composable leaves composition.
- Protect the fretboard as the primary playfield. Keep the sole persistent HUD
  attachment compact and above the upper bout; do not restore a centered
  dashboard, permanent controls list, or large reset button.

## Build and run

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
pico-cli app install app/build/outputs/apk/debug/app-debug.apk
pico-cli app launch com.haisnap.spatialguitar --activity .platform.LaunchActivity
```

Run on a physical PICO OS 6 device for final Poke and audio-latency tuning.

Latest host verification: 56 `testDebugUnitTest` tests, `lintDebug`,
`assembleDebug`, and the SpatialUI design-style verifier pass. The 48 CC0 WAV
assets are stored uncompressed in the APK. Final-build install/launch succeeds
on `emulator-5554`; both timbres decode without errors in about 5.2 seconds on
that emulator, the app process remains alive, and its fresh crash buffer is
empty. The latest unobstructed guided-panel reference is
`artifacts/guided-sing-along-settled.png`; later capture attempts were obscured
by the emulator's system launcher/another app's stale ANR, not by a guitar-app
failure. No sample-decoding or lint error was observed. Lint still reports
pre-existing SDK/dependency-version and unused-resource warnings. Shared-resource target
creation takes about 176 ms on that emulator, down from about 3.6 seconds. The
CLI cannot automate volumetric controller/Poke dragging or string strikes, so
final broad-pad hit comfort, placement feel, directional dynamics, and perceived latency still require
the physical-device play pass in
`docs/pico-audio-calibration.md`.

## Natural next steps

- Calibrate gesture velocity and end-to-end acoustic latency on a physical PICO headset.
- Add an optional count-in/metronome or simple drum loop after the broad
  accompaniment pad is physically validated.
- Run the A/B headset protocol in `docs/audio-ab-comparison.md`, then decide
  whether B should remain optional. Continue looking for an acoustic set that
  also has real velocity layers and per-string samples.
- Add an optional Full Space performance Stage only if raw two-hand tracking is
  explicitly requested.
