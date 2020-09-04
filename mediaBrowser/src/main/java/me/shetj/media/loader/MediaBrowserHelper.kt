package me.shetj.media.loader

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media2.common.BaseResult.RESULT_ERROR_UNKNOWN
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaLibraryService
import androidx.media2.session.MediaSession
import me.shetj.media.callback.OnSubscribeCallBack

/**
 * 帮助类，用设置自定义和默认情况
 */
internal object MediaBrowserHelper{
    /**
     * 通过不同的 parentMediaId ,使用不同的加载方式
     */
    private val mediaLoadDataCallBack = HashMap<String, OnSubscribeCallBack>()


    fun addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack) {
        mediaLoadDataCallBack[parentMediaId] = callBack
    }

    fun removeMediaLoadDataCallBack(parentMediaId:String) {
        mediaLoadDataCallBack.remove(parentMediaId)
    }

    /*********************************************** model 内使用的方法 ****************************************************************/


    /**
     * RESULT_SUCCESS 成功
     * RESULT_ERROR_UNKNOWN 失败
     */
    internal fun onSubscribe(
        sessionPlayer: SessionPlayer,
        controller: MediaSession.ControllerInfo,
        parentId: String,
        params: MediaLibraryService.LibraryParams?): Int {
        val callBack = mediaLoadDataCallBack[parentId]
        callBack?.let {
         return   callBack.onSubscribe(sessionPlayer, controller, parentId, params)
        }
        return RESULT_ERROR_UNKNOWN
    }

    internal fun checkParentId(parentId: String): Boolean {
       return mediaLoadDataCallBack.containsKey(parentId)
    }


    /*********************************************** 私有方法 ****************************************************************/



    internal fun getActivity(mContext: Context): PendingIntent {
        val openUI =
            mContext.packageManager.getLaunchIntentForPackage(mContext.packageName)
        openUI?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            mContext,
            100,
            openUI,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

}