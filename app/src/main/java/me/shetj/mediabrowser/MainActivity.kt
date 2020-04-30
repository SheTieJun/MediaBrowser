package me.shetj.mediabrowser

import android.Manifest
import android.os.Bundle
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import me.shetj.base.base.BaseActivity
import me.shetj.base.kt.getRxPermissions
import me.shetj.base.kt.toJson
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.media.MediaBrowserLoader
import me.shetj.media.loader.MetadataUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<MediaPresenter>() {

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
    }

    override fun initData() {
        mPresenter = MediaPresenter(this)
        mAdapter = MusicAdapter(ArrayList())
        iRecyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            run {
                val item = mAdapter.getItem(position)
                ArmsUtils.makeText(item.toJson() ?: "数据异常")
                MediaBrowserLoader.getMediaController()?.skipToPlaylistItem(position)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }


    //region 创建MediaItem
    private fun createMediaItemAlbum(music: Music): MediaMetadata {
        return MetadataUtil.getMediaMetadataCompat(
            mediaId = music.name!!,
            album = music.img!!,
            duration = music.duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = music.name!!
            , fileUrl = music.url!!
        )

    }

    private fun createMediaItemAlbum(music: NetMusic): MediaMetadata {
        return MetadataUtil.getMediaMetadataCompat(
            mediaId = music.url!!,
            album = music.imgUrl!!,
            duration = music.duration?.toLong() ?: 100,
            durationUnit = TimeUnit.SECONDS,
            genre = "1",
            title = music.title!!
            , fileUrl = music.url!!
        )
    }

    private fun createTestUrlMedia(
        url: String,
        name: String,
        duration: Long
    ):MediaMetadata{
       return   MetadataUtil.getMediaMetadataCompat(
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
      fun connectionCallback(isSuccess: Boolean) {
        Timber.w("connectionCallback:$isSuccess")
        if (isSuccess) {
            //连接成功，默认初始化网络数据
            subscribeNetWorkMusic()
        }
    }

      fun onPlaybackStateChanged(@SessionPlayer.PlayerState state: Int) {
        when (state) {
            SessionPlayer.PLAYER_STATE_IDLE -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            SessionPlayer.PLAYER_STATE_PAUSED -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
            SessionPlayer.PLAYER_STATE_PLAYING -> floatingActionButton.setImageResource(R.drawable.ic_media_pause)
            else -> floatingActionButton.setImageResource(R.drawable.ic_media_play)
        }
    }

    //endregion

    override fun onDestroy() {
        super.onDestroy()
    }
}
