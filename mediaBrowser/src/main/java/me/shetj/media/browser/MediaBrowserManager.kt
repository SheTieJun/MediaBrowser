package me.shetj.media.browser

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.common.SessionPlayer.PLAYER_STATE_PLAYING
import androidx.media2.common.SubtitleData
import androidx.media2.session.*
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.service.MusicService
import java.util.*

internal class MediaBrowserManager private constructor() {

    companion object {
        @Volatile
        private var browserManager: MediaBrowserManager? = null

        fun getInstance() =
            browserManager ?: synchronized(this) {
                MediaBrowserManager().also { browserManager = it }
            }

    }

    private var mContext: Context? = null
    private var mMediaBrowser: MediaBrowser? = null
    private val mMediaStatusChangeListenerList = ArrayList<OnMediaStatusChangeListener>()


    /**
     * 必须start
     */
    fun getMediaBrowser(): MediaBrowser {
        return mMediaBrowser!!
    }

    // ########################################音频变化回调 管理列表###################################################


    /**
     * 跟随Activity的生命周期
     */
    fun onStart(mContext: Context) {
        this.mContext = mContext
        if (mMediaBrowser == null) {
            // 创建MediaBrowserCompat
            mMediaBrowser = MediaBrowser.Builder(mContext)
                .setSessionToken(
                    SessionToken(
                        mContext,
                        ComponentName(mContext, MusicService::class.java)
                    )
                )
                .setControllerCallback(ContextCompat.getMainExecutor(mContext),
                    object : MediaBrowser.BrowserCallback() {
                        override fun onConnected(
                            controller: MediaController,
                            allowedCommands: SessionCommandGroup
                        ) {
                            super.onConnected(controller, allowedCommands)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.connectionCallback(true)
                            }
                        }

                        override fun onDisconnected(controller: MediaController) {
                            super.onDisconnected(controller)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.connectionCallback(false)
                            }
                        }

                        override fun onChildrenChanged(
                            browser: MediaBrowser,
                            parentId: String,
                            itemCount: Int,
                            params: MediaLibraryService.LibraryParams?
                        ) {
                            super.onChildrenChanged(browser, parentId, itemCount, params)
                        }

                        override fun onSearchResultChanged(
                            browser: MediaBrowser,
                            query: String,
                            itemCount: Int,
                            params: MediaLibraryService.LibraryParams?
                        ) {
                            super.onSearchResultChanged(browser, query, itemCount, params)
                        }

                        override fun onPlaybackSpeedChanged(
                            controller: MediaController,
                            speed: Float
                        ) {
                            super.onPlaybackSpeedChanged(controller, speed)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaybackSpeedChanged(controller, speed)
                            }
                        }

                        override fun onPlaylistChanged(
                            controller: MediaController,
                            list: MutableList<MediaItem>?,
                            metadata: MediaMetadata?
                        ) {
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaylistChanged(list, metadata)
                            }
                            super.onPlaylistChanged(controller, list, metadata)
                        }

                        override fun onPlayerStateChanged(controller: MediaController, state: Int) {
                            super.onPlayerStateChanged(controller, state)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlayerStateChanged(state)
                            }
                        }

                        override fun onPlaybackCompleted(controller: MediaController) {
                            super.onPlaybackCompleted(controller)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaybackCompleted()
                            }
                        }

                        override fun onPlaylistMetadataChanged(
                            controller: MediaController,
                            metadata: MediaMetadata?
                        ) {
                            super.onPlaylistMetadataChanged(controller, metadata)
                        }

                        override fun onCurrentMediaItemChanged(
                            controller: MediaController,
                            item: MediaItem?
                        ) {
                            super.onCurrentMediaItemChanged(controller, item)
                            item?.let {
                                for (callback in mMediaStatusChangeListenerList) {
                                    callback.onPlaylistMetadataChanged(item)
                                }
                            }
                        }

                        override fun onPlaybackInfoChanged(
                            controller: MediaController,
                            info: MediaController.PlaybackInfo
                        ) {
                            super.onPlaybackInfoChanged(controller, info)
                        }

                        override fun onSeekCompleted(controller: MediaController, position: Long) {
                            super.onSeekCompleted(controller, position)
                        }

                        override fun onSetCustomLayout(
                            controller: MediaController,
                            layout: MutableList<MediaSession.CommandButton>
                        ): Int {
                            return super.onSetCustomLayout(controller, layout)
                        }

                        override fun onSubtitleData(
                            controller: MediaController,
                            item: MediaItem,
                            track: SessionPlayer.TrackInfo,
                            data: SubtitleData
                        ) {
                            Log.i("MediaBrowser", "onSubtitleData  MediaBrowserManager")
                            super.onSubtitleData(controller, item, track, data)
                        }
                    })
                .build()
        }
    }

    fun subscribe(parentId: String) {
        mMediaBrowser?.subscribe(parentId, null)
    }


    fun unSubscribe(parentId: String) {
        mMediaBrowser?.unsubscribe(parentId)
    }

    fun setSpeed(@FloatRange(from = 0.01)speed:Float){
        mMediaBrowser?.playbackSpeed = speed
    }

    fun addOnMediaStatusListener(l: OnMediaStatusChangeListener) {
        if (!mMediaStatusChangeListenerList.contains(l)) {
            mMediaStatusChangeListenerList.add(l)
        }
    }

    /**
     * 移除音频变化回调
     *
     * @param l
     */
    fun removeOnMediaStatusListener(l: OnMediaStatusChangeListener) {
        mMediaStatusChangeListenerList.remove(l)
    }

}