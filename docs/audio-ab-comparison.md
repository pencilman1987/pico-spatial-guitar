# Offline guitar timbre A/B baseline

## Runtime choices

| Choice | Source | Default | Root samples | Runtime gain |
| --- | --- | --- | ---: | ---: |
| A — Nylon | FreePats Spanish classical guitar, CC0 | Yes | 34 | 0.85 |
| B — Steel | 2017 Martin HD28 by Jeff Learman, CC0 | No | 14 | 1.00 |

Both sets cover every playable MIDI note from 40 through 79. Selecting a chip
changes only subsequent notes; the ECS guitar and already-sustaining streams are
not rebuilt.

## Measured host baseline

Measurements use the first 0.5 seconds after a −50 dBFS onset threshold. Steel
files were multiplied by a constant per-file gain to reach −0.5 dBFS peak; no
compression, limiting, EQ, trimming, or resampling was used.

| Metric | A — Nylon | B — Steel |
| --- | ---: | ---: |
| Bundled WAV bytes | 9,405,924 | 3,087,286 |
| Median attack RMS after runtime gain | −13.47 dBFS | −13.50 dBFS |
| Highest resulting peak | about −1.47 dBFS | −0.50 dBFS |
| Emulator ready-after-prepare | 2,618 ms | 2,934 ms |

The two ready times come from one PICO emulator cold launch, so they are a
packaging/decoding regression check, not a physical-headset performance claim.

## APK impact

- Previous debug APK: 43,810,874 bytes.
- A/B debug APK: 46,918,027 bytes.
- Increase: 3,107,153 bytes / 2.96 MiB / 7.09%.
- All 48 WAV files are stored uncompressed so `SoundPool` can open them through
  `AssetFileDescriptor` without extraction.

## Realism interpretation

B objectively matches the visible instrument category better because it is a
recorded steel-string acoustic guitar. A has denser chromatic sampling and
remains the stable default. B has one recorded layer per root zone, no round
robins, and no physical-string identity, so a headset listening pass must still
judge attack character, pitch-shift artifacts, sustain, and six-note sweeps.

## Physical-headset protocol

1. Use the same headset volume and the same normal-strength stroke.
2. Alternate A/B every five single notes across E2, E3, E4, and G5.
3. Repeat five downstrokes and five upstrokes across all six strings.
4. Compare body resonance, metallic attack, pitch-shift artifacts, note decay,
   apparent loudness, dropped notes, and perceived latency.
5. Capture `SpatialGuitarAudio`; confirm `timbre`, `input_gain`, `output_gain`,
   and `source` change after each chip selection.
