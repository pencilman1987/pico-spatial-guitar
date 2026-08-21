# Steel-string sample-source research

Goal: find an offline sample set that can be redistributed inside the APK, has
an acoustic steel-string timbre, multiple real velocity layers, and preferably
records the same pitch on different physical strings separately.

## Current decision

Keep the bundled CC0 FreePats nylon-string set as default A. The CC0 Martin HD28
candidate is now bundled as optional B for direct runtime comparison; it has not
replaced A. No reviewed candidate satisfies all four long-term goals.

## Reviewed candidates

### Martin HD28 — preferred steel-string baseline

- Source: [Discord SFZ GM Bank instrument](https://github.com/sfzinstruments/Discord-SFZ-GM-Bank/blob/master/Discord%20GM/Melodic/026-Acoustic%20Guitar%20%28steel%29.sfz)
- Provenance: the SFZ identifies a 2017 Martin HD28 Vintage Series, author Jeff
  Learman, and Creative Commons CC0.
- Coverage: 15 samples from MIDI E2 through B5, enough for this application's
  E2-G5 range.
- Limitation: one recorded layer per sample zone; velocity changes a low-pass
  filter rather than selecting real soft/medium/hard recordings. There is no
  per-physical-string mapping.
- Decision: integrated as optional B with transparent peak normalization and a
  documented A/B baseline, but not promoted to the default or treated as the
  final expressive library.

### Karoryfer Emilyguitar — expressive but wrong instrument type

- Source: [official repository](https://github.com/sfzinstruments/karoryfer.emilyguitar)
  and [CC0 license](https://github.com/sfzinstruments/karoryfer.emilyguitar/blob/master/LICENSE).
- Strength: four velocity layers, three round robins, release samples, and
  explicit CC0 licensing.
- Limitation: an Epiphone electric guitar recorded directly through pickups;
  it does not match the acoustic steel-string artwork or expected body tone.
- Decision: do not integrate into this acoustic-guitar product.

### FreePats FSS steel-string — acoustic but license-coupled

- Source: [FreePats steel-string page](https://freepats.zenvoid.org/Guitar/steel-acoustic-guitar.html).
- Strength: true steel-string acoustic source and compact SFZ/WAV distribution.
- Limitation: GPLv3-or-later with a special exception, not the permissive CC0
  chain used by the current APK. It also does not provide the desired real
  multi-velocity/per-string matrix.
- Decision: do not bundle without a deliberate application-license review.

## Acceptance gate for a later replacement

1. Preserve the upstream license and source URL beside the files and in
   `THIRD_PARTY_NOTICES.md`.
2. Verify redistribution and commercial-use rights from the author or official
   repository, not from a mirror or forum post.
3. Normalize samples to a consistent PCM format and loudness without clipping.
4. Map velocity layers with crossfade-free thresholds and retain physical-string
   identity where the source provides it.
5. Compare APK size, decode time, dropped-note behavior, and headset latency
   against the current CC0 baseline before switching defaults.
