package me.shetj.media

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.media2.common.MediaItem
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaBrowser
import androidx.media2.session.MediaLibraryService
import me.shetj.media.browser.MediaBrowserManager
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.loader.MediaBrowserHelper
import java.util.concurrent.atomic.AtomicBoolean

object MediaBrowserLoader{
    private var browserManager = MediaBrowserManager.getInstance()
    private val isStart = AtomicBoolean(false)

    /**
     * 1、如果音乐服务没有开启，就开始服务
     * 2、连接服务
     */
    @JvmStatic
    fun startBrowser(context: Context){
        browserManager.onStart(context.applicationContext)
        isStart.compareAndSet(false,true)
//        getLibraryRoot()
    }


    /**
     * 获取[MediaControllerCompat]
     */
    @JvmStatic
    fun getMediaController(): MediaBrowser{
        checkLoaderInit()
        return browserManager.getMediaBrowser()
    }

    fun getLibraryRoot() {
        getMediaController().getLibraryRoot(getLibraryParams(mRecent = true,mOffline = true,mSuggested = true))
    }

    @JvmOverloads
    fun getLibraryParams( mRecent: Boolean = false,
                          mOffline: Boolean = false,
                          mSuggested :Boolean= false,
                          extras: Bundle?=null): MediaLibraryService.LibraryParams {
       return MediaLibraryService.LibraryParams.Builder()
            .setExtras(extras)
            .setRecent(mRecent)
            .setOffline(mOffline)
            .setSuggested(mSuggested)
            .build()
    }

    /**
     * 设置播放模式
     * 暂时 不支持
     */
    @JvmStatic
    private fun setRepeatMode(@SessionPlayer.RepeatMode repeatMode:Int){
        getMediaController().repeatMode = repeatMode
    }

    /**
     * 通知获取service 进行 subscribe [parentId] ,然后回调 [OnSubscribeCallBack]
     */
    @JvmStatic
    fun subscribe(parentId:String) {
        checkLoaderInit()
        checkHelper(parentId)
        browserManager.subscribe(parentId)
    }

    @JvmStatic
    fun unSubscribe(parentId: String) {
        checkLoaderInit()
        browserManager.unSubscribe(parentId)
    }

    /**
     * 添加 [parentMediaId] 的加载数据的方式
     * 只会存在唯一的一个
     */
    @JvmStatic
    fun addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack) :MediaBrowserLoader{
        MediaBrowserHelper.addMediaLoadDataCallBack(parentMediaId, callBack)
        return this
    }

    /**
     * 移除 [parentMediaId] 的加载数据的方式
     */
    @JvmStatic
    fun removeMediaLoadDataCallBack(parentMediaId:String) :MediaBrowserLoader{
        MediaBrowserHelper.removeMediaLoadDataCallBack(parentMediaId)
        return this
    }

    /**
     * 添加音频变化回调
     * @param listener
     */
    @JvmStatic
    fun addOnMediaStatusListener(listener : OnMediaStatusChangeListener) :MediaBrowserLoader{
        checkLoaderInit()
        browserManager.addOnMediaStatusListener(listener)
        return this
    }

    /**
     * 移除音频变化回调
     * @param listener
     */
    @JvmStatic
    fun removeOnMediaStatusListener(listener: OnMediaStatusChangeListener) :MediaBrowserLoader{
        checkLoaderInit()
        browserManager.removeOnMediaStatusListener(listener)
        return this
    }


    @SuppressLint("RestrictedApi")
    fun playPosition(position:Int) {
        getMediaController().apply {
            if (playerState == SessionPlayer.PLAYER_STATE_PLAYING) {
                pause()
            }
            skipToPlaylistItem(position) //回调
            prepare()
            play()
        }
    }


    fun playPosition(item: MediaItem) {
        getMediaController().apply {
            if (playerState == SessionPlayer.PLAYER_STATE_PLAYING) {
                pause()
            }
            playlist?.indexOf(item)?.apply {
                skipToPlaylistItem(this)
                prepare()
                play()
            }
        }
    }

    /***********************************私有方法***************************************************/

    private fun checkLoaderInit() {
        if (!isStart.get()) {
            throw IllegalStateException("u should start")
        }
    }

    private fun checkHelper(parentId: String) {
        if (!MediaBrowserHelper.checkParentId(parentId)){
            throw NullPointerException("$parentId has no OnSubscribeCallBack. You should " +
                    "addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack)")
        }
    }
}