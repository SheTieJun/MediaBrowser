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


#### [如何使用](https://github.com/SheTieJun/MediaBrowser/blob/master/app/src/main/java/me/shetj/mediabrowser/MainActivity.kt)
<img src="https://github.com/SheTieJun/MediaBrowser/blob/master/doc/img/demo.gif" width="32%" height="32%" /><img src="https://github.com/SheTieJun/MediaBrowser/blob/master/doc/img/demo_pic1.jpg" width="32%" height="32%" /><img src="https://github.com/SheTieJun/MediaBrowser/blob/master/doc/img/demo_pic2.jpg" width="32%" height="32%"/>

初始化

```  // onLoadChildren 这个方法会在     MediaBrowserLoader.subscribe(parentId) 之后回调
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
    MediaBrowserLoader.startOrPause()
```

[具体方法](./mediaBrowser/src/main/java/me/shetj/media/MediaBrowserLoader.kt)

获取当前时间，如果是播放中,可以每300ms循环调用一次

比如：
```
Flowable.interval(300, TimeUnit.MILLISECONDS)
                .compose(RxLifecycle.bindUntilEvent(lifecycle(), ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    time.text = playbackStateCompat?.currentPlayBackPosition?.let{MusicUtils.formatTime(it)}
                }.subscribe()
```

获取总时长
```
   override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        metadata?.let {
            Timber.i("onMetadataChanged: 获取总时长  duration = ${ metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)}")
        }
    }
```

### 基础流程（因为可能有人对google的这框架不了解的，所以简单介绍其中主要的流程）

1. 初始化 `MediaBrowserLoader.init() `
2. 订阅流程：`MediaBrowserLoader.subscribe(parentId)` -> `onLoadChildren()` -> `result.sendResult(it)`-> `onChildrenLoaded()`
3. 取消订阅：`MediaBrowserLoader.unSubscribe(parentId)`
4. 如果切换订阅，必须先取消上一个的订阅，再订阅新的
5. 判断是否是否可以订阅 `MediaBrowserLoader.isCanSubscribe()`

### 注意点
如果使用本地文件，并且targetSdkVersion >= 29
请在AndroidManifest.xml
```
    <application
        ...
        android:requestLegacyExternalStorage="true"
   >     
```

如果你要换ICON,可以在app Module,使用相同名称的资源名称，就可以替换了

### 建议拉下来自己在改一波