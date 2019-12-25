/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.shetj.media.loader


import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.NonNull
import androidx.media.MediaBrowserServiceCompat
import me.shetj.media.BuildConfig
import me.shetj.media.R
import me.shetj.media.model.Music


import java.util.HashMap
import java.util.TreeMap
import java.util.concurrent.TimeUnit


object MetadataUtil {

    // 构造音频数据
    private val music = TreeMap<String, MediaMetadataCompat>()
    // 图片资源id
    private val albumRes = HashMap<String, String>()
    // 音频播放路径
    private val musicFileName = HashMap<String, String>()

    val root: String
        get() = "root"

    private fun getAlbumArtUri(albumArtResName: String): String {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtResName
    }

    fun getMusicFilename(mediaId: String): String? {
        return if (musicFileName.containsKey(mediaId)) musicFileName[mediaId] else null
    }

    private fun getAlbumRes(mediaId: String): String {
        return if (albumRes.containsKey(mediaId)) albumRes[mediaId]!! else ""
    }

    /**
     * 根据id 获取图片
     *
     * @param context
     * @param mediaId  可以对mediaID 头部做区分（本地音乐，应用内音乐，线上音乐）
     * @return
     */
    fun getAlbumBitmap(context: Context, mediaId: String): Bitmap? {

        return null
    }


    /**
     * 拷贝一份音频数据
     *
     * @param context
     * @param mediaId
     * @return
     */
    fun getMetadata(context: Context, mediaId: String): MediaMetadataCompat {
        // 根据id 音频列表获取音频数据
        val metadataWithoutBitmap = music[mediaId]
        // 获取音频图片数据
        val albumArt = getAlbumBitmap(context, mediaId)

        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
        val builder = MediaMetadataCompat.Builder()
        // 设置数据
        for (key in arrayOf(
                MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                MediaMetadataCompat.METADATA_KEY_GENRE,
                MediaMetadataCompat.METADATA_KEY_TITLE)) {
            builder.putString(key, metadataWithoutBitmap!!.getString(key))
        }
        //
        builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                metadataWithoutBitmap!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
        // 添加图片
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        return builder.build()
    }


    fun getMediaMetadataCompat(mediaId: String = "",
                               album: String = "",
                               artist: String = "",
                               duration: Long = 0L,
                               durationUnit: TimeUnit,
                               genre: String= "",
                               albumArtResName: String= "",
                               title: String= ""
                               ,fileUrl:String =""): MediaMetadataCompat{
        albumRes[mediaId] = album
        musicFileName[mediaId] = fileUrl
        val metadataCompat = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                        TimeUnit.MILLISECONDS.convert(duration, durationUnit))
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        getAlbumArtUri(albumArtResName))
                .putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                        getAlbumArtUri(albumArtResName))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build()
        music[mediaId] = metadataCompat
        return metadataCompat
    }




    @NonNull
    private fun createMediaItemAlbum(@NonNull music: Music): MediaBrowserCompat.MediaItem {
        val mediaMetadataCompat = getMediaMetadataCompat(mediaId = music.name!!,
            album = music.img!!,
            duration = music.duration,
            durationUnit = TimeUnit.MILLISECONDS,
            genre = "1",
            title = music.name!!
            ,fileUrl = music.url!!)
        return MediaBrowserCompat.MediaItem(mediaMetadataCompat.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
}