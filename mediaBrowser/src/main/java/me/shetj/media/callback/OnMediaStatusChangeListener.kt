package me.shetj.media.callback

import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaController

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
    fun onPlayerStateChanged(@SessionPlayer.PlayerState  state:Int)

    /**
     * 当前播放歌曲信息修改
     */
    fun onPlaylistMetadataChanged(metadata: MediaMetadata?)

    /**
     * 播放队列修改
     */
    fun onPlaylistChanged(list: MutableList<MediaItem>?, metadata: MediaMetadata?)

    /**
     * 播放完成
     */
    fun onPlaybackCompleted()


    /**
     * 倍数修改
     */
    fun onPlaybackSpeedChanged(controller: MediaController, speed: Float)
}
