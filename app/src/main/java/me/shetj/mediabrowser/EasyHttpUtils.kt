package me.shetj.mediabrowser

import android.app.Application
import androidx.annotation.Keep
import com.zhouyou.http.EasyHttp

@Keep
object EasyHttpUtils {

    fun init(application: Application, isDebug: Boolean, baseUrl: String, version: Int) {
        EasyHttp.init(application)
    }
}