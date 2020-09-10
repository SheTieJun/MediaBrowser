package me.shetj.media.service

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.AudioAttributesCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media2.common.BaseResult.RESULT_SUCCESS
import androidx.media2.player.MediaPlayer
import androidx.media2.session.*
import me.shetj.media.loader.MediaBrowserHelper


/**
 * MediaBrowserServiceCompat
 */
class MusicService : MediaLibraryService() {
    private var mMediaSession: MediaLibrarySession? = null
    override fun onCreate() {
        super.onCreate()
        // 创建MediaSessionCompat
        mMediaSession = MediaLibrarySession.Builder(
            this,
            MediaPlayer(this).apply {
                //必须设置 ，否则无法进行播放
                setAudioAttributes(AudioAttributesCompat.Builder()
                    .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build())
            },
            ContextCompat.getMainExecutor(this),
            object : MediaLibrarySession.MediaLibrarySessionCallback() {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): SessionCommandGroup? {
                    return super.onConnect(session, controller)
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle?
                ): SessionResult {
                    return super.onCustomCommand(session, controller, customCommand, args)
                }


                override fun onGetChildren(
                    session: MediaLibrarySession,
                    controller: MediaSession.ControllerInfo,
                    parentId: String,
                    page: Int,
                    pageSize: Int,
                    params: LibraryParams?
                ): LibraryResult {
                    return super.onGetChildren(
                        session,
                        controller,
                        parentId,
                        page,
                        pageSize,
                        params
                    )
                }


                override fun onFastForward(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    return SessionResult.RESULT_SUCCESS
                }

                override fun onRewind(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    return  SessionResult.RESULT_SUCCESS
                }

                override fun onSkipBackward(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    return SessionResult.RESULT_SUCCESS
                }

                override fun onSkipForward(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    return SessionResult.RESULT_SUCCESS
                }

                override fun onSubscribe(
                    session: MediaLibrarySession,
                    controller: MediaSession.ControllerInfo,
                    parentId: String,
                    params: LibraryParams?
                ): Int {
                    return MediaBrowserHelper.onSubscribe(
                        session.player,
                        controller,
                        parentId,
                        params
                    )
                }


                override fun onUnsubscribe(
                    session: MediaLibrarySession,
                    controller: MediaSession.ControllerInfo,
                    parentId: String
                ): Int {
                    session.player.playlist?.clear()
                    return RESULT_SUCCESS
                }
            }

        )
            .setId("MediaSessionService")
            .setSessionActivity(MediaBrowserHelper.getActivity(this))
            .build()
    }


    private var mMediaSessionCompat :MediaSessionCompat ? = null


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mMediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUpdateNotification(session: MediaSession): MediaNotification? {
        //更新通知栏
        return super.onUpdateNotification(session)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaSession?.close()
    }

}