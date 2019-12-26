/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.shetj.media.browser


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.service.MusicService
import java.util.*

/**
 * MediaBrowserManager for a MediaBrowser that handles connecting, disconnecting,
 * and basic browsing.
 */
internal class MediaBrowserManager private constructor() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var browserManager :MediaBrowserManager ?=null

        fun getInstance() :MediaBrowserManager {
            if (browserManager == null){
                browserManager = MediaBrowserManager()
            }
            return browserManager!!
        }

    }

    private var mContext:Context ? = null
    private var mMediaBrowserCompat: MediaBrowserCompat? = null
    private var mMediaController: MediaControllerCompat? = null
    private val mMediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
    private val mMediaControllerCallback = MediaControllerCallback()
    private val mMediaBrowserSubscriptionCallback = MediaBrowserSubscriptionCallback()
    /**
     * 音频变化回调 管理列表
     */
    private val mMediaStatusChangeListenerList = ArrayList<OnMediaStatusChangeListener>()

    /**
     * 获取播放控制器 通过该方法控制播放
     *
     * @return
     */
    val transportControls: MediaControllerCompat.TransportControls?
        get() {
            return mMediaController?.transportControls
        }

    fun getMediaController(): MediaControllerCompat ?{
        return mMediaController
    }

    // ########################################音频变化回调 管理列表###################################################


    /**
     * 跟随Activity的生命周期
     */
    fun onStart(mContext: Context) {
        this.mContext = mContext
        if (mMediaBrowserCompat == null) {
            // 创建MediaBrowserCompat
            mMediaBrowserCompat = MediaBrowserCompat(
                mContext,
                // 创建ComponentName 连接 MusicService
                ComponentName(mContext, MusicService::class.java),
                // 创建callback
                mMediaBrowserConnectionCallback, null)//
            // 链接service
            mMediaBrowserCompat!!.connect()
        }
        Log.i("MediaBrowserManager","onStart: Creating MediaBrowser, and connecting")
    }

    /**
     * 跟随Activity的生命周期
     */
    fun onStop() {
        if (mMediaController != null) {
            mMediaController!!.unregisterCallback(mMediaControllerCallback)
            mMediaController = null
        }
        if (mMediaBrowserCompat != null && mMediaBrowserCompat!!.isConnected) {
            mMediaBrowserCompat!!.disconnect()
            mMediaBrowserCompat = null
        }
        mContext = null
        // 数据置空
        Log.i("MediaBrowserManager","onStop: Releasing MediaController, Disconnecting from MediaBrowser")
    }

    fun subscribe(parentId:String){
        mMediaBrowserCompat?.subscribe(parentId,mMediaBrowserSubscriptionCallback)
    }

    // ############################################onConnected CallBack################################################

    /**
     * mediaService的链接回调
     */
    inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        // 连接成功
        // Happens as a result of onStart().
        override fun onConnected() {
            try {
                mMediaController = MediaControllerCompat(
                    mContext,
                    mMediaBrowserCompat!!.sessionToken)
                mMediaController?.registerCallback(mMediaControllerCallback)
                if (mMediaController?.metadata != null) {
                    mMediaControllerCallback.onMetadataChanged(mMediaController?.metadata)
                    mMediaControllerCallback.onPlaybackStateChanged(mMediaController?.playbackState)
                }
            } catch (e: RemoteException) {
                Log.i("MediaBrowserManager",String.format("onConnected: Problem: %s", e.toString()))
                throw RuntimeException(e)
            }
            for (callback in mMediaStatusChangeListenerList) {
                callback.connectionCallback(true)
            }
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            for (callback in mMediaStatusChangeListenerList) {
                callback.connectionCallback(false)
            }
        }

    }

    // ############################################onChildrenLoaded CallBack################################################


    /**
     * 加载新数据后调用
     * Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
     * that is ready for playback.
     */
    inner class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

        /**
         * service 的数据发送到这里
         *
         * @param parentId
         * @param children
         */
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            if (mMediaController == null) {
                return
            }

            //通知界面 parentId 加载成功
            for (callback in mMediaStatusChangeListenerList) {
                callback.onChildrenLoaded(parentId, children)
            }

            // Queue up all media items for this simple sample.
            for (mediaItem in children) {
                mMediaController!!.addQueueItem(mediaItem.description)
            }
            // Call "playFromMedia" so the UI is updated.
            mMediaController!!.transportControls.prepare()
        }
    }


    // ############################################MediaControllerCallback CallBack################################################


    /**
     * service 通过MediaControllerCallback 回调到client
     */
    inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onSessionReady() {
            super.onSessionReady()

        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            for (callback in mMediaStatusChangeListenerList) {
                callback.onMetadataChanged(metadata)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            for (callback in mMediaStatusChangeListenerList) {
                callback.onPlaybackStateChanged(state)
            }
        }

        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {
            super.onQueueChanged(queue)
            for (callback in mMediaStatusChangeListenerList) {
                callback.onQueueChanged(queue)
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)

        }

        override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
            super.onAudioInfoChanged(info)
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
            super.onQueueTitleChanged(title)
        }

        override fun onCaptioningEnabledChanged(enabled: Boolean) {
            super.onCaptioningEnabledChanged(enabled)
        }

        // service被杀死时调用
        override fun onSessionDestroyed() {

        }

    }

    /**
     * 添加音频变化回调
     * @param l
     */
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