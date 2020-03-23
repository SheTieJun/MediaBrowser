package me.shetj.mediabrowser

import android.content.Context
import android.provider.MediaStore
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import me.shetj.base.base.BasePresenter
import me.shetj.base.base.IView
import java.util.*

class MediaPresenter(view:IView) :BasePresenter<MediaModel>(view){
    init {
        model  = MediaModel()
    }

    /**
     * 查询本地的音乐文件
     */
    fun loadFileData(context: Context): Flowable<List<Music>> {
        return Flowable.create({ emitter ->
            val resolver = context.contentResolver
            val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null)
            cursor!!.moveToFirst()
            val musicList = ArrayList<Music>()
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
                        music.img = album
                        musicList.add(music)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            if (musicList.size > 0) {
                emitter.onNext(musicList)
            } else {
                emitter.onError(Throwable("本地没有音乐~"))
            }
        }, BackpressureStrategy.BUFFER)
    }
}
