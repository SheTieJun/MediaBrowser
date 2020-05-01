package me.shetj.mediabrowser

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.media2.common.*
import androidx.media2.common.BaseResult.RESULT_SUCCESS
import androidx.media2.session.MediaController
import androidx.media2.session.MediaLibraryService
import androidx.media2.session.MediaSession
import kotlinx.android.synthetic.main.activity_main.*
import me.shetj.base.base.BaseActivity
import me.shetj.base.kt.getRxPermissions
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.callback.OnMediaStatusChangeListener
import me.shetj.media.callback.OnSubscribeCallBack
import me.shetj.media.kt.getMediaMetadataCompat
import me.shetj.media.kt.toFileItem
import me.shetj.media.kt.toMediaItem
import me.shetj.media.kt.toUriItem
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<MediaPresenter>(), OnMediaStatusChangeListener {

    private lateinit var mAdapter: MusicAdapter
    private val parentId = "Local_music"
    private val parentId2 = "netMusic"
    private val rxPermission = getRxPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MediaBrowserLoader.startBrowser(this)
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
        MediaBrowserLoader
            .addOnMediaStatusListener(this)
            .addMediaLoadDataCallBack(parentId, object : OnSubscribeCallBack {

                override fun onSubscribe(
                    sessionPlayer: SessionPlayer,
                    controller: MediaSession.ControllerInfo,
                    parentId: String,
                    params: MediaLibraryService.LibraryParams?
                ): Int {
                    Log.i("MediaBrowser","onSubscribe  $parentId MainActivity"  )
                    val list = MusicUtils.loadFileData(rxContext).map {
                        //本地音频
                        ArrayList<MediaItem>().apply {
                            //网络音频测试
//                            add(
//                                createTestUrlMedia(
//                                    "https://media.lycheer.net/lecture/6583/5dba9468334c6837aee49262_transcoded.m4a",
//                                    "网路：温暖春天的爱情",
//                                    100 * 1000
//                                )
//                            )
                            addAll(it.map {
                                createMediaItemAlbum(it)
                            })
                        }
                    }.blockingFirst()
                    createTestUrlMetadata(
                        "https://media.lycheer.net/lecture/6583/5dba9468334c6837aee49262_transcoded.m4a",
                        "网路：温暖春天的爱情",
                        100 * 1000
                    )
                    sessionPlayer.setPlaylist(list,null )
                    return RESULT_SUCCESS
                }
            })
        MediaBrowserLoader.addMediaLoadDataCallBack(parentId2, object : OnSubscribeCallBack {

            override fun onSubscribe(
                sessionPlayer: SessionPlayer,
                controller: MediaSession.ControllerInfo,
                parentId: String,
                params: MediaLibraryService.LibraryParams?
            ): Int {
                //如果在这里进行网络请求必须，要求网络时同步请求
                mPresenter?.loadNetMusic {
                        it?.map { music ->
                            createMediaItemAlbum(music)
                        }?.apply {
                            sessionPlayer.setPlaylist(this,null)
                        }
                    }
                return RESULT_SUCCESS
            }
        })

    }

    fun String?.showLog(){
        Log.i("MediaBrowser",this)
    }

    override fun initData() {
        mPresenter = MediaPresenter(this)
        mAdapter = MusicAdapter(ArrayList())
        iRecyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            run {
                val item = mAdapter.getItem(position)
                MediaBrowserLoader.getMediaController()?.skipToPlaylistItem(position)
                MediaBrowserLoader.getMediaController()?.prepare()
                MediaBrowserLoader.getMediaController()?.play()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }


    //region 创建MediaItem
    private fun createMediaItemAlbum(music: Music): FileMediaItem {
        return getMediaMetadataCompat(
            mediaId = music.name!!,
            album = music.img!!,
            duration = music.duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = music.name!!
            , fileUrl = music.url!!
        ).toFileItem()
    }

    private fun createMediaItemAlbum(music: NetMusic): UriMediaItem {
        return  getMediaMetadataCompat(
            mediaId = music.url!!,
            album = music.imgUrl!!,
            duration = 214112,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = music.title!!
            , fileUrl = music.url!!
        ).toUriItem()
    }

    private fun createTestUrlMedia(
        url: String,
        name: String,
        duration: Long
    ): MediaItem {


       return   getMediaMetadataCompat(
            mediaId = url,
            album = "",
            duration = duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = name,
            fileUrl = url
        ).toMediaItem()


    }

    private fun createTestUrlMetadata(
        url: String,
        name: String,
        duration: Long
    ): MediaMetadata {

        return   getMediaMetadataCompat(
            mediaId = url,
            album = "",
            duration = duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = name,
            fileUrl = url
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
        if (isSuccess) {
            subscribeNetWorkMusic()
        }
    }

    override fun onPlayerStateChanged(state: Int) {
        when (state) {
            SessionPlayer.PLAYER_STATE_IDLE -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            SessionPlayer.PLAYER_STATE_PAUSED -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            SessionPlayer.PLAYER_STATE_PLAYING -> floatingActionButton.setImageResource(R.drawable.ic_media_pause)
            else -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
        }
    }

    override fun onPlaylistMetadataChanged(metadata: MediaMetadata?) {
        mAdapter.selectMediaId(metadata?.mediaId)
    }

    override fun onPlaylistChanged(list: MutableList<MediaItem>?, metadata: MediaMetadata?) {
        mAdapter.setNewData(list)
        mAdapter.selectMediaId(metadata?.mediaId)
    }

    override fun onPlaybackCompleted() {
    }

    override fun onPlaybackSpeedChanged(controller: MediaController, speed: Float) {

    }

    override fun onDestroy() {
        super.onDestroy()
        MediaBrowserLoader
            .removeOnMediaStatusListener(this)
    }

}
