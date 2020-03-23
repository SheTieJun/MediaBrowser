package me.shetj.media.callback

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

/**
 * 音频变化回调
 */
interface OnMediaStatusChangeListener {

    /**
     * 连接服务成功
     */
    fun connectionCallback(isSuccess: Boolean)
    /**
     * 播放状态修改
     */
    fun onPlaybackStateChanged(state: PlaybackStateCompat?)

    /**
     * 当前播放歌曲信息修改
     */
    fun onMetadataChanged(metadata: MediaMetadataCompat?)

    /**
     * 播放队列修改
     */
    fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?)

    /**
     * 获取数据成功回调
     */
    fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>)
}
