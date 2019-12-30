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

package me.shetj.media.notifications


import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import me.shetj.media.R
import me.shetj.media.kt.isPlaying
import me.shetj.media.kt.isSkipToNextEnabled
import me.shetj.media.kt.isSkipToPreviousEnabled
import me.shetj.media.loader.MediaBrowserHelper

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
internal class MediaNotificationManager(private val mContext: Context) {


    private val mPlayAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_play_arrow_white_24dp,
            mContext.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, PlaybackStateCompat.ACTION_PLAY))

    private val mPauseAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_pause_white_24dp,
            mContext.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    mContext,
                    PlaybackStateCompat.ACTION_PAUSE))

    private val mNextAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_skip_next_white_24dp,
            mContext.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    mContext,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT))

    private val mPrevAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_skip_previous_white_24dp,
            mContext.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    mContext,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, PlaybackStateCompat.ACTION_STOP)

    fun onDestroy() {
        NotificationManagerCompat.from(mContext).cancel(NOTIFICATION_ID)
    }

    fun getNotification(metadata: MediaMetadataCompat,
                        state: PlaybackStateCompat,
                        token: MediaSessionCompat.Token): Notification {
        val isPlaying = state.isPlaying
        val description = metadata.description
        val builder = buildNotification(state, token, isPlaying, description)
        return builder.build()
    }

    private fun buildNotification(state: PlaybackStateCompat,
                                  token: MediaSessionCompat.Token,
                                  isPlaying: Boolean,
                                  description: MediaDescriptionCompat): NotificationCompat.Builder {
        // Create the (mandatory) notification channel when running on Android Oreo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
        var position = 0
        if (state.isSkipToPreviousEnabled) {
            builder.addAction(mPrevAction)
            ++position
        }

        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)

        if (state.isSkipToNextEnabled) {
            builder.addAction(mNextAction)
        }

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
            .setShowActionsInCompactView(position)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mContext,
                                PlaybackStateCompat.ACTION_STOP))
        builder.setStyle(mediaStyle)
            .setColor(ContextCompat.getColor(mContext, R.color.notification_bg))
                .setSmallIcon(R.drawable.ic_stat_image_audiotrack)
                .setContentIntent(createContentIntent(token))
                .setDeleteIntent(stopPendingIntent)
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setLargeIcon(MediaBrowserHelper.getAlbumBitmap(mContext, description.mediaId!!))
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mContext, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        MediaBrowserHelper.createChannel(mContext)
    }

    private fun createContentIntent(
        token: MediaSessionCompat.Token
    ): PendingIntent {
        return MediaBrowserHelper.createContentIntent(this.mContext,token)
    }

    companion object {
        const val NOTIFICATION_ID = 412
        internal const val CHANNEL_ID = "me.shetj.media"
        internal const val REQUEST_CODE = 501
    }

}