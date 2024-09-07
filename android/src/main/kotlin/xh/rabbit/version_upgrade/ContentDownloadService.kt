package xh.rabbit.version_upgrade

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.DownloadContextListener
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.Util
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.math.roundToInt

class ContentDownloadService : Service() {

    companion object {
        private const val TAG = "ContentDownloadService"

        private const val CHANNEL_ID = "content_download_channel"
        private const val EXTRA_URLS = "xh.rabbit.download.ContentDownloadService.EXTRA_URLS"
        private const val EXTRA_SAVE_DIR = "xh.rabbit.download.ContentDownloadService.EXTRA_SAVE_DIR"
        private const val NOTIFICATION_ID = 3

        fun start(context: Context, conn: ServiceConnection, urls: ArrayList<String>, saveDir: String) {
            val intent = Intent(context, ContentDownloadService::class.java)
            intent.putExtra(EXTRA_URLS, urls)
            intent.putExtra(EXTRA_SAVE_DIR, saveDir)
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "start service")
        }

        fun stop(context: Context, conn: ServiceConnection) {
            context.unbindService(conn)
        }
    }

    private val binder = DownloadBinder()
    private var mNotifyManager: NotificationManager? = null
    private lateinit var mBuilder: NotificationCompat.Builder
    private var urls: ArrayList<String>? = null
    private var saveDir: String? = null

    private var callback: DownloadCallback? = null

    private var downloadContext: DownloadContext? = null
    private val downloadListener = object : ContentDownloadListener() {
        override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            callback?.onStart()
            mNotifyManager?.notify(NOTIFICATION_ID, mBuilder.build())
        }

        override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
            val totalLength = task.info?.totalLength ?: 0
            val readableTotalLength = Util.humanReadableBytes(totalLength, true)
            val percent: Int = ((task.info?.totalOffset?.toFloat() ?: 0f) / totalLength.toFloat() * 100f).roundToInt()
            Log.d(TAG, "percent: $percent%, ${task.info?.totalOffset}, ${totalLength}, $readableTotalLength")
            Log.d(TAG, "fetchProgress: ${task.id}")
            callback?.onProgress(percent, task.filename, currentCount, totalCount)
            mNotifyManager?.notify(NOTIFICATION_ID, mBuilder.build())
        }

        override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            currentCount ++
            callback?.onProgress(100, task.filename, currentCount, totalCount)
            Log.d(TAG, "fetchEnd: $currentCount / $totalCount")
            if (currentCount >= totalCount) {
                callback?.onFinish()
            }
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
            Log.e(TAG,"task ${task.getTag(1)}, ${cause}, End: $realCause")
            callback?.onEnd(cause, realCause?.toString())
        }
    }
    private var totalCount: Int = 0
    private var currentCount: Int = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        urls = intent.getStringArrayListExtra(EXTRA_URLS)
        saveDir = intent.getStringExtra(EXTRA_SAVE_DIR)
        initialNotification()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        downloadContext?.stop()
        return super.onUnbind(intent)
    }

    private fun initialNotification() {
        mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        val appName = getString(applicationInfo.labelRes)
        val icon = applicationInfo.icon

        mBuilder.setContentTitle(appName)
            .setSmallIcon(icon)
            .setOnlyAlertOnce(true)
            .priority = NotificationCompat.PRIORITY_LOW

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "content download task"
            val description = "content download task"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description
            // Register the channel with the system
            mNotifyManager?.createNotificationChannel(channel)
        }
    }

    fun setDownloadCallback(cb: DownloadCallback) {
        callback = cb
    }

    fun setData(urls: ArrayList<String>?, saveDir: String?) {
        this.urls = urls
        this.saveDir = saveDir
    }

    fun download() {
        Log.d(TAG, "download: $urls")
        if (urls == null || saveDir == null) return
        val parentFile = File(saveDir!!)
        // 下载到内部文件夹
        val builder: DownloadContext.Builder = DownloadContext.QueueSet()
            .setParentPathFile(parentFile)
            .setWifiRequired(true)
            .setMinIntervalMillisCallbackProcess(50)
            .commit()
        urls?.forEachIndexed { index, url ->
            val task = DownloadTask.Builder(url, parentFile)
                .setConnectionCount(1)
                .build()
                .addTag(1, index)
            builder.bindSetTask(task)
        }
        totalCount = urls!!.size
        currentCount = 0
        downloadContext = builder.build()
        downloadContext?.startOnSerial(downloadListener)
    }

    inner class DownloadBinder : Binder() {
        fun getService() : ContentDownloadService = this@ContentDownloadService
    }

    interface DownloadCallback {
        fun onStart()
        fun onProgress(percent: Int, fileName: String?, currentIndex: Int, total: Int)
        fun onFinish()
        fun onEnd(e: EndCause, message: String?)
    }
}