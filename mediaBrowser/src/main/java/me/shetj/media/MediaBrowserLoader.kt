package me.shetj.media

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import me.shetj.media.loader.MediaBrowserHelper

object MediaBrowserLoader{


    fun init(context: Context?) {
        requireNotNull(context) { "the provided context must not be null!" }

    }

    /**
     * 通过不同的 parentMediaId ,使用不同的加载方式
     */
    private val mediaLoadDataCallBack = HashMap<String, MediaBrowserHelper.OnSubscribeCallBack>()
    private var onCreateChannel: MediaBrowserHelper.OnCreateChannel?=null

    fun setOnCreateChannel(onCreateChannel: MediaBrowserHelper.OnCreateChannel):MediaBrowserLoader{
        MediaBrowserLoader.onCreateChannel = onCreateChannel
        return this
    }


    fun onLoadChildren(parentMediaId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) :MediaBrowserLoader{
        val callBack = mediaLoadDataCallBack[parentMediaId]
        callBack?.let {
            callBack.onLoadChildren(parentMediaId, result)
        }
        return this
    }

    fun addMediaLoadDataCallBack(parentMediaId:String, callBack: MediaBrowserHelper.OnSubscribeCallBack) :MediaBrowserLoader{
        mediaLoadDataCallBack[parentMediaId] = callBack
        return this
    }

    fun removeMediaLoadDataCallBack(parentMediaId:String) {
        mediaLoadDataCallBack.remove(parentMediaId)
    }


}