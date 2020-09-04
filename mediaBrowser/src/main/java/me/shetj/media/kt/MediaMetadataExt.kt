package me.shetj.media.kt

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.os.ParcelFileDescriptor.MODE_READ_WRITE
import androidx.media2.common.FileMediaItem
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.UriMediaItem
import java.io.File
import java.util.concurrent.TimeUnit

fun MediaMetadata.toMediaItem():MediaItem{


   return MediaItem.Builder()
        .setMetadata(this)
        .build()
}

fun getMediaMetadataCompat(mediaId: String = "",
                           album: String = "https://www.google.com/url?sa=i&url=http%3A%2F%2Fpic.netbian.com%2Ftupian%2F4780.html&psig=AOvVaw3XAWdpJgCAlXCkk5VdUNa-&ust=1599277872026000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCMDRz7zMzusCFQAAAAAdAAAAABAN",
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