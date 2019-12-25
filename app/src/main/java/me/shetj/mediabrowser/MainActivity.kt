package me.shetj.mediabrowser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.android.synthetic.main.activity_main.*
import me.shetj.base.base.BaseActivity
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.json.GsonKit
import me.shetj.media.browser.MediaBrowserManager
import java.util.ArrayList

class MainActivity : BaseActivity<MediaPresenter>(), MediaBrowserManager.OnMediaStatusChangeListener {

    private lateinit var browserManager: MediaBrowserManager
    private lateinit var mAdapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingActionButton.setOnClickListener {
            startOrPause()
        }
    }

    private fun startOrPause() {


    }

    override fun initView() {
        browserManager = MediaBrowserManager.getInstance(this)
        browserManager.addOnMediaStatusListener(this)
        browserManager.removeOnMediaStatusListener(this)
        browserManager.onStart()
    }

    override fun initData() {
        mPresenter = MediaPresenter(this)
        mAdapter = MusicAdapter(ArrayList())
        mAdapter.bindToRecyclerView(iRecyclerView)
        mAdapter.setOnItemClickListener { _, _, position ->
            run {
                val item = mAdapter.getItem(position)
                ArmsUtils.makeText(GsonKit.objectToJson(item!!)!!)
//                    mMediaController?.transportControls?.playFromMediaId(item.mediaId,null)
            }
        }
    }

    override fun connectionCallback(isSuccess: Boolean) {


    }


    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {

    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

    }

    override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {

    }

    override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {


    }

    override fun onDestroy() {
        super.onDestroy()
//        browserManager.onStop()
    }
}
