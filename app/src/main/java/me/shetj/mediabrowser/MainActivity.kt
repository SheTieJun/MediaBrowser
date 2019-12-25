package me.shetj.mediabrowser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import me.jessyan.autosize.utils.LogUtils
import me.shetj.base.base.BaseActivity
import me.shetj.base.kt.getRxPermissions
import me.shetj.base.kt.showToast
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.json.GsonKit
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.loader.MetadataUtil
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<MediaPresenter>(), OnMediaStatusChangeListener {

    private lateinit var mAdapter: MusicAdapter
    private val parentId  = "Local_music"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingActionButton.setOnClickListener {
            startOrPause()
        }
    }

    private fun startOrPause() {
        when(MediaBrowserLoader.getMediaController()?.playbackState?.state){
            PlaybackStateCompat.STATE_PLAYING ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.pause()
            }
            PlaybackStateCompat.STATE_PAUSED ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.play()
            }
            else ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.play()
            }
        }
    }

    override fun initView() {


        MediaBrowserLoader.init(this)
            .addOnMediaStatusListener(this)
            .addMediaLoadDataCallBack(parentId,object :OnSubscribeCallBack{
                override fun onLoadChildren(parentMediaId: String,
                    result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
                    when(parentMediaId)
                    {
                        parentId ->{
                            MusicUtils.loadFileData(rxContext).map {
                                val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                                it.apply {
                                    forEach {
                                        mediaItems.add(createMediaItemAlbum(it))
                                    }
                                }
                                mediaItems
                            }.subscribe({
                                result.sendResult(it)
                            },{
                                Timber.e(it)
                            })
                        }
                    }
                }
            }).start()
    }

    private fun createMediaItemAlbum(music: Music): MediaBrowserCompat.MediaItem {
        val mediaMetadataCompat = MetadataUtil.getMediaMetadataCompat(
            mediaId = music.name!!,
            album = music.img!!,
            duration = music.duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = music.name!!
            , fileUrl = music.url!!
        )
        return MediaBrowserCompat.MediaItem(mediaMetadataCompat.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)

    }

    override fun initData() {
        mPresenter = MediaPresenter(this)
        mAdapter = MusicAdapter(ArrayList())
        mAdapter.bindToRecyclerView(iRecyclerView)
        mAdapter.setOnItemClickListener { _, _, position ->
            run {
                val item = mAdapter.getItem(position)
                ArmsUtils.makeText(GsonKit.objectToJson(item!!)!!)
                MediaBrowserLoader.getTransportControls().playFromMediaId(item.mediaId,null)
            }
        }
    }

    override fun connectionCallback(isSuccess: Boolean) {
        if (isSuccess){
            getRxPermissions().request(android.Manifest.permission.READ_EXTERNAL_STORAGE
                ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    if (it){
                        MediaBrowserLoader.subscribe(parentId)
                    }
                }
        }
    }


    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        //音乐播放状态改变回调
        when (state?.state) {
            PlaybackStateCompat.STATE_NONE -> "需要点击开始".showToast()
            PlaybackStateCompat.STATE_PAUSED -> "需要点击重新开始".showToast()
            PlaybackStateCompat.STATE_PLAYING -> "需要点击暂停".showToast()
            else -> "需要点击开始".showToast()
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        metadata?.let {
            mAdapter.selectMediaId(it.description.mediaId)
        }
    }

    override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {
        Timber.w("onQueueChanged")
        //播放列表在变化
    }

    /**
     * 加载成功的数据
     */
    override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
        Timber.i("onChildrenLoaded--- $parentId")
        //children 为来自Service的列表数据
        children.forEach{
            Timber.i(it.description.title.toString())
        }

        mAdapter.setNewData(children)
        Timber.i("setNewData---")
        if (MediaBrowserLoader.getMediaController() == null) {
            return
        }
//            // Queue up all media items for this simple sample.
        for (mediaItem in children) {
            MediaBrowserLoader.getMediaController()?.addQueueItem(mediaItem.description)
        }
        // Call "playFromMedia" so the UI is updated.
        MediaBrowserLoader.getMediaController()?.transportControls?.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaBrowserLoader.removeOnMediaStatusListener(this)
//        MediaBrowserLoader.stop()
    }
}