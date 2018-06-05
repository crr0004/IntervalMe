package io.github.crr0004.intervalme

import android.content.Context
import android.media.MediaPlayer



class IntervalSoundController {
    private val mMediaPlayer: MediaPlayer
    private var mId: Int = -1

    companion object {
        private val mSoundsControllerInstances: HashMap<Int, IntervalSoundController> = HashMap(2)
        fun instanceWith(context: Context, id: Int): IntervalSoundController{
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
        mMediaPlayer = MediaPlayer.create(context, id)
    }

    fun playDone() {
        //mMediaPlayer.stop()
        mMediaPlayer.start() // no need to call prepare(); create() does that for you
    }

    fun release() {
        mMediaPlayer.stop()
        mMediaPlayer.release()
        IntervalSoundController.mSoundsControllerInstances.remove(mId)
    }

}
