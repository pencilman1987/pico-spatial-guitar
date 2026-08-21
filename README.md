# Spatial Guitar

PICO OS 6 / Spatial SDK 0.13.3 原生 Shared Space 吉他模拟器。

## 当前体验

- Volumetric `DefaultWindowContainer`，默认运行在 Shared Space。
- 6 根琴弦、0–15 品，共 96 个可交互音区。
- 默认直接进入“跟唱《一起唱》”：面板同时显示当前歌词、当前/下一和弦和剩余扫弦次数。每扫 4 下自动换和弦并进入下一句，用户无需做精细按弦动作。
- 内置《一起唱》和《晚风》两首原创练习歌，支持 -5 到 +6 个半音的即时升降调；界面和实际和弦声高会一起变化。
- “自由”伴奏保留 C / G / Am / F / Em / Dm / E 手动和弦。琴身中部有约 `58 × 45 × 21 cm` 的宽容错安全区，手在琴面前约 14 cm 内扫过即可发声，不必真正碰到六根视觉琴弦；首次轻触必定发声，慢速上下往返也可持续扫和弦。
- “单音模式”保留原来的逐弦玩法：点按演奏，或按住空间指针 / Poke 横跨琴弦扫弦；沿琴弦方向移动不会被误算成拨弦力度。
- 状态面板提供独立“移动”模式：开启后可直接拖动琴身调整吉他、琴弦和面板的整体空间位置；“居中”可随时恢复默认位置。移动与演奏输入互斥，避免调位置时误触发琴音。
- 保留原 Web 版的黑灰指板、银/铜琴弦、品位圆点、蓝色触发高亮和玻璃状态面板。
- 默认 A 保留随 APK 分发的 FreePats CC0 尼龙弦多采样；状态面板可即时切换到 B/CC0 Martin HD28 钢弦，切换只影响后续拨弦，不重建场景且完全离线。
- A/B 已做响度匹配：钢弦 WAV 透明峰值归一化到 −0.5 dBFS，尼龙播放增益为 `0.85`；两者 0.5 秒起音中位 RMS 约为 −13.47/−13.50 dBFS。
- 采样尚未解码完成或播放失败时，自动回退到本地 Karplus–Strong 拨弦合成。
- 控制器/手部 `inputDevicePose.rawPosition` 以米为单位计算真实空间速度；只投影到垂直琴弦的 Y/Z 平面，经 48 ms 平滑和 `0.10/0.045 m/s` 双阈值迟滞后，再映射到源版的 `0.5..1.0` 力度范围。
- 命中回调先直接触发低延迟音频，再异步提交音名与力度 UI 状态，避免 Compose 重组进入发声关键路径。
- 视觉层是 6 根连续琴弦；96 个音区只保留轻量交互实体，并共享碰撞 `ShapeResource` 与 `PhysicsMaterialResource`。
- 弦序按真实正面视角排列：低音 E 在上、高音 E 在下；命中区互不重叠，弦面只比背景画面前置约 4.6 mm（最终世界尺度）。

## 架构

- `Main.kt`：Shared Space Volumetric 入口。
- `ui/home/`：MVI-lite 状态、事件和页面组合。
- `ui/home/EasyStrumDetector.kt`：伴奏模式的首次触碰、低速扫弦、反向重触发和防抖状态机。
- `ui/home/GuitarPlacement.kt`：空间位移累计、单位换算后的边界限制和居中基准。
- `scene/GuitarRuntime.kt`：ECS 几何、96 个碰撞目标及视觉反馈。
- `scene/GuitarSpatialLayout.kt`：琴弦顺序、命中高度和画面/琴弦深度的单一布局基准。
- `audio/GuitarAudioEngine.kt`：低延迟多采样优先、算法音色兜底与触发遥测。
- `audio/GuitarSampleMap.kt`：FreePats 尼龙弦与 Martin HD28 钢弦的 E2–G5 映射、A/B 输出增益。
- `domain/`：调弦、品位和 MIDI 音名规则。
- `domain/model/GuitarSong.kt`：可分发的原创歌词、和弦进行与每句扫弦数。
- `data/repository/`：标准调弦数据源。

## 构建

```bash
./gradlew assembleDebug
```

## 安装与启动

```bash
pico-cli app install app/build/outputs/apk/debug/app-debug.apk
pico-cli app launch com.haisnap.spatialguitar --activity .platform.LaunchActivity
```

真机验证重点是大范围伴奏扫弦、琴身空间拖动、Poke 命中舒适度、快速扫弦去重、速度曲线和端到端音频延迟。
具体校准步骤见 `docs/pico-audio-calibration.md`，第三方资源说明见
`THIRD_PARTY_NOTICES.md`；后续钢弦音源筛选见
`docs/steel-string-source-research.md`，本次 A/B 基准见
`docs/audio-ab-comparison.md`，轻松伴奏模式的产品依据见
`docs/easy-accompaniment-design.md`。
