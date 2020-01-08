# MediaBrowser

Android 媒体播放框架MediaSession 的封装使用

- 快速接入


```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        }
}
```

Step 2. Add the dependency

[![](https://jitpack.io/v/SheTieJun/MediaBrowser.svg)](https://jitpack.io/#SheTieJun/MediaBrowser)
```
dependencies {
  implementation 'androidx.media:media:1.1.0'
  implementation 'com.github.SheTieJun:MediaBrowser:+'
}
```


#### [如何使用](./app/src/main/java/me/shetj/mediabrowser/MainActivity.kt) 
初始化
```
      MediaBrowserLoader.init() //必须执行
                .addOnMediaStatusListener(this)
                .addMediaLoadDataCallBack("local_music",object :OnSubscribeCallBack{
                override fun onLoadChildren(parentMediaId: String,
                    result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
                    when(parentMediaId)
                    {
                        "local_music" ->{
                        //获取资源的方式
                            MusicUtils.loadFileData(rxContext).map {
                                val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                                it.apply {
                                    forEach {
                                        mediaItems.add(createMediaItemAlbum(it))
                                    }
                                }
                                mediaItems
                            }.subscribe({
                            //告诉客户端获取的资源
                                result.sendResult(it)
                            },{
                             result.sendResult(null)
                                Timber.e(it)
                            })
                        }
                    }
                }
            })
```
关闭client
```
 MediaBrowserLoader.stop()
```

客户端绑定生命周期
```
   override fun onResume() {
        super.onResume()
        MediaBrowserLoader.start(this)
    }

    override fun onStop() {
        super.onStop()
        MediaBrowserLoader.stop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        MediaBrowserLoader.removeOnMediaStatusListener(this)
    }
```
控制播放
```
    private fun startOrPause() {
        when(MediaBrowserLoader.getMediaController()?.playbackState?.state){
            PlaybackStateCompat.STATE_PLAYING ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.pause()
            }
            PlaybackStateCompat.STATE_PAUSED ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.play()
            }
            else ->{
                MediaBrowserLoader.getMediaController()?.transportControls?.play()
            }
        }
    }
```

[具体方法](./mediaBrowser/src/main/java/me/shetj/media/MediaBrowserLoader.kt)
获取时间
```
long currentPosition = ((SystemClock.elapsedRealtime() – playbackState.getLastPositionUpdateTime() ) * playbackState. getPlaybackSpeed() ) + playbackState.getPosition();
```
### 注意点
如果使用本地文件，并且targetSdkVersion >= 29
请在AndroidManifest.xml
```
    <application
        ...
        android:requestLegacyExternalStorage="true"
   >     
```