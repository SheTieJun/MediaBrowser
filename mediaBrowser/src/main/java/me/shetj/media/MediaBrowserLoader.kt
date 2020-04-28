package me.shetj.media

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import me.shetj.media.browser.MediaBrowserManager
import me.shetj.media.callback.NotificationHelper
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.loader.MediaBrowserHelper
import me.shetj.media.loader.MediaConstant.Companion.COMMAND_CLEAR
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicBoolean

object MediaBrowserLoader{

    private lateinit var browserManager: MediaBrowserManager
    private val isInit = AtomicBoolean(false)
    private val isStart = AtomicBoolean(false)

    /**
     * 初始化，必须执行的方法
     */
    @JvmStatic
    fun init() :MediaBrowserLoader{
        if (!isInit.get()) {
            browserManager = MediaBrowserManager.getInstance()
            isInit.compareAndSet(false,true)
        }
        return this
    }

    /**
     * 1、如果音乐服务没有开启，就开始服务
     * 2、连接服务
     */
    @JvmStatic
    fun start(context: Context){
        checkLoaderInit()
        browserManager.onStart(context.applicationContext)
        isStart.compareAndSet(false,true)
    }

    /**
     * 获取音乐控制器
     */
    @JvmStatic
    fun getTransportControls(): MediaControllerCompat.TransportControls? {
        checkLoaderInit()
        return browserManager.transportControls
    }

    /**
     * 获取[MediaControllerCompat]
     */
    @JvmStatic
    fun getMediaController(): MediaControllerCompat? {
        checkLoaderInit()
        return browserManager.getMediaController()
    }

    /**
     * 停止更新界面，但是没有关闭服务
     * 当下一次链接服务
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
     * 暂时 不支持
     * TODO 替换到exoPlayer?
     */
    @JvmStatic
    private fun setRepeatMode(@PlaybackStateCompat.RepeatMode repeatMode:Int){
        getTransportControls()?.setRepeatMode(repeatMode)
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
     * @param isClearPlayList if true will stop media and clear list
     */
    @JvmStatic
    fun unSubscribe(parentId:String,isClearPlayList:Boolean = true) {
        checkLoaderInit()
        browserManager.unSubscribe(parentId)
        if (isClearPlayList) {
            getMediaController()?.sendCommand(COMMAND_CLEAR, null, null)
        }
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

    private fun checkHelper(parentId: String) {
        if (!MediaBrowserHelper.checkParentId(parentId)){
            throw NullPointerException("$parentId has no OnSubscribeCallBack. You should " +
                    "addMediaLoadDataCallBack(parentMediaId:String, callBack: OnSubscribeCallBack)")
        }
    }

}