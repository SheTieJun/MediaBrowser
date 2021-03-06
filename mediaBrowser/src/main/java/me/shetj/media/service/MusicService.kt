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

package me.shetj.media.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import me.shetj.media.callback.MediaSessionCallback
import me.shetj.media.callback.PlayInfoCallback
import me.shetj.media.loader.MediaBrowserHelper
import me.shetj.media.loader.MetadataUtil
import me.shetj.media.notifications.MediaNotificationManager
import me.shetj.media.player.MediaPlayerManager


/**
 * MediaBrowserServiceCompat
 */
class MusicService : MediaBrowserServiceCompat() {
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private var mMediaPlayerManager: MediaPlayerManager? = null
    private var mMediaNotificationManager: MediaNotificationManager? = null
    private var mServiceInStartedState: Boolean = false
    private var mMediaSessionCompat: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        // 创建MediaSessionCompat
        mMediaSessionCompat = MediaSessionCompat(this, "MusicService")
        mMediaPlayerManager = MediaPlayerManager(this, MediaPlayerListener())
        // setCallBack
        mMediaSessionCompat!!.setCallback(MediaSessionCallback(this, mMediaSessionCompat!!, mMediaPlayerManager!!, 0))
        mMediaSessionCompat!!.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
        sessionToken = mMediaSessionCompat!!.sessionToken
        mMediaNotificationManager = MediaNotificationManager(this)
        becomingNoisyReceiver =
            BecomingNoisyReceiver(context = this, sessionToken = mMediaSessionCompat!!.sessionToken)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mMediaNotificationManager?.onDestroy()
        mMediaPlayerManager?.stop()
        mMediaSessionCompat?.release()
    }

    override fun onGetRoot(clientPackageName: String,
                           clientUid: Int,
                           rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MetadataUtil.root, null)
    }

    //通过parentMediaId 加载不同的数据列表
    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>) {
        //通过不同的parentId,获取不同的列表
        MediaBrowserHelper.onLoadChildren(parentMediaId,result)
    }


    /**
     * Returns a list of [MediaItem]s that match the given search query
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {

    }
    private class BecomingNoisyReceiver(
        private val context: Context,
        sessionToken: MediaSessionCompat.Token
    ) : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val controller = MediaControllerCompat(context, sessionToken)

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }



    /**
     * MediaPlayer 播放状态回调
     */
    internal inner class MediaPlayerListener : PlayInfoCallback() {

        override fun onPlaybackStateChange(state: PlaybackStateCompat?) {
            state?.let {
                mMediaSessionCompat!!.setPlaybackState(state)
                when (state.state) {
                    PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_PLAYING -> moveServiceToStartedState(state)
                    PlaybackStateCompat.STATE_PAUSED -> updateNotificationForPause(state)
                    PlaybackStateCompat.STATE_STOPPED -> moveServiceOutOfStartedState(state)
                }
            }
        }


        override fun onPlaybackCompleted() {

        }


        private fun moveServiceToStartedState(state: PlaybackStateCompat) {
            becomingNoisyReceiver.register()
            val notification = mMediaNotificationManager!!.getNotification(
                mMediaPlayerManager!!.currentMedia!!, state, sessionToken!!
            )
            if (!mServiceInStartedState) {
                ContextCompat.startForegroundService(
                    this@MusicService,
                    Intent(this@MusicService, MusicService::class.java)
                )
                mServiceInStartedState = true
            }
            startForeground(MediaBrowserHelper.getNotificationID(), notification)
        }

        /**
         * @param state
         */
        private fun updateNotificationForPause(state: PlaybackStateCompat) {
            becomingNoisyReceiver.unregister()
            NotificationManagerCompat.from(applicationContext).notify(
                MediaBrowserHelper.getNotificationID(), mMediaNotificationManager!!.getNotification(
                    mMediaPlayerManager!!.currentMedia!!, state, sessionToken!!
                )
            )
            stopForeground(false)
        }

        /**
         * @param state
         */
        private fun moveServiceOutOfStartedState(state: PlaybackStateCompat) {
            stopForeground(true)
            stopSelf()
            mServiceInStartedState = false
        }


    }

}