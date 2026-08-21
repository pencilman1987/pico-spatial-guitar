# PICO physical-device audio calibration

The emulator verifies packaging, decoding, lifecycle, and the Spatial scene. It
cannot establish perceived acoustic latency or representative Poke/controller
motion speed, so the following pass must be run on a physical PICO OS 6 device.

## Current baseline

- Gesture gain range: `0.50..1.00`, matching the legacy Web app.
- Full-gain motion speed: `1.20 m/s`.
- Curve exponent: `0.72`.
- Poke boost: `+0.04`.
- Audio path: predecoded `SoundPool`, 12 concurrent streams, one sustained note
  per physical guitar string.
- Timbre path: A/nylon remains the default; B/steel changes subsequent playback
  without rebuilding ECS. Emulator readiness baseline is 2,618 ms for A and
  2,934 ms for B when both sets preload together.
- Motion source: `inputDevicePose.rawPosition`, sampled in Spatial world meters;
  the Compose pointer `x/y` pixel coordinates are intentionally ignored.
- Direction model: strings run along X, so only Y/Z velocity contributes to a
  strike. A 48 ms time-weighted window smooths device jitter; motion arms at
  `0.10 m/s` and disarms at `0.045 m/s`; a target miss shorter than 32 ms is
  treated as collider-edge jitter rather than a new strike.
- Runtime tag: `SpatialGuitarInput` records `direction`,
  `string_normal_speed_mps`, gain, and target after the audio command has been
  issued. Use it to compare downstroke, upstroke, and Poke behavior.
- Runtime tag: `SpatialGuitarAudio` records `pointer_to_audio_command_ms` from the
  SDK input timestamp to the audio command and `engine_command_us` spent inside
  the playback engine, plus `timbre`, `input_gain`, `output_gain`, and sample
  `source`. These are scheduling telemetry, not speaker-output latency.
- The hit callback invokes audio before it submits UI state, so Compose
  recomposition is not part of the audio-command path.

## Device pass

1. Install a release-like build on the target PICO headset and launch in Shared
   Space.
2. Record at least 20 gentle, 20 normal, and 20 hard strokes for both Poke and
   controller ray/pinch. Confirm that normal strokes occupy the middle of the
   loudness range and hard strokes do not reach full gain too early.
3. Capture `SpatialGuitarAudio` log lines while performing single notes, six-note
   sweeps, and repeated strikes on one string. Repeat the same sequence for A
   and B without changing headset volume.
4. Measure end-to-end acoustic latency with a high-frame-rate camera or a
   loopback microphone: count from visible contact to waveform onset. Do not use
   `pointer_to_audio_command_ms` or `engine_command_us` as the end-to-end result.
5. Tune `FULL_VELOCITY_SPEED_MPS`, `VELOCITY_EXPONENT`, and `POKE_GAIN_BOOST` in
   `GuitarGestureVelocity.kt`; tune the smoothing, enter, and exit thresholds in
   `GuitarStrikeMotionTracker.kt`. Repeat until Poke and ray/pinch feel equally
   controllable without false retriggers.

Acceptance target: no dropped notes in a six-string sweep, no audible cut on a
different string, stable soft/normal/hard separation, and a documented median
and p95 acoustic latency for the actual headset model.
