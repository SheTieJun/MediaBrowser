# MediaBrowser

Android 媒体播放框架MediaSession 的封装使用，简化学习使用成本
快速接入

功能点:
1. 通知栏
2. 自定义加载数据（通过parentID）
3. 可以简单自定义Notification的一些参数



```
    implementation 'androidx.media:media:1.1.0'
```


### [初始化以及开始](./app/src/main/java/me/shetj/mediabrowser/MainActivity.kt) 
```
      MediaBrowserLoader.init() //必须执行
                .addOnMediaStatusListener(this)
                .addMediaLoadDataCallBack("id",object :OnSubscribeCallBack{
                override fun onLoadChildren(parentMediaId: String,
                    result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
                    when(parentMediaId)
                    {
                        "id" ->{
                            MusicUtils.loadFileData(rxContext).map {
                                val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                                it.apply {
                                    forEach {
                                        mediaItems.add(createMediaItemAlbum(it))
                                    }
                                }
                                mediaItems
                            }.subscribe({
                                result.sendResult(it)
                            },{
                                Timber.e(it)
                            })
                        }
                    }
                }
            }).start(this)
```


```
关闭client
 MediaBrowserLoader.stop()
```
[具体方法](./mediaBrowser/src/main/java/me/shetj/media/MediaBrowserLoader.kt)
获取时间
```
long currentPosition = ((SystemClock.elapsedRealtime() – playbackState.getLastPositionUpdateTime() ) * playbackState. getPlaybackSpeed() ) + playbackState.getPosition();
```
### 注意点
如果使用本地文件，并且>= 29
请在AndroidManifest.xml
```
    <application
        ...
        android:requestLegacyExternalStorage="true"
   >     
```