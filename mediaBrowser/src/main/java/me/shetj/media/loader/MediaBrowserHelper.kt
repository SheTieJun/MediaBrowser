package me.shetj.media.loader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.R
import me.shetj.media.callback.NotificationHelper
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.notifications.MediaNotificationManager
import me.shetj.media.notifications.MediaNotificationManager.Companion.NOTIFICATION_ID

/**
 * 帮助类，用设置自定义和默认情况
 */
internal object MediaBrowserHelper{
    /**
     * 通过不同的 parentMediaId ,使用不同的加载方式
     */
    private val mediaLoadDataCallBack = HashMap<String, OnSubscribeCallBack>()
    private var notificationHelper: NotificationHelper?=null

    fun setNotificationHelper(notificationHelper: NotificationHelper){
        this.notificationHelper = notificationHelper
    }

    fun addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack) {
        mediaLoadDataCallBack[parentMediaId] = callBack
    }

    fun removeMediaLoadDataCallBack(parentMediaId:String) {
        mediaLoadDataCallBack.remove(parentMediaId)
    }

    /*********************************************** model 内使用的方法 ****************************************************************/

    internal fun getNotificationID(): Int {
        return  if (notificationHelper != null){
            notificationHelper!!.getNotificationID()
        }else{
            NOTIFICATION_ID
        }
    }

    internal fun onLoadChildren(parentMediaId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        val callBack = mediaLoadDataCallBack[parentMediaId]
        callBack?.let {
            callBack.onLoadChildren(parentMediaId, result)
        }
    }

    internal fun checkParentId(parentId: String): Boolean {
       return  null != mediaLoadDataCallBack[parentId]
    }

    internal fun getAlbumBitmap(mContext: Context, mediaId: String): Bitmap? {
        if (notificationHelper != null){
            return  notificationHelper?.getAlbumBitmap(mContext, mediaId)
        }
        return  BitmapFactory.decodeResource(mContext.resources,
            if (MediaBrowserLoader.getMediaController()?.playbackState?.state
                == PlaybackStateCompat.STATE_PLAYING)
                R.drawable.ic_media_with_pause else R.drawable.ic_media_with_play)
    }
    internal fun createContentIntent(
        mContext: Context,
        token: MediaSessionCompat.Token
    ): PendingIntent {
        return notificationHelper?.createContentIntent() ?: defCreateContentIntent(mContext,token)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun createChannel(mContext: Context) {
        if (notificationHelper != null){
            notificationHelper!!.createChannel(mContext)
        }else{
            defCreateChannel(mContext)
        }
    }

    /*********************************************** 私有方法 ****************************************************************/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defCreateChannel(mContext: Context){
        if (NotificationManagerCompat.from(mContext).getNotificationChannel(
                getChannelID()
            ) == null) {
            val name = "MediaSession"
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(getChannelID(), name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            NotificationManagerCompat.from(mContext).createNotificationChannel(mChannel)
        }
    }

    private fun getChannelID(): String {
        return  if (notificationHelper != null){
            notificationHelper!!.getChannelID()?: MediaNotificationManager.CHANNEL_ID
        }else{
            MediaNotificationManager.CHANNEL_ID
        }
    }

    private fun defCreateContentIntent(
        mContext: Context,
        token: MediaSessionCompat.Token
    ): PendingIntent {
        val controller = MediaControllerCompat(mContext, token)
        if (controller.sessionActivity == null){
            val openUI =
                mContext.packageManager.getLaunchIntentForPackage(mContext.packageName)
            openUI?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            return PendingIntent.getActivity(
                mContext, MediaNotificationManager.REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return controller.sessionActivity
    }

}