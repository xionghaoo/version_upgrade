package xh.rabbit.version_upgrade

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import android.os.Environment.MEDIA_MOUNTED

/** VersionUpgradePlugin */
class VersionUpgradePlugin: FlutterPlugin,
  MethodCallHandler,
  ActivityAware,
  ContentManager.OnContentDownloadListener,
  EventChannel.StreamHandler
{

  companion object {
    private const val METHOD_DOWNLOAD_APK = "downloadApk"
    private const val METHOD_STOP_DOWNLOAD_APK = "stopDownloadApk"
    private const val METHOD_CALLBACK_ON_PROGRESS = "onProgress"
    const val TAG = "VersionUpgrade"
  }

  private lateinit var channel : MethodChannel
  private lateinit var eventChannel : EventChannel
  private var eventSink: EventChannel.EventSink? = null
  private lateinit var context: Context
  private lateinit var activity: Activity
  private lateinit var contentManager: ContentManager
  private var apkFile: File? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "xh.rabbit/update")
    channel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "xh.rabbit/update_stream")
    eventChannel.setStreamHandler(this)

    context = flutterPluginBinding.applicationContext
    contentManager = ContentManager(context)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {

    when (call.method) {
      METHOD_DOWNLOAD_APK -> {
        val url = call.argument<String>("url")
        if (url == null) {
          result.error("-1", "url is null", null)
          return
        }
        val name = url.split("/").lastOrNull() ?: return
        val saveDir = getSaveDir()
        apkFile = File(saveDir, name)
        contentManager.download(
          urls = arrayListOf(url),
          saveDir = saveDir!!.absolutePath,
          listener = this
        )
      }
      METHOD_STOP_DOWNLOAD_APK -> {
        contentManager.stop()
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onDetachedFromActivity() {

  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  override fun onFinish() {
    if (apkFile?.exists() == true) {
      installApk(context, "${context.packageName}.provider", apkFile!!)
    }
  }

  override fun onProgress(progress: Int, index: Int, total: Int) {
    try {
      eventSink?.success(progress)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun installApk(context: Context, authority: String, apk: File) {
    Log.d(TAG, "installApk start")
    val intent = Intent(Intent.ACTION_VIEW)
    //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
    try {
      val command = arrayOf("chmod", "777", apk.toString())
      val builder = ProcessBuilder(*command)
      builder.start()

      var uri: Uri? = null
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(context, authority, apk)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } else {
        uri = Uri.fromFile(apk)
      }

      intent.setDataAndType(uri, "application/vnd.android.package-archive")
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    } catch (ignored: Exception) {
//      Logger.e(ignored)
      Log.e(TAG, "installApk failure: ${ignored.localizedMessage}")
    }
  }

  private fun getSaveDir(): File? {
    return if (MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(context)) {
      context.externalCacheDir
    } else {
      context.cacheDir
    }
  }

  private fun hasExternalStoragePermission(context: Context): Boolean {
    val perm = context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    return perm == PackageManager.PERMISSION_GRANTED
  }

}
