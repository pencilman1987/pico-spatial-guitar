# Third-party notices

## FreePats Spanish classical guitar

- Source: <https://freepats.zenvoid.org/Guitar/acoustic-guitar.html>
- Upstream package: `SpanishClassicalGuitar-SFZ-20190618.7z`
- Included subset: 34 mono PCM WAV samples covering MIDI 40–79 (E2–G5)
- Author/source statement: self-recorded Spanish classical guitar samples by the FreePats project
- License: CC0 1.0 Universal (public-domain dedication)

The upstream `readme.txt`, `cc0.txt`, and complete SFZ mapping are preserved in
`app/src/main/assets/licenses/freepats_spanish_classical_guitar/`. The runtime
mapping in `GuitarSampleMap.kt` is a direct E2–G5 subset of that SFZ file.

## Discord SFZ GM Bank — Martin HD28 steel-string guitar

- Official source: <https://github.com/sfzinstruments/Discord-SFZ-GM-Bank>
- Source commit: `7a9c478fe331f94f246d33332f0adedb25bbbe27`
- Upstream instrument: `Discord GM/Melodic/026-Acoustic Guitar (steel).sfz`
- Included subset: 14 mono PCM WAV samples covering MIDI 40–79 (E2–G5)
- Instrument/author: 2017 Martin HD28 Vintage Series, Jeff Learman
- License declared in the upstream SFZ: CC0 1.0 Universal

The bundled copies use transparent per-file linear peak normalization to
−0.5 dBFS. Source and bundled SHA-256 hashes, the unmodified SFZ mapping, CC0
text, and processing details are preserved in
`app/src/main/assets/licenses/discord_martin_hd28_steel/`.
