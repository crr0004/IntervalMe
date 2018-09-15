package io.github.crr0004.intervalme.interval

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import android.provider.Settings.System.DEFAULT_NOTIFICATION_URI


class IntervalSoundController {
    private val mMediaPlayer: MediaPlayer
    private var mId: Int = -1

    companion object {
        private val mSoundsControllerInstances: HashMap<Int, IntervalSoundController> = HashMap(2)
        fun instanceWith(context: Context, id: Int): IntervalSoundController {
            var instance: IntervalSoundController? = mSoundsControllerInstances[id]
            if(instance == null){
                instance = IntervalSoundController(context, id)
                mSoundsControllerInstances[id] = instance
            }
            return instance
        }

        fun releaseAllInstances(){
            mSoundsControllerInstances.values.forEachIndexed { index, intervalSoundController ->
                intervalSoundController.release()
            }
        }
        fun release(controller: IntervalSoundController){
            mSoundsControllerInstances[controller.mId]?.release()
        }
    }

    constructor(context: Context, id: Int){
        mId = id
        mMediaPlayer = try {
            MediaPlayer.create(context, id)
        }catch(e: Resources.NotFoundException){
            MediaPlayer.create(context, DEFAULT_NOTIFICATION_URI)
        }
    }

    fun playDone() {
        //mMediaPlayer.stop()
        mMediaPlayer.start() // no need to call prepare(); create() does that for you
    }

    fun playLoop(loops: Int){
        //mMediaPlayer.isLooping = true
        var loopCount = 0
        mMediaPlayer.setOnCompletionListener {
            loopCount++
            if(loopCount < loops){
                mMediaPlayer.start()
            }
        }
        mMediaPlayer.start()
    }

    fun release() {
        mMediaPlayer.stop()
        mMediaPlayer.release()
        mSoundsControllerInstances.remove(mId)
    }

}
