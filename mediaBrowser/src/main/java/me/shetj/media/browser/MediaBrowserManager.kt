package me.shetj.media.browser

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media2.session.*
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.service.MusicService
import java.util.*
internal class MediaBrowserManager private constructor() {

    companion object {
        @Volatile
        private var browserManager :MediaBrowserManager ?=null

        fun getInstance() =
            browserManager ?: synchronized(this) {
                MediaBrowserManager().also { browserManager = it }
            }

    }

    private var mContext:Context ? = null
    private var mMediaBrowserCompat: MediaBrowser? = null
    private val mMediaStatusChangeListenerList = ArrayList<OnMediaStatusChangeListener>()


    fun getMediaController(): MediaBrowser ?{
        return mMediaBrowserCompat
    }

    // ########################################音频变化回调 管理列表###################################################


    /**
     * 跟随Activity的生命周期
     */
    fun onStart(mContext: Context) {
        this.mContext = mContext
        if (mMediaBrowserCompat == null) {
            // 创建MediaBrowserCompat
            mMediaBrowserCompat = MediaBrowser.Builder(mContext)
                .setSessionToken(SessionToken(mContext,ComponentName(mContext, MusicService::class.java)))
                .setControllerCallback(ContextCompat.getMainExecutor(mContext),object :MediaBrowser.BrowserCallback(){
                    override fun onConnected(
                        controller: MediaController,
                        allowedCommands: SessionCommandGroup
                    ) {
                        super.onConnected(controller, allowedCommands)
                        for (callback in mMediaStatusChangeListenerList) {
                            callback.connectionCallback(true)
                        }
                    }


                    override fun onDisconnected(controller: MediaController) {
                        super.onDisconnected(controller)
                        for (callback in mMediaStatusChangeListenerList) {
                            callback.connectionCallback(false)
                        }
                    }

                    override fun onChildrenChanged(
                        browser: MediaBrowser,
                        parentId: String,
                        itemCount: Int,
                        params: MediaLibraryService.LibraryParams?
                    ) {
                        super.onChildrenChanged(browser, parentId, itemCount, params)

                    }

                    override fun onSearchResultChanged(
                        browser: MediaBrowser,
                        query: String,
                        itemCount: Int,
                        params: MediaLibraryService.LibraryParams?
                    ) {
                        super.onSearchResultChanged(browser, query, itemCount, params)
                    }
                })
                .build()
        }
    }

    fun subscribe(parentId:String){
        mMediaBrowserCompat?.subscribe(parentId,null)
    }

    fun unSubscribe(parentId:String){
        mMediaBrowserCompat?.unsubscribe(parentId)
    }


    fun addOnMediaStatusListener(l: OnMediaStatusChangeListener) {
        if (!mMediaStatusChangeListenerList.contains(l)) {
            mMediaStatusChangeListenerList.add(l)
        }
    }

    /**
     * 移除音频变化回调
     *
     * @param l
     */
    fun removeOnMediaStatusListener(l: OnMediaStatusChangeListener) {
        mMediaStatusChangeListenerList.remove(l)
    }


}