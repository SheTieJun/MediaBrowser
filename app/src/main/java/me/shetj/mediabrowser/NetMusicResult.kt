package me.shetj.mediabrowser

import androidx.annotation.Keep

/**
 * **@author：** shetj<br></br>
 * **@createTime：** 2020/4/12 0012<br></br>
 * **@email：** 375105540@qq.com<br></br>
 * **@describe**  <br></br>
 */
@Keep
class NetMusicResult {
    var msg: String? = null
    var code = 0
    var data: List<NetMusic>? = null
}