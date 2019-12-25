package me.shetj.media.callback

import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat

interface OnSubscribeCallBack{
    fun onLoadChildren(parentMediaId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>)
}