# Easy accompaniment design

## Product goal

The default experience is not a strict guitar simulator. It is a virtual
backing instrument for people who want to sing while creating their own chord
rhythm. A useful first session should require only two ideas:

1. Choose a chord.
2. Sweep the sound-hole area to play it.

## Market patterns used

- LiberLive C1 separates chord selection from a dedicated strum paddle and
  describes the flow as press a chord, strum, then follow lyrics/chord sheets:
  https://liberlive.com/products/liberlive-c1-stringless-smart-guitar
- LAVA GENIE uses its touch fretboard for chords, separate triggers for rhythm,
  and a swipe area for arpeggios:
  https://www.lava-music.de/en/lava-genie-in-black/LVGENIEBK
- AeroBand Smart Mode simplifies left-hand and right-hand actions, maps up to
  seven song chords, and offers a performance setting where keeping rhythm can
  produce the intended chord:
  https://helpcenter.aeroband.net/hc/en-us/articles/56273183800985-AeroBand-Guitar-Smart-Mode-Quick-Start-Guide

The common principle is that beginners control harmony and rhythm, while the
instrument handles voicing and detailed string selection.

## Spatial Guitar mapping

- Default mode: `ACCOMPANIMENT`.
- Harmony: seven one-touch presets — C, G, Am, F, Em, Dm, E.
- Rhythm input: one body-sized local `0.44 x 0.34 x 0.16 m` safety volume
  around the sound hole (about `0.58 x 0.45 x 0.21 m` after root scale and
  reaching about `0.14 m` in front of the guitar). Users sweep through the
  middle of the body instead of aiming at the six thin visual strings.
- Accessibility: first contact and deliberate re-entry always sound without a
  speed requirement; slow `0.055 m/s` strokes, direction reversals, and a
  sustained sweep can retrigger.
- Output: the chosen six-string chord voicing is triggered at a minimum audible
  gain. All chord fallback voices are prepared off the UI thread.
- Advanced mode: `SOLO` restores the 96 original string/fret targets and the
  stricter world-pose velocity curve.

## Deferred until physical-device validation

- Metronome, drum loop, and style presets.
- Physical-headset tuning of the safety volume, gesture curve, and latency.
