package com.vs.schoolmessenger.School.LSRW.Adapter

data class AudioFile(
    val filePath: String,
    var isPlaying: Boolean = false,
    var duration: Int = 0
)
