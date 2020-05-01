package me.shetj.media.browser

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer.PLAYER_STATE_PLAYING
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


    fun getMediaBrowser(): MediaBrowser? {
        return mMediaBrowser
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
                            Log.i("MediaBrowser", "onConnected   MediaBrowserManager")
                        }


                        override fun onDisconnected(controller: MediaController) {
                            super.onDisconnected(controller)
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.connectionCallback(false)
                            }
                            Log.i("MediaBrowser", "onDisconnected   MediaBrowserManager")
                        }

                        override fun onChildrenChanged(
                            browser: MediaBrowser,
                            parentId: String,
                            itemCount: Int,
                            params: MediaLibraryService.LibraryParams?
                        ) {
                            super.onChildrenChanged(browser, parentId, itemCount, params)
                            Log.i("MediaBrowser", "onChildrenChanged  MediaBrowserManager")
                        }

                        override fun onSearchResultChanged(
                            browser: MediaBrowser,
                            query: String,
                            itemCount: Int,
                            params: MediaLibraryService.LibraryParams?
                        ) {
                            super.onSearchResultChanged(browser, query, itemCount, params)
                            Log.i("MediaBrowser", "onSearchResultChanged  MediaBrowserManager")
                        }

                        override fun onPlaybackSpeedChanged(
                            controller: MediaController,
                            speed: Float
                        ) {
                            super.onPlaybackSpeedChanged(controller, speed)
                            Log.i("MediaBrowser", "onPlaybackSpeedChanged  MediaBrowserManager")
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaybackSpeedChanged(controller, speed)
                            }
                        }

                        override fun onPlaylistChanged(
                            controller: MediaController,
                            list: MutableList<MediaItem>?,
                            metadata: MediaMetadata?
                        ) {
                            Log.i("MediaBrowser", "onPlaylistChanged  MediaBrowserManager")
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaylistChanged(list, metadata)
                            }
                            super.onPlaylistChanged(controller, list, metadata)
                        }

                        override fun onPlayerStateChanged(controller: MediaController, state: Int) {
                            Log.i("MediaBrowser", "onPlayerStateChanged  MediaBrowserManager")
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlayerStateChanged(state)
                            }
                            super.onPlayerStateChanged(controller, state)
                        }

                        override fun onPlaybackCompleted(controller: MediaController) {
                            Log.i("MediaBrowser", "onPlaybackCompleted  MediaBrowserManager")
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaybackCompleted()
                            }
                            super.onPlaybackCompleted(controller)
                        }

                        override fun onPlaylistMetadataChanged(
                            controller: MediaController,
                            metadata: MediaMetadata?
                        ) {
                            for (callback in mMediaStatusChangeListenerList) {
                                callback.onPlaylistMetadataChanged(metadata)
                            }
                            Log.i("MediaBrowser", "onPlaylistMetadataChanged  MediaBrowserManager")
                            super.onPlaylistMetadataChanged(controller, metadata)
                        }

                        override fun onPlaybackInfoChanged(
                            controller: MediaController,
                            info: MediaController.PlaybackInfo
                        ) {
                            Log.i("MediaBrowser", "onPlaybackInfoChanged  MediaBrowserManager")
                            super.onPlaybackInfoChanged(controller, info)
                        }

                        override fun onSeekCompleted(controller: MediaController, position: Long) {
                            super.onSeekCompleted(controller, position)
                        }



                    })
                .build()
        }
    }

    fun subscribe(parentId: String) {
        pause()
        mMediaBrowser?.playlist?.clear()
        mMediaBrowser?.subscribe(parentId, null)
    }


    fun unSubscribe(parentId: String) {
        pause()
        mMediaBrowser?.unsubscribe(parentId)
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

    private fun pause() {
        if (mMediaBrowser?.playerState == PLAYER_STATE_PLAYING) {
            mMediaBrowser?.pause()
        }
    }

}