package me.shetj.media

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import me.shetj.media.browser.MediaBrowserManager
import me.shetj.media.callback.NotificationHelper
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.loader.MediaBrowserHelper
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicBoolean

object MediaBrowserLoader{

    @SuppressLint("StaticFieldLeak")
    private lateinit var browserManager: MediaBrowserManager
    private val isInit = AtomicBoolean(false)
    private val isStart = AtomicBoolean(false)

    @JvmStatic
    fun initAndStart(context: Context?) :MediaBrowserLoader{
        init(context)
        start()
        return this
    }

    /**
     * 初始化，必须执行的方法
     */
    @JvmStatic
    fun init(context: Context?) :MediaBrowserLoader{
        if (!isInit.get()) {
            requireNotNull(context) { "the provided context must not be null!" }
            browserManager = MediaBrowserManager.getInstance(context.applicationContext)
            isInit.compareAndSet(false,true)
        }
        return this
    }

    /**
     * 开始服务
     */
    @JvmStatic
    fun start(){
        checkLoaderInit()
        browserManager.onStart()
        isStart.compareAndSet(false,true)
    }

    /**
     * 获取音乐控制器
     */
    @JvmStatic
    fun getTransportControls(): MediaControllerCompat.TransportControls {
        checkStart()
        return browserManager.transportControls
    }

    /**
     * 获取[MediaControllerCompat]
     */
    @JvmStatic
    fun getMediaController(): MediaControllerCompat? {
        checkStart()
        return browserManager.getMediaController()
    }

    /**
     * 关闭音乐播放服务
     */
    @JvmStatic
    fun stop(){
        if (isStart.get()) {
            browserManager.onStop()
            isStart.set(false)
        }
    }

    /**
     * 设置播放模式
     */
    @JvmStatic
    fun setRepeatMode(@PlaybackStateCompat.RepeatMode repeatMode:Int){
        getTransportControls().setRepeatMode(repeatMode)
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
     * 自定义[Notification]的构建
     */
    @JvmStatic
    fun setNotificationHelper(notificationHelper: NotificationHelper):MediaBrowserLoader{
        MediaBrowserHelper.setNotificationHelper(notificationHelper)
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

    /***********************************私有方法***************************************************/

    private fun checkLoaderInit() {
        if (!isInit.get()) {
            throw Exception("u should init first")
        }
    }

    private fun checkStart() {
        if (!isStart.get()) {
            throw Exception("u should init first")
        }
    }

    private fun checkHelper(parentId: String) {
        if (!MediaBrowserHelper.checkParentId(parentId)){
            throw NullPointerException("$parentId has no OnSubscribeCallBack. You should " +
                    "addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack)")
        }
    }

}