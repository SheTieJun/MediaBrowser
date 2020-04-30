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


import android.content.Context
import android.graphics.Bitmap
import androidx.media2.common.MediaMetadata
import java.util.*
import java.util.concurrent.TimeUnit


object MetadataUtil {

    // 构造音频数据
    private val music = TreeMap<String, MediaMetadata>()
    // 图片资源id
    private val albumRes = HashMap<String, String>()
    // 音频播放路径
    private val musicFileName = HashMap<String, String>()

    val root: String
        get() = "root"

    fun getMusicFilename(mediaId: String): String? {
        return if (musicFileName.containsKey(mediaId)) musicFileName[mediaId] else null
    }

    /**
     * 根据id 获取图片
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
    fun getMetadata(context: Context, mediaId: String): MediaMetadata {
        // 根据id 音频列表获取音频数据
        val metadataWithoutBitmap = music[mediaId]
        // 获取音频图片数据
        val albumArt = getAlbumBitmap(context, mediaId)
        val builder = MediaMetadata.Builder()
        // 设置数据
        for (key in arrayOf(
            MediaMetadata.METADATA_KEY_MEDIA_ID,
            MediaMetadata.METADATA_KEY_ALBUM,
            MediaMetadata.METADATA_KEY_ARTIST,
            MediaMetadata.METADATA_KEY_GENRE,
            MediaMetadata.METADATA_KEY_TITLE)) {
            builder.putString(key, metadataWithoutBitmap!!.getString(key))
        }
        //
        builder.putLong(
            MediaMetadata.METADATA_KEY_DURATION,
            metadataWithoutBitmap!!.getLong(MediaMetadata.METADATA_KEY_DURATION))
        // 添加图片
        builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
        return builder.build()
    }


    fun getMediaMetadataCompat(mediaId: String = "",
                               album: String = "",
                               artist: String = "",
                               duration: Long = 0L,
                               durationUnit: TimeUnit,
                               genre: String= "",
                               title: String= ""
                               ,fileUrl:String =""): MediaMetadata{
        albumRes[mediaId] = album
        musicFileName[mediaId] = fileUrl
        val metadataCompat = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadata.METADATA_KEY_DURATION,
                TimeUnit.MILLISECONDS.convert(duration, durationUnit))
            .putString(MediaMetadata.METADATA_KEY_GENRE, genre)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, album)
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, album)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .build()
        music[mediaId] = metadataCompat
        return metadataCompat
    }

}