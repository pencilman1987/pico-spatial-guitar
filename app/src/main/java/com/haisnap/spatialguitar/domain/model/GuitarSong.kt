package com.haisnap.spatialguitar.domain.model

data class GuitarSongStep(
    val chord: GuitarChord,
    val lyric: String,
    val strums: Int = 4,
) {
    init {
        require(lyric.isNotBlank())
        require(strums > 0)
    }
}

/**
 * Short original practice songs shipped with the app. Keeping these lyrics
 * original makes the offline demo redistributable while the song-sheet engine
 * remains ready for licensed/user-authored catalogs later.
 */
enum class GuitarSong(
    val title: String,
    private val baseKeyPitchClass: Int,
    private val isMinorKey: Boolean,
    val steps: List<GuitarSongStep>,
) {
    SING_TOGETHER(
        title = "一起唱",
        baseKeyPitchClass = 0,
        isMinorKey = false,
        steps =
            listOf(
                GuitarSongStep(GuitarChord.C_MAJOR, "窗边的风轻轻吹进来"),
                GuitarSongStep(GuitarChord.G_MAJOR, "把今天的心情唱出来"),
                GuitarSongStep(GuitarChord.A_MINOR, "不用害怕脚步有点慢"),
                GuitarSongStep(GuitarChord.F_MAJOR, "一下一下就是我们的节拍"),
                GuitarSongStep(GuitarChord.C_MAJOR, "你唱一句我来轻轻伴"),
                GuitarSongStep(GuitarChord.G_MAJOR, "平凡的夜也会有星光"),
                GuitarSongStep(GuitarChord.F_MAJOR, "当最后一个和弦落下来"),
                GuitarSongStep(GuitarChord.C_MAJOR, "我们笑着再唱一遍"),
            ),
    ),
    EVENING_BREEZE(
        title = "晚风",
        baseKeyPitchClass = 9,
        isMinorKey = true,
        steps =
            listOf(
                GuitarSongStep(GuitarChord.A_MINOR, "晚风从小路那头走来"),
                GuitarSongStep(GuitarChord.F_MAJOR, "路灯把影子慢慢拉开"),
                GuitarSongStep(GuitarChord.C_MAJOR, "今天的故事还没讲完"),
                GuitarSongStep(GuitarChord.G_MAJOR, "就用一首歌把它留下来"),
                GuitarSongStep(GuitarChord.A_MINOR, "不必每个音都唱得明白"),
                GuitarSongStep(GuitarChord.F_MAJOR, "跟着心跳就不会迷路"),
                GuitarSongStep(GuitarChord.G_MAJOR, "当你和我在同一个节拍"),
                GuitarSongStep(GuitarChord.C_MAJOR, "这一刻就值得记住"),
            ),
    ),
    ;

    init {
        require(steps.isNotEmpty())
    }

    fun keyNameAt(transposeSemitones: Int): String =
        pitchName(baseKeyPitchClass + transposeSemitones) + if (isMinorKey) "m" else ""
}
