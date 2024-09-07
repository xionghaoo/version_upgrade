package xh.rabbit.version_upgrade

import android.util.Log
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.Util
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import java.lang.Exception

abstract class ContentDownloadListener: DownloadListener {
    companion object {
        const val TAG = "ContentDownloadListener"
    }
    override fun taskStart(task: DownloadTask) {
        Log.d(TAG,"taskStart")
    }

    override fun connectTrialStart(
        task: DownloadTask,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        Log.d(TAG,"connectTrialStart")
    }

    override fun connectTrialEnd(
        task: DownloadTask,
        responseCode: Int,
        responseHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        Log.d(TAG,"connectTrialEnd")
    }

    override fun downloadFromBeginning(
        task: DownloadTask,
        info: BreakpointInfo,
        cause: ResumeFailedCause
    ) {
        Log.d(TAG,"downloadFromBeginning")
    }

    override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
        Log.d(TAG,"downloadFromBreakpoint")
    }

    override fun connectStart(
        task: DownloadTask,
        blockIndex: Int,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        Log.d(TAG,"connectStart")
    }

    override fun connectEnd(
        task: DownloadTask,
        blockIndex: Int,
        responseCode: Int,
        responseHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        Log.d(TAG,"connectEnd")
    }

}