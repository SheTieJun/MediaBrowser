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

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import me.shetj.media.callback.PlayInfoCallback
import me.shetj.media.loader.MetadataUtil


/**
 * Exposes the functionality of the [MediaPlayer] and implements the [PlayerAdapter]
 */
internal class MediaPlayerManager (context: Context,
                          private val mPlaybackInfoListener: PlayInfoCallback
)
    : PlayerAdapter(context) {
    // 上下文对象
    private val mContext: Context = context.applicationContext
    // 音频播放器MediaPlayer
    private var mMediaPlayer: MediaPlayer? = null
    // 当前音频id
    private var mFilename: String? = null
    // 当前的播放状态
    @PlaybackStateCompat.State
    private var mState: Int = 0
    // 是否播放完成
    private var mCurrentMediaPlayedToCompletion: Boolean = false

    // Work-around for a MediaPlayer bug related to the behavior of MediaPlayer.seekTo()
    // while not playing.
    private var mSeekWhileNotPlaying = -1


    /**
     * 音频是否在播放
     *
     * @return
     */
    override val isPlaying: Boolean
        get() = mMediaPlayer != null && mMediaPlayer!!.isPlaying

    private val availableActions: Long
        @PlaybackStateCompat.Actions
        get() {
            var actions = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            actions = when (mState) {
                PlaybackStateCompat.STATE_STOPPED -> actions or (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
                PlaybackStateCompat.STATE_PLAYING -> actions or (PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SEEK_TO)
                PlaybackStateCompat.STATE_PAUSED -> actions or (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP)
                else -> actions or (PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE)
            }
            return actions
        }

    // Implements PlaybackControl.
    override fun playFromMedia(metadata: MediaMetadataCompat) {
        // 当前音频信息
        currentMedia = metadata
        // 音频id
        val mediaId = metadata.description.mediaId
        mediaId?.let {
            playFile(MetadataUtil.getMusicFilename(mediaId))
        }
    }


    /**
     * 播放音频
     */
    override fun onPlay() {
        if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onPause() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }


    public override fun onStop() {
        // Regardless of whether or not the MediaPlayer has been created / started, the state must
        // be updated, so that MediaNotificationManager can take down the notification.
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }


    /**
     * seek
     *
     * @param position
     */
    override fun seekTo(position: Long) {
        if (mMediaPlayer != null) {
            // 音频未播放
            if (!mMediaPlayer!!.isPlaying) {
                mSeekWhileNotPlaying = position.toInt()
            }
            // seek to
            mMediaPlayer!!.seekTo(position.toInt())

            // Set the state (to the current state) because the position changed and should
            // be reported to clients.
            setNewState(mState)
        }
    }


    /**
     * 设置音频播放音量
     *
     * @param volume
     */
    override fun setVolume(volume: Float) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.setVolume(volume, volume)
        }
    }

    // ##########################################################################################


    /**
     * Once the [MediaPlayer] is released, it can't be used again, and another one has to be
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     * 初始化mediaPlayer
     */
    private fun initializeMediaPlayer() {
        // 创建MediaPlayer
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            // 音频播放完成的回调
        }
    }

    /**
     * 根据音频id进行音频播放
     *
     * @param filename
     */
    private fun playFile(filename: String?) {
        // 音频是否发生变化
        var mediaChanged = mFilename == null || filename != mFilename
        // 音频是否播放完成
        if (mCurrentMediaPlayedToCompletion) {
            mediaChanged = true
            mCurrentMediaPlayedToCompletion = false
        }
        // 音频未发生变化
        if (!mediaChanged) {
            // 没有播放则播放
            if (!isPlaying) {
                play()
            }
            return
        }
        // 音频已发生变化
        mFilename = filename
        // 创建MediaPlayer
        initializeMediaPlayer()
        // 设置要播放的音频文件
        try {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.setDataSource(mFilename)
        } catch (e: Exception) {
            throw RuntimeException("Failed to open file: " + mFilename!!, e)
        }

        // 准备播放
        try {
            mMediaPlayer!!.prepare()
        } catch (e: Exception) {
            throw RuntimeException("Failed to open file: " + mFilename!!, e)
        }

        // 播放
        play()
    }


    /**
     * 播放状态
     *
     * @param newPlayerState
     */
    // This is the main reducer for the player state machine.
    private fun setNewState(@PlaybackStateCompat.State newPlayerState: Int) {
        // 设置播放状态
        mState = newPlayerState
        /**
         * 状态为STOPPED，则为完成状态
         */
        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true
        }
        val reportPosition: Long
        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying.toLong()
            //
            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                mSeekWhileNotPlaying = -1
            }
        } else {
            reportPosition = (if (mMediaPlayer == null) 0 else mMediaPlayer!!.currentPosition).toLong()
        }
        // 回调播放状态
        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(availableActions)
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime())
        // 播放状态回调
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build())
    }


    /**
     * 释放 MediaPlayer
     */
    private fun release() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun setOnCompletionListener(onCompletionListener: MediaPlayer.OnCompletionListener) {
        if (mMediaPlayer == null){
            initializeMediaPlayer()
        }
        mMediaPlayer!!.setOnCompletionListener{
            onCompletionListener.onCompletion(it)
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

}
