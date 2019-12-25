package me.shetj.mediabrowser

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.media.MediaBrowserCompat

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class MusicAdapter(data: List<MediaBrowserCompat.MediaItem>?) : BaseQuickAdapter<MediaBrowserCompat.MediaItem, BaseViewHolder>(R.layout.item_select_music2, data) {

    private var position :Int = -1

    /******************** 存储相关常量  */
    /**
     * KB与Byte的倍数
     */
    val KB = 1024
    /**
     * MB与Byte的倍数
     */
    val MB = 1048576
    /**
     * GB与Byte的倍数
     */
    val GB = 1073741824



    override fun convert(helper: BaseViewHolder, item: MediaBrowserCompat.MediaItem) {
        val itemPosition = helper.adapterPosition - headerLayoutCount
        helper.setText(R.id.tv_music_name, item.description.title)
                .addOnClickListener(R.id.tv_play)
                .setTextColor(R.id.tv_music_name,when(itemPosition == position){
                    true ->  Color.RED
                    false -> Color.BLACK
                } )
    }

    /**
     * 设置选中的位置
     */
    fun setSelectPosition(targetPos: Int) {
        //如果不相等，说明有变化
        if (position != targetPos) {
            val old: Int = position
            this.position = targetPos
            if (old != -1) {
                notifyItemChanged(old + headerLayoutCount)
            }
            if (targetPos != -1) {
                notifyItemChanged(targetPos + headerLayoutCount)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun byte2FitMemorySize(byteNum: Long): String {
        return if (byteNum < 0) {
            "shouldn't be less than zero!"
        } else if (byteNum < KB) {
            String.format("%.2fB", byteNum + 0.005)
        } else if (byteNum < MB) {
            String.format("%.2fKB", byteNum / KB + 0.005)
        } else if (byteNum < GB) {
            String.format("%.2fMB", byteNum / MB + 0.005)
        } else {
            String.format("%.2fGB", byteNum / GB + 0.005)
        }
    }

    fun formatTime(ms: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        //
        val day = ms / dd
        val hour = (ms - day * dd) / hh
        val minute = (ms - day * dd - hour * hh) / mi
        val second = (ms - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss
        //
        val strDay = if (day < 10) "0$day" else "" + day
        //        //天
        val strHour = if (hour < 10) "0$hour" else "" + hour
        //        //小时
        val strMinute = if (minute < 10) "0$minute" else "" + minute
        //        //分钟
        val strSecond = if (second < 10) "0$second" else "" + second
        //        //秒
        var strMilliSecond = if (milliSecond < 10) "0$milliSecond" else "" + milliSecond
        //
        return "$strMinute：$strSecond"
    }

    fun selectMediaId(mediaId: String?) {
        mediaId?.let {
            data.forEach {
                if(it.description.mediaId == mediaId){
                    setSelectPosition(data.indexOf(it))
                }
            }
        }
    }


}