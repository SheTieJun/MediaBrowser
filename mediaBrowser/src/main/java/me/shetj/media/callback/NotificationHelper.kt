package me.shetj.media.callback

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2019/12/25<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  通知栏创建 <br>
 */
interface NotificationHelper{


    /**
     * 渠道ID, 如果为空，会使用默认值
     */
    fun getChannelID(): String ?

    /**
     * 创建渠道
     */
    fun createChannel(mContext: Context)

    /**
     * 创建点击通知栏的意图
     */
    fun createContentIntent(): PendingIntent

    /**
     * 通过mediaID 获取bitmap
     */
    fun  getAlbumBitmap(mContext: Context, mediaId: String): Bitmap?

    /**
     * 获取 NotificationID
     */
    fun getNotificationID(): Int

}