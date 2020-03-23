package me.shetj.media.kt

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import me.shetj.media.MediaBrowserLoader


fun MediaControllerCompat.TransportControls.startOrPause(){
    when(MediaBrowserLoader.getMediaController()?.state()){
        PlaybackStateCompat.STATE_PLAYING ->{
            pause()
        }
        PlaybackStateCompat.STATE_PAUSED ->{
            play()
        }
        else ->{
            play()
        }
    }
}

fun MediaControllerCompat.state(): Int? {
   return  playbackState?.state
}


