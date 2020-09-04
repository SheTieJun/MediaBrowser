package me.shetj.media.kt

import android.support.v4.media.session.MediaControllerCompat
import androidx.media2.common.SessionPlayer.PLAYER_STATE_PAUSED
import androidx.media2.common.SessionPlayer.PLAYER_STATE_PLAYING
import me.shetj.media.MediaBrowserLoader

fun startOrPause(){
    when(MediaBrowserLoader.getMediaController().playerState){
        PLAYER_STATE_PAUSED ->{
            MediaBrowserLoader.getMediaController().play()
        }
        PLAYER_STATE_PLAYING ->{
            MediaBrowserLoader.getMediaController().pause()
        }
        else ->{
            MediaBrowserLoader.getMediaController().play()
        }
    }
}

fun MediaControllerCompat.state(): Int? {
   return  playbackState?.state
}


