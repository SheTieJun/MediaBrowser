package me.shetj.media.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
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
            MediaPlayer(this),
            ContextCompat.getMainExecutor(this),
            object : MediaLibrarySession.MediaLibrarySessionCallback() {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): SessionCommandGroup? {
                    Log.i("MediaBrowser", "onConnect  MusicService")
                    return super.onConnect(session, controller)
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle?
                ): SessionResult {
                    Log.i("MediaBrowser", "onCustomCommand  MusicService")
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
                    Log.i("MediaBrowser", "onCustomCommand  MusicService")
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
                    Log.i("MediaBrowser", "onFastForward  MusicService")
                    return SessionResult.RESULT_SUCCESS
                }

                override fun onRewind(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    Log.i("MediaBrowser", "onRewind  MusicService")
                    return  SessionResult.RESULT_SUCCESS
                }

                override fun onSkipBackward(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): Int {
                    Log.i("MediaBrowser", "onSkipBackward  MusicService")
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
                    Log.i("MediaBrowser", "onSubscribe  $parentId MusicService")
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
                    Log.i("MediaBrowser", "onUnsubscribe  $parentId MusicService")
                    session.player.playlist?.clear()
                    return RESULT_SUCCESS
                }


            }

        )
            .setId("MediaSessionService")
            .setSessionActivity(MediaBrowserHelper.getActivity(this))
            .build()
        Log.i("MediaBrowser", "mMediaSession create  MusicService")
    }




    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        Log.i("MediaBrowser", "onGetSession  MusicService")
        return mMediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.i("MediaBrowser", "onTaskRemoved  MusicService")
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("MediaBrowser", "onStartCommand  MusicService")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUpdateNotification(session: MediaSession): MediaNotification? {
        return super.onUpdateNotification(session)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaSession?.close()
    }

}