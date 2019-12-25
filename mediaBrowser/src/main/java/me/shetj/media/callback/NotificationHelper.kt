package me.shetj.media.callback

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2019/12/25<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
interface NotificationHelper{
    fun createChannel(mContext: Context)

    fun createContentIntent(): PendingIntent

    fun  getAlbumBitmap(mContext: Context, mediaId: String): Bitmap?
}