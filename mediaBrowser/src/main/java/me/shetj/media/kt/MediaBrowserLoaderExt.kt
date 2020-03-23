package me.shetj.media.kt

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.callback.OnMediaStatusChangeListener




fun MediaBrowserLoader.startOrPause(){
    getTransportControls()?.startOrPause()
}


fun MediaBrowserLoader.addMediaStatusListener(
    connectionCallback: ((isSuccess: Boolean) ->Unit )?= null,
    onPlaybackStateChanged:((state: PlaybackStateCompat?) ->Unit )?= null,
    onMetadataChanged:((metadata: MediaMetadataCompat?)->Unit )?= null,
    onQueueChanged:((queue: List<MediaSessionCompat.QueueItem>?)->Unit )?= null,
    onChildrenLoaded:((parentId: String, children: List<MediaBrowserCompat.MediaItem>)->Unit )?= null): OnMediaStatusChangeListener {
    val listener = object : OnMediaStatusChangeListener {
        override fun connectionCallback(isSuccess: Boolean) {
            connectionCallback?.invoke(isSuccess)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            onPlaybackStateChanged?.invoke(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            onMetadataChanged?.invoke(metadata)
        }

        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {
            onQueueChanged?.invoke(queue)
        }

        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaBrowserCompat.MediaItem>
        ) {
            onChildrenLoaded?.invoke(parentId, children)
        }
    }
    addOnMediaStatusListener(listener)
    return listener
}
