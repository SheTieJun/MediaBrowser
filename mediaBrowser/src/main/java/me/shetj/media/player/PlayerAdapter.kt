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

package me.shetj.media.player

import android.content.*
import android.media.AudioManager
import android.support.v4.media.MediaMetadataCompat

/**
 * Abstract player implementation that handles playing music with proper handling of headphones
 * and audio focus.
 */
abstract class PlayerAdapter(context: Context) {
    // 播放的上下文对象
    private val mContext: Context = context.applicationContext
    // 获取AudioManager
    private val mAudioManager: AudioManager
    // OnAudioFocusChangeListener
    private val mAudioFocusHelper: AudioFocusHelper

    var currentMedia: MediaMetadataCompat ? =null

    abstract val isPlaying: Boolean

    // ##########################################获取焦点帮助类###############################################


    // 是否失去焦点时，停止了音频播放
    private var mPlayingOnAudioFocusLoss = false


    // ##########################################耳机状态变化的广播接收者###############################################


    /**
     * 耳机插拔等状态变化的监听
     */
    private var mAudioNoisyReceiverRegistered = false
    // receiver
    private val mAudioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 耳机插拔变化等的监听
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // 正在播放，则停止播放
                if (isPlaying) {
                    pause()
                }
            }
        }
    }


    init {
        // 上下文对象
        // 获取AudioManager
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // OnAudioFocusChangeListener
        mAudioFocusHelper = AudioFocusHelper()
    }

    abstract fun playFromMedia(metadata: MediaMetadataCompat)


    /**
     * 播放音频
     */
    fun play() {
        // 是否获取到焦点的判断
        if (mAudioFocusHelper.requestAudioFocus()) {
            // 注册插拔耳机广播
            registerAudioNoisyReceiver()
            // 播放
            onPlay()
        }
    }

    /**
     * Called when media is ready to be played and indicates the app has audio focus.
     */
    protected abstract fun onPlay()


    /**
     * 停止播放
     */
    fun pause() {
        // 丢弃音频焦点
        if (!mPlayingOnAudioFocusLoss) {
            mAudioFocusHelper.abandonAudioFocus()
        }
        // 取消广播注册
        unregisterAudioNoisyReceiver()
        // 暂停播放
        onPause()
    }

    /**
     * Called when media must be paused.
     */
    protected abstract fun onPause()


    /**
     * 停止播放
     */
    fun stop() {
        // 放弃焦点
        mAudioFocusHelper.abandonAudioFocus()
        // 取消耳机插拔广播注册
        unregisterAudioNoisyReceiver()
        // 结束播放
        onStop()
    }

    /**
     * Called when the media must be stopped. The player should clean up resources at this
     * point.
     */
    protected abstract fun onStop()

    /**
     * seek to
     *
     * @param position
     */
    abstract fun seekTo(position: Long)

    /**
     * 设置音频播放音量
     *
     * @param volume
     */
    abstract fun setVolume(volume: Float)


    /**
     * Helper class for managing audio focus related tasks.
     *
     *
     * 音频焦点
     */
    private inner class AudioFocusHelper : AudioManager.OnAudioFocusChangeListener {

        /**
         * 请求音频焦点
         *
         * @return
         */
        fun requestAudioFocus(): Boolean {
            // 请求音频焦点  并判断音频焦点的获取情况
            val result = mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        /**
         * 放弃音频焦点
         */
        fun abandonAudioFocus() {
            mAudioManager.abandonAudioFocus(this)
        }

        /**
         * 音频焦点变化回调
         *
         * @param focusChange
         */
        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                // 获取到音频焦点
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // 没有播放&&焦点失去时停止过播放 则播放
                    if (mPlayingOnAudioFocusLoss && !isPlaying) {
                        play()
                    } else if (isPlaying) {
                        setVolume(MEDIA_VOLUME_DEFAULT)
                    }// 正在播放
                    mPlayingOnAudioFocusLoss = false
                }
                // 播放中失去焦点，可降低播放质量，来维持播放
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> setVolume(MEDIA_VOLUME_DUCK)
                // 失去焦点
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                    // 失去焦点，暂停播放
                    if (isPlaying) {
                        mPlayingOnAudioFocusLoss = true
                        pause()
                    }
                // 失去焦点
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // 停止播放
                    mAudioManager.abandonAudioFocus(this)
                    mPlayingOnAudioFocusLoss = false
                    stop()
                }
            }
        }
    }


    /**
     * 注册Receiver
     */
    private fun registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER)
            mAudioNoisyReceiverRegistered = true
        }
    }

    /**
     * 取消Receiver注册
     */
    private fun unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver)
            mAudioNoisyReceiverRegistered = false
        }
    }

    companion object {
        // 默认的音量 0~1之间
        private val MEDIA_VOLUME_DEFAULT = 1.0f
        // 失去焦点时，降低音量后的音量
        private val MEDIA_VOLUME_DUCK = 0.2f
        // filter
        private val AUDIO_NOISY_INTENT_FILTER = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }
}
