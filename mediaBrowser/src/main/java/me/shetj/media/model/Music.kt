package me.shetj.media.model

import androidx.annotation.Keep

@Keep
internal  class Music {
    var name: String ?=null
    var size: Long = 0
    var url: String?=null
    var duration: Long = 0
    var img :String ?=null
}
