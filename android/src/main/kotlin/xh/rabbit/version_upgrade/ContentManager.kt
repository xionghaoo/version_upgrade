package xh.rabbit.version_upgrade

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import com.liulishuo.okdownload.core.cause.EndCause
import java.util.ArrayList

class ContentManager(
    private val context: Context
): ContentDownloadService.DownloadCallback {

    companion object {
        private const val TAG = "ContentManager"
    }

    private var mService: ContentDownloadService? = null
    private var mBound: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ContentDownloadService.DownloadBinder
            mService = binder.getService()
            mService?.setDownloadCallback(this@ContentManager)
            mService?.download()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }
    }
    private var listener: OnContentDownloadListener? = null
    private var isFinish: Boolean = false

    private var downloadTotal: Int = 0
    private var downloadCompletedCount: Int = 0
    private var hasConnectionRegister: Boolean = false

    fun download(urls: ArrayList<String>, saveDir: String, listener: OnContentDownloadListener) {
        downloadTotal = urls.size
        this.listener = listener
        isFinish = false
//        if (isShowDialog) showContentDownloadDialog()
        downloadCompletedCount = 0
        hasConnectionRegister = true
        if (mBound) {
            mService?.setData(urls, saveDir)
            mService?.download()
        } else {
            ContentDownloadService.start(context, connection, urls, saveDir)
        }
    }

    fun stop() {
        if (hasConnectionRegister) {
            ContentDownloadService.stop(context, connection)
            hasConnectionRegister = false
        }
    }

    override fun onStart() {

    }

    override fun onProgress(percent: Int, fileName: String?, currentIndex: Int, total: Int) {
        listener?.onProgress(percent, currentIndex + 1, total)
    }

    override fun onFinish() {
        doFinish()
    }

    override fun onEnd(e: EndCause, message: String?) {
        if (e == EndCause.COMPLETED) {
            downloadCompletedCount ++
            if (downloadCompletedCount >= downloadTotal) {
//                dialog?.dismiss()
                doFinish()
            }
        } else {
//            ToastUtil.show(context, message ?: "")
        }
    }

    private fun doFinish() {
        if (!isFinish) {
            listener?.onFinish()
            isFinish = true
        }
    }

    interface OnContentDownloadListener {
        fun onFinish()
        fun onProgress(progress: Int, index: Int, total: Int)
    }
}