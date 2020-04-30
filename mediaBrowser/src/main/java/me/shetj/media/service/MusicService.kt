package me.shetj.media.service

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media2.player.MediaPlayer
import androidx.media2.session.MediaLibraryService
import androidx.media2.session.MediaSession
import me.shetj.media.loader.MediaBrowserHelper


/**
 * MediaBrowserServiceCompat
 */
class MusicService : MediaLibraryService() {
    private var mMediaSession: MediaLibrarySession? = null
    private val mMediaPlayer: MediaPlayer by lazy {
        MediaPlayer(this)
    }
    override fun onCreate() {
        super.onCreate()
        // 创建MediaSessionCompat
        mMediaSession = MediaLibrarySession.Builder(
            this,
             mMediaPlayer,
            ContextCompat.getMainExecutor(this),
            MediaLibrarySession.MediaLibrarySessionCallback()
        )
            .setId("MediaSessionService")
            .setSessionActivity(MediaBrowserHelper.getActivity(this))
            .build()
    }

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

    override fun onDestroy() {
        super.onDestroy()
    }


}