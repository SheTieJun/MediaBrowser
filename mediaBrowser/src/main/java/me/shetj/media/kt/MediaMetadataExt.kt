package me.shetj.media.kt

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import androidx.media2.common.FileMediaItem
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.MediaMetadata.METADATA_KEY_MEDIA_URI
import androidx.media2.common.UriMediaItem
import java.io.File
import java.util.concurrent.TimeUnit

fun MediaMetadata.toMediaItem():MediaItem{


   return MediaItem.Builder()
        .setMetadata(this)
        .build()
}

fun getMediaMetadataCompat(mediaId: String = "",
                           album: String = "",
                           artist: String = "",
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
        .build()
}

fun MediaItem?.getMediaIdString():String{
    return this?.metadata?.mediaId ?:""
}

fun MediaMetadata.toFileItem():FileMediaItem{

    return FileMediaItem.Builder(ParcelFileDescriptor.open(File(getString(MediaMetadata.METADATA_KEY_MEDIA_URI)),MODE_READ_ONLY))
        .setMetadata(this).build()

}

fun MediaMetadata.toUriItem():UriMediaItem{
    return UriMediaItem.Builder(Uri.parse(getString(MediaMetadata.METADATA_KEY_MEDIA_URI)))
        .setMetadata(this).build()
}