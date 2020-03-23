package me.shetj.mediabrowser

import android.Manifest
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import kotlinx.android.synthetic.main.activity_main.*
import me.shetj.base.base.BaseActivity
import me.shetj.base.kt.getRxPermissions
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.json.GsonKit
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.kt.startOrPause
import me.shetj.media.loader.MetadataUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<MediaPresenter>(), OnMediaStatusChangeListener {

    private lateinit var mAdapter: MusicAdapter
    private val parentId  = "Local_music"

    private val rxPermission = getRxPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MediaBrowserLoader.init()
        floatingActionButton.setOnClickListener {
            MediaBrowserLoader.startOrPause()
        }
    }


    override fun initView() {
        MediaBrowserLoader.addOnMediaStatusListener(this)
            .addMediaLoadDataCallBack(parentId,object :OnSubscribeCallBack{
                override fun onLoadChildren(parentMediaId: String,
                                            result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
                    mPresenter?.apply {
                        addDispose(MusicUtils.loadFileData(rxContext).map {
                            //本地音频
                            val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                            it.apply {
                                forEach { it1 ->
                                    mediaItems.add(createMediaItemAlbum(it1))
                                }
                            }
                            //网络音频测试
                            mediaItems.add(createTestUrlMedia())
                            mediaItems
                        }.subscribe({
                            result.sendResult(it)
                        }, {
                            Timber.e(it)
                            result.sendResult(null)
                        }))
                    }
                }
            })
    }

    override fun initData() {
        mPresenter = MediaPresenter(this)
        mAdapter = MusicAdapter(ArrayList())
        iRecyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            run {
                val item = mAdapter.getItem(position)
                ArmsUtils.makeText(GsonKit.objectToJson(item!!)!!)
                MediaBrowserLoader.getTransportControls()?.playFromMediaId(item.mediaId,null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MediaBrowserLoader.start(this)
    }

    override fun onStop() {
        super.onStop()
        //停止更新界面
        MediaBrowserLoader.stop()
    }

    //region 创建MediaItem
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

    private fun createTestUrlMedia(): MediaBrowserCompat.MediaItem {
        val mediaMetadataCompat = MetadataUtil.getMediaMetadataCompat(
            mediaId = "测试音乐",
            album = "",
            duration = 100,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = "测试音乐",
            fileUrl = "https://xxxxxx.m4a"
        )
        return MediaBrowserCompat.MediaItem(mediaMetadataCompat.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
    //endregion


    private fun subscribeLocalMusic() {
        mPresenter?.addDispose(rxPermission
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe {
                if (it) {
                    MediaBrowserLoader.subscribe(parentId)
                }
            })
    }

    override fun connectionCallback(isSuccess: Boolean) {
        Timber.w("connectionCallback:$isSuccess")
        if (isSuccess){
            subscribeLocalMusic()
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        Timber.w("onPlaybackStateChanged")
        when (state?.state) {
            PlaybackStateCompat.STATE_NONE -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            PlaybackStateCompat.STATE_PAUSED -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            PlaybackStateCompat.STATE_PLAYING -> floatingActionButton.setImageResource(R.drawable.ic_media_pause)
            else ->floatingActionButton.setImageResource(R.drawable.ic_media_play)
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Timber.w("onMetadataChanged")
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
     * @param children 为来自Service的列表数据
     */
    override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
        Timber.i("onChildrenLoaded--- $parentId")
        mAdapter.setNewData(children.toMutableList())
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaBrowserLoader.removeOnMediaStatusListener(this)
    }
}
