package me.shetj.mediabrowser

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.media.MediaBrowserCompat

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import timber.log.Timber

class MusicAdapter(data: MutableList<MediaBrowserCompat.MediaItem>) : BaseQuickAdapter<MediaBrowserCompat.MediaItem, BaseViewHolder>(R.layout.item_select_music2, data) {

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
                .setTextColor(R.id.tv_music_name,when(itemPosition == position){
                    true ->  Color.RED
                    false -> Color.BLACK
                } )
        addChildClickViewIds(R.id.tv_play)
    }

    /**
     * 设置选中的位置
     */
    fun setSelectPosition(targetPos: Int) {
        Timber.i("setSelectPosition=$targetPos")
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