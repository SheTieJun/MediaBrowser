package me.shetj.media.ui

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * SeekBar
 */
class MediaSeekBar : AppCompatSeekBar {
    private var mIsTracking = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun setOnSeekBarChangeListener(listener: OnSeekBarChangeListener) {
        super.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                listener.onProgressChanged(seekBar, progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) { //
                listener.onStartTrackingTouch(seekBar)
                //
                mIsTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) { //
                listener.onStopTrackingTouch(seekBar)
                //
                mIsTracking = false
            }
        })
    }
    // #########################################################################################
    /**
     * 属性动画
     */
    private var mProgressAnimator: ValueAnimator? = null

    /**
     * 开始
     *
     * @param start
     * @param end
     * @param duration
     */
    fun startProgressAnima(start: Int, end: Int, duration: Int) { // 停止播放动画
        stopProgressAnima()
        // 开始播放动画
        mProgressAnimator = ValueAnimator.ofInt(start, end).setDuration(duration.toLong())
        mProgressAnimator!!.interpolator = LinearInterpolator()
        mProgressAnimator!!.addUpdateListener { animation ->
            //
            onProgressUpdate(animation)
        }
        mProgressAnimator!!.start()
    }

    /**
     * 停止播放动画
     */
    fun stopProgressAnima() {
            mProgressAnimator?.cancel()
    }
    // #########################################################################################
    /**
     * 更新进度
     *
     * @param valueAnimator
     */
    fun onProgressUpdate(valueAnimator: ValueAnimator) { // If the user is changing the slider, cancel the animation.
        if (mIsTracking) {
            valueAnimator.cancel()
            return
        }
        // 设置播放进度
        val animatedIntValue = valueAnimator.animatedValue as Int
        // 设置进度
        progress = animatedIntValue
    }
}