# Martin HD28 steel-string sample provenance

- Official repository: <https://github.com/sfzinstruments/Discord-SFZ-GM-Bank>
- Source commit: `7a9c478fe331f94f246d33332f0adedb25bbbe27`
- Upstream instrument: `Discord GM/Melodic/026-Acoustic Guitar (steel).sfz`
- Instrument/recording: 2017 Martin HD28 Vintage Series
- Author: Jeff Learman
- License declared in the upstream instrument SFZ: Creative Commons CC0
- Included subset: 14 PCM WAV roots used to cover MIDI 40–79 (E2–G5)

The upstream B5 root is outside the application's playable range and is not
bundled. The original SFZ is preserved beside this file for mapping and license
evidence. `cc0.txt` contains the CC0 1.0 legal text.

## Transparent volume preparation

The upstream WAV files are 44.1 kHz, 16-bit, mono PCM. Each included file was
multiplied by one constant linear gain so its absolute peak reaches −0.5 dBFS.
No compression, limiting, equalization, trimming, resampling, or format change
was applied. Runtime A/B comparison attenuates the nylon set by `0.85`, yielding
measured median 0.5-second attack RMS values of approximately −13.47 dBFS for A
and −13.50 dBFS for B.

## Original download SHA-256

```text
3d80eb25aa8cbe8d1d2b5971d42a63df279c5429437df6267422ceef1c75b50f  MartinGM2_040__E2_1.wav
574f9b934a6965cfbb9eee1db6918a7bc47ea8577c321ac98ff20872e8da42ed  MartinGM2_043__G2_1.wav
aadf19ca38c73473373a22947f49317dbee0c2b58e040e0115801fe57b76d3e6  MartinGM2_046_Bb2_1.wav
bf57a94a18e63c6c7f90c84c6bf08e998a1ea7e0b8bf1a6a09d31bed77e4a2c7  MartinGM2_049_Db3_1.wav
e2ee2b611260e25bd6dacf00dbe5b3b8c7e5f69cc2cc252461af98025dd284fe  MartinGM2_052__E3_1.wav
694679709136628e7b807a8059a10e4264af8123bf5fc819e9761c4e0e563f29  MartinGM2_055__G3_1.wav
cdacb0918cbe4c2e8da3324f559d94b2be11c3614be6c5da0b7654373e842080  MartinGM2_058_Bb3_1.wav
81c1ea3bdd795d7e91b369f47001757ab3e6f82da0aeb4486da9032451cae958  MartinGM2_061_Db4_1.wav
1ce5ce765892d3f557cd3c47c7fbba9ac866765d10fe342cb03e00b8be0ee412  MartinGM2_064__E4_1.wav
85595b30c7d2fcca30186a3539ca401c4e551e4ca6506c72c26c291a8d333701  MartinGM2_068_Ab4_1.wav
5278514d5a99ba1871c2a5035335219740d759cc6553a06eb54d82742ae052f3  MartinGM2_071__B4_1.wav
272f34deb22bc1ddc17f01c65b34016a6bc519bacf912aa57aecad827ff2f0da  MartinGM2_074__D5_1.wav
981d8956ffde9ec5b045a3ddf563d3802957e6845079540cf4958797060c415a  MartinGM2_077__F5_1.wav
1dfca9cf6adb0f6e4a37ac7ab3818c62c5bab560e6db13373255db7cef23de2e  MartinGM2_080_Ab5_1.wav
```

## Bundled normalized SHA-256

```text
b030bd15d580fcffba42bf3ee43f562b42d70147841d2664d1039ac3d25d44eb  MartinGM2_040__E2_1.wav
77bffdeb23e16db4cca45782c6f1f2d935bd603853cdf2799c3e928ca2f0952a  MartinGM2_043__G2_1.wav
726251cecba465aa674a4ba6cf949f00802e60529530686682fae231a0ffa276  MartinGM2_046_Bb2_1.wav
170c1273cc3f46c9d760e1d3f2b00aea09d267806f02667b07bff3c63d754bc5  MartinGM2_049_Db3_1.wav
6660c434d318ccecf13ee35cbf219885a69c88c6d48a3ea6a06fb5388c7177db  MartinGM2_052__E3_1.wav
507e2896fa0905bc34faa05d8fffef55a9223029a6109fad668ae637946b4ee1  MartinGM2_055__G3_1.wav
cc19a3f52e94fd39877bf5ba1cec7cbc4024ab192895fa75a97da7a87982651c  MartinGM2_058_Bb3_1.wav
234ecfc9583554d2df831f862828ac391c4b9df5f07d03e9e3ac26b09d62942e  MartinGM2_061_Db4_1.wav
b04a034c816d0582846c35acf56afc357c2175228081057ad35b0a83deebcc6e  MartinGM2_064__E4_1.wav
43714fb5f1db686410af4829daa65720a8dd4e0abce72058070e811654f2978e  MartinGM2_068_Ab4_1.wav
72f2a828c6e439506dee15998a260036db5a36b1c2e0ccd99267f7e42fee3b6a  MartinGM2_071__B4_1.wav
199e9f4b1e6001a85e47532b93586bab0dbfe2a6de4fa3787b6de5e869e7e2a9  MartinGM2_074__D5_1.wav
baf5b44cfc463502909591c4263533e2d3bb1d4a71f0148eac6bb147f7bd3304  MartinGM2_077__F5_1.wav
2427016e2a6a074a4f46ccbb2e3c417daa250b8149e3f7212bc73d399caca13b  MartinGM2_080_Ab5_1.wav
```
