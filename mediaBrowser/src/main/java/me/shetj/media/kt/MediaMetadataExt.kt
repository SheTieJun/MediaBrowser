package me.shetj.media.kt

import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.os.ParcelFileDescriptor.MODE_READ_WRITE
import androidx.media2.common.FileMediaItem
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.MediaMetadata.METADATA_KEY_ALBUM_ART
import androidx.media2.common.UriMediaItem
import java.io.File
import java.util.concurrent.TimeUnit

fun MediaMetadata.toMediaItem():MediaItem{


   return MediaItem.Builder()
        .setMetadata(this)
        .build()
}

fun getMediaMetadataCompat(mediaId: String = "",
                           album: String ="",
                           album_art:Bitmap?=null,
                           artist: String? = "作家名称",
                           duration: Long = 0L,
                           durationUnit: TimeUnit,
                           genre: String= "",
                           title: String= ""
                           , fileUrl:String =""): MediaMetadata{
    return MediaMetadata.Builder()
        .putString(MediaMetadata.METADATA_KEY_MEDIA_URI,fileUrl)
        .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId)
        .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
        .putLong(MediaMetadata.METADATA_KEY_DURATION,
            TimeUnit.MILLISECONDS.convert(duration, durationUnit))
        .putString(MediaMetadata.METADATA_KEY_GENRE, genre)
        .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, album)
        .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, album)
        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
        .putBitmap(METADATA_KEY_ALBUM_ART,album_art)
        .build()
}

fun MediaItem?.getMediaIdString():String{
    return this?.metadata?.mediaId ?:""
}

fun MediaMetadata.toFileItem():UriMediaItem{
    return UriMediaItem.Builder(Uri.parse(getString(MediaMetadata.METADATA_KEY_MEDIA_URI)))
        .setMetadata(this)
        .build()

}

fun MediaMetadata.toUriItem():UriMediaItem{
    return UriMediaItem.Builder(Uri.parse(getString(MediaMetadata.METADATA_KEY_MEDIA_URI)))
        .setMetadata(this)
        .build()
}