package me.shetj.mediabrowser

import android.content.Context
import android.provider.MediaStore
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import me.shetj.media.model.Music
import java.util.*


/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2019/7/20<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
object MusicUtils{
    /**
     * 查询本地的音乐文件
     */
      fun loadFileData(context:Context): Flowable<List<Music>> {
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


//    fun  getMusci(context: Context){

//        val musicList: MutableList<Music> = ArrayList<Music>()
//        val mVideoUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(MediaStore.Audio.Media._ID
//                ,MediaStore.Audio.Media.DATA
//                , MediaStore.Audio.Media.DISPLAY_NAME
//                , MediaStore.Audio.Media.DURATION
//                , MediaStore.Audio.Media.SIZE
//                , MediaStore.Audio.Media.DATE_ADDED
//                , MediaStore.Audio.Media.ALBUM
//                , MediaStore.Audio.Media.DATE_MODIFIED)
//        val mCursor: Cursor = context.getContentResolver().query(mVideoUri,
//                projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC ")!!
//
//        if (mCursor != null) {
//            while (mCursor.moveToNext()) { // 获取视频的路径
//                val videoId: Int = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID))
//                var path: String
//                path = if (Build.VERSION.SDK_INT === Build.VERSION_CODES.P) {
//                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                            .buildUpon()
//                            .appendPath(videoId.toString()).build().toString()
//                } else {
//                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA))
//                }
//                val duration: Long = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
//                var size: Long = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024 //单位kb
//                if (size < 0) { //某些设备获取size<0，直接计算
//                    size = File(path).length() / 1024
//                }
//                val displayName: String = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
//                //用于展示相册初始化界面
//                val timeIndex: Int = mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
//                val date: Long = mCursor.getLong(timeIndex) * 1000
//                val album = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
//                //需要判断当前文件是否存在  一定要加，不然有些文件已经不存在图片显示不出来。这里适配Android Q
//                synchronized(context) {
//                    val fileIsExists: Boolean
//                    if (Build.VERSION.SDK_INT === Build.VERSION_CODES.P) {
//                        fileIsExists = FileManagerUtils.isContentUriExists(MSApplication.getmContext(), Uri.parse(path))
//                        if (fileIsExists) {
//                            musicList.add(MediaData(videoId, MediaConstant.VIDEO, path, "", getVideoUri(mCursor), duration, date, displayName, false))
//                        }
//                    } else {
//                        if (duration in 1000..2000000) {
//                            val music = Music()
//                            music.name = displayName
//                            music.size = size
//                            music.url = path
//                            music.duration = duration
//                            music.img = album
//                            musicList.add(music)
//                        }
//                    }
//                }
//            }
//            mCursor.close()
//        }
//
//    }
}