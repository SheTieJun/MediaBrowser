package me.shetj.mediabrowser

import android.content.Context
import android.provider.MediaStore
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.SimpleCallBack
import com.zhouyou.http.exception.ApiException
import com.zhouyou.http.model.ApiResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import me.shetj.base.base.BasePresenter
import me.shetj.base.base.IView
import me.shetj.base.tools.json.GsonKit
import timber.log.Timber
import java.util.*

class MediaPresenter(view: IView) : BasePresenter<MediaModel>(view) {
    init {
        model = MediaModel()
    }

    /**
     * 查询本地的音乐文件
     */
    fun loadFileData(context: Context): Flowable<List<Music>> {
        return Flowable.create({ emitter ->
            val resolver = context.contentResolver
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null
            )
            cursor!!.moveToFirst()
            val musicList = ArrayList<Music>()
            if (cursor.moveToFirst()) {
                do {
                    val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                    val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val duration =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val album =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
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

    //模拟网络请求
    fun loadNetMusic(onSuccess: ((List<NetMusic>?) -> Unit)? = null) {
        val dateInfo = "{\"msg\": \"ok\", \"code\": 0, \"data\": [\n" +
                "{\"url\": \"https://media.lycheer.net/lecture/6583/5dba9468334c6837aee49262_transcoded.m4a\", \"title\": \"温暖春天的爱情\", \"duration\":\"180\", \"imgUrl\": \"https://img.lycheer.net/material/6583/5d70da288069bd42ddc9a961.png\"},\n" +
                "{\"url\": \"https://media.lycheer.net/lecture/6583/5dba946faa26622516464ad3_transcoded.m4a\", \"title\": \"浪漫的灵感\", \"duration\":\"150\", \"imgUrl\": \"https://img.lycheer.net/material/6583/5d6b813e06d1a720bf6a8b33.png\"}\n" +
                "]}"
        EasyHttp.get("https://xxxxx.xxx.com/an_music.json")
            .syncRequest(true) //必须同步
            .execute(object : SimpleCallBack<String>() {

                override fun onError(e: ApiException?) {
                    onSuccess?.invoke(
                        GsonKit.jsonToBean(
                            dateInfo,
                            NetMusicResult::class.java
                        )?.data
                    )
                }

                override fun onSuccess(t: String?) {
                    Timber.i(t)
                    onSuccess?.invoke(
                        GsonKit.jsonToBean(
                            dateInfo,
                            NetMusicResult::class.java
                        )?.data
                    )
                }
            })
    }

}
