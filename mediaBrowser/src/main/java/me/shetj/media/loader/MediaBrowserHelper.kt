package me.shetj.media.loader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import com.shetj.diyalbume.playVideo.media.notifications.MediaNotificationManager

object MediaBrowserHelper{


    fun init(context: Context?) {
        requireNotNull(context) { "the provided context must not be null!" }
    }

    /**
     * 通过不同的 parentMediaId ,使用不同的加载方式
     */
    private val mediaLoadDataCallBack = HashMap<String, OnSubscribeCallBack>()
    private var onCreateChannel: OnCreateChannel?=null

    fun setOnCreateChannel(onCreateChannel: OnCreateChannel){
        MediaBrowserHelper.onCreateChannel = onCreateChannel
    }


    fun onLoadChildren(parentMediaId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        val callBack = mediaLoadDataCallBack[parentMediaId]
        callBack?.let {
            callBack.onLoadChildren(parentMediaId, result)
        }
    }

    fun addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack) {
        mediaLoadDataCallBack[parentMediaId] = callBack
    }

    fun removeMediaLoadDataCallBack(parentMediaId:String) {
        mediaLoadDataCallBack.remove(parentMediaId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(mContext: Context) {
        if (onCreateChannel != null){
            onCreateChannel!!.createChannel(mContext)
        }else{
            defCreateChannel(mContext)
        }
    }

    /*********************************************** 私有方法 ****************************************************************/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defCreateChannel(mContext: Context){
        if (NotificationManagerCompat.from(mContext).getNotificationChannel(
                MediaNotificationManager.CHANNEL_ID
            ) == null) {
            val name = "MediaSession"
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(MediaNotificationManager.CHANNEL_ID, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            NotificationManagerCompat.from(mContext).createNotificationChannel(mChannel)
        }
    }

    /*********************************************** 接口部分 ****************************************************************/

    interface OnSubscribeCallBack{
       fun onLoadChildren(parentMediaId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>)
    }

    interface OnCreateChannel{
        fun createChannel(mContext: Context)
    }

}