package me.shetj.media.callback

import androidx.media2.common.SessionPlayer
import androidx.media2.session.LibraryResult
import androidx.media2.session.MediaLibraryService
import androidx.media2.session.MediaSession

interface OnSubscribeCallBack{

    /**
     * RESULT_SUCCESS 成功
     * RESULT_ERROR_UNKNOWN 失败
     */
    @LibraryResult.ResultCode
    fun  onSubscribe(
        sessionPlayer: SessionPlayer,
        controller: MediaSession.ControllerInfo,
        parentId: String,
        params: MediaLibraryService.LibraryParams?):  Int

}