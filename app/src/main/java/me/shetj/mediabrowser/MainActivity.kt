package me.shetj.mediabrowser

import android.Manifest
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.trello.rxlifecycle3.RxLifecycle
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import me.shetj.base.base.BaseActivity
import me.shetj.base.kt.getRxPermissions
import me.shetj.base.kt.showToast
import me.shetj.base.kt.toJson
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.kt.currentPlayBackPosition
import me.shetj.media.kt.startOrPause
import me.shetj.media.loader.MetadataUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<MediaPresenter>(), OnMediaStatusChangeListener {

    private var subscribe: Disposable? = null
    private lateinit var mAdapter: MusicAdapter
    private val parentId = "Local_music"
    private val parentId2 = "netMusic"
    private val rxPermission = getRxPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MediaBrowserLoader.init()
        floatingActionButton.setOnClickListener {
            MediaBrowserLoader.startOrPause()
        }
        //点击切换
        btn_changeMusic.setOnClickListener {
            changeSubscribeMusic()
        }
    }


    private fun changeSubscribeMusic() {
        if (btn_changeMusic.text != parentId) {
            subscribeLocalMusic()
        } else {
            subscribeNetWorkMusic()
        }
    }


    override fun initView() {
        // onLoadChildren 这个方法会在     MediaBrowserLoader.subscribe(parentId) 之后回调
        MediaBrowserLoader.addOnMediaStatusListener(this)
            .addMediaLoadDataCallBack(parentId, object : OnSubscribeCallBack {
                override fun onLoadChildren(
                    parentMediaId: String,
                    result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
                ) {
                    mAdapter.setNewData(null)
                    mPresenter?.apply {
                        addDispose(MusicUtils.loadFileData(rxContext).map {
                            //本地音频
                            ArrayList<MediaBrowserCompat.MediaItem>().apply {
                                //网络音频测试
                                add(
                                    createTestUrlMedia(
                                        "https://media.lycheer.net/lecture/6583/5dba9468334c6837aee49262_transcoded.m4a",
                                        "网路：温暖春天的爱情",
                                        100 * 1000
                                    )
                                )
                                addAll(it.map {
                                    createMediaItemAlbum(it)
                                })
                            }
                        }.subscribe({
                            result.sendResult(it)
                        }, {
                            Timber.e(it)
                            result.sendResult(null)
                        }))
                    }
                }
            })
        MediaBrowserLoader.addMediaLoadDataCallBack(parentId2, object : OnSubscribeCallBack {
            override fun onLoadChildren(
                parentMediaId: String,
                result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
            ) {
                mAdapter.setNewData(null)
                //如果在这里进行网络请求必须，要求网络时同步请求
                mPresenter?.loadNetMusic {
                    if (it == null) {
                        result.sendResult(null)
                    } else {
                        it.map { music ->
                            createMediaItemAlbum(music)
                        }.apply {
                            result.sendResult(this)
                        }
                    }
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
                ArmsUtils.makeText(item.toJson() ?: "数据异常")
                MediaBrowserLoader.getTransportControls()?.playFromMediaId(item.mediaId, null)
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
        return MediaBrowserCompat.MediaItem(
            mediaMetadataCompat.description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    private fun createMediaItemAlbum(music: NetMusic): MediaBrowserCompat.MediaItem {
        val mediaMetadataCompat = MetadataUtil.getMediaMetadataCompat(
            mediaId = music.url!!,
            album = music.imgUrl!!,
            duration = music.duration?.toLong() ?: 100,
            durationUnit = TimeUnit.SECONDS,
            genre = "1",
            title = music.title!!
            , fileUrl = music.url!!
        )
        return MediaBrowserCompat.MediaItem(
            mediaMetadataCompat.description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    private fun createTestUrlMedia(
        url: String,
        name: String,
        duration: Long
    ): MediaBrowserCompat.MediaItem {
        val mediaMetadataCompat = MetadataUtil.getMediaMetadataCompat(
            mediaId = url,
            album = "",
            duration = duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = name,
            fileUrl = url
        )
        return MediaBrowserCompat.MediaItem(
            mediaMetadataCompat.description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
    //endregion 创建MediaItem

    //region 开始订阅
    private fun subscribeLocalMusic() {
        mPresenter?.addDispose(rxPermission
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe {
                if (it) {
                    btn_changeMusic.text = parentId
                    MediaBrowserLoader.unSubscribe(parentId2)
                    MediaBrowserLoader.subscribe(parentId)
                }
            })
    }

    private fun subscribeNetWorkMusic() {
        btn_changeMusic.text = parentId2
        MediaBrowserLoader.unSubscribe(parentId)
        MediaBrowserLoader.subscribe(parentId2)

        //建议时先获取网络列表，在进行订阅
        //   mPresenter?.loadNetMusic {
        //     成功后，先保存数据，然后切换
        //              btn_changeMusic.text = parentId2
        //        MediaBrowserLoader.unSubscribe(parentId)
        //        MediaBrowserLoader.subscribe(parentId2)
        //  }
    }
    //endregion


    //region 回调部分
    override fun connectionCallback(isSuccess: Boolean) {
        Timber.w("connectionCallback:$isSuccess")
        if (isSuccess) {
            //连接成功，默认初始化网络数据
            subscribeNetWorkMusic()
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        Timber.w("onPlaybackStateChanged : currentPosition =  ${state?.currentPlayBackPosition}")
        when (state?.state) {
            PlaybackStateCompat.STATE_NONE -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            PlaybackStateCompat.STATE_PAUSED -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            PlaybackStateCompat.STATE_PLAYING -> floatingActionButton.setImageResource(R.drawable.ic_media_pause)
            else -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
        }
        setTimeConfig(state)
        //PlaybackStateCompat.STATE_PLAYING 如果需要不断获取进度 就每隔几百毫秒掉一次 ${state?.currentPlayBackPosition }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Timber.w("onMetadataChanged")
        metadata?.let {

            Timber.i("onMetadataChanged: 获取总时长  duration = ${metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)}")
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
        "当前是：$parentId".showToast()
    }
    //endregion

    private fun setTimeConfig(state: PlaybackStateCompat?) {
        if (state?.state == PlaybackStateCompat.STATE_PLAYING) {
            disSubscribe()
            //每300 毫秒更新一次
            subscribe = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .compose(RxLifecycle.bindUntilEvent(lifecycle(), ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    time.text = getCurrentTime(state)
                }.subscribe()
        } else {
            disSubscribe()
        }
    }

    private fun disSubscribe() {
        if (subscribe != null && !subscribe!!.isDisposed) {
            subscribe?.dispose()
        }
    }

    private fun getCurrentTime(state: PlaybackStateCompat?): String {
        return "${state?.currentPlayBackPosition?.let { it1 ->
            MusicUtils.formatTime(
                it1
            )
        }}"
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaBrowserLoader.removeOnMediaStatusListener(this)
    }
}
