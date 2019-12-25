package me.shetj.mediabrowser

import android.Manifest
import android.provider.MediaStore
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shetj.base.base.BasePresenter
import me.shetj.base.base.IView
import me.shetj.base.kt.getRxPermissions
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.app.ArmsUtils.Companion.getMessage
import java.util.ArrayList

class MediaPresenter(view:IView) :BasePresenter<MediaModel>(view){
    private var musicList: ArrayList<Music>? = null
    init {
        model  = MediaModel()
    }


    fun initMusic() {
        view.rxContext.getRxPermissions()
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { aBoolean ->
                    if (aBoolean!!) {
                        loadFileData()
                    }
                }
    }

    /**
     * 查询本地的音乐文件
     */
    private fun loadFileData() {
        val disposable = Flowable.create(FlowableOnSubscribe<List<Music>> { emitter ->
            val resolver = view.rxContext.contentResolver
            val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null)
            cursor!!.moveToFirst()
            musicList = ArrayList()
            if (cursor.moveToFirst()) {
                do {
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                    val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    if (duration in 1000..2000000) {
                        val music = Music()
                        music.name = title
                        music.size = size
                        music.url = url
                        music.duration = duration
                        music.img  = album
                        musicList!!.add(music)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            if (musicList!!.size > 0) {
                emitter.onNext(musicList!!)
            } else {
                emitter.onError(Throwable("本地没有音乐~"))
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .compose(view.rxContext.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ music -> view.updateView(getMessage(1, music)) },
                        { throwable -> throwable.message?.let { ArmsUtils.makeText(it) } })
        addDispose(disposable)
    }
}
