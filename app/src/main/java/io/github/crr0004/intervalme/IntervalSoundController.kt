package io.github.crr0004.intervalme

import android.content.Context
import android.media.MediaPlayer



class IntervalSoundController {
    private val mMediaPlayer: MediaPlayer

    constructor(context: Context, id: Int){
        mMediaPlayer = MediaPlayer.create(context, id)
    }

    fun playDone() {
        mMediaPlayer.start() // no need to call prepare(); create() does that for you
    }

    fun release() {
        mMediaPlayer.stop()
        mMediaPlayer.release()
    }

}
