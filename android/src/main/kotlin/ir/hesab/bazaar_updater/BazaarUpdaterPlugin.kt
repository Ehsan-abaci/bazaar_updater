package ir.hesab.bazaar_updater

import android.app.Activity
import android.content.Context
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.InstallDownloadedUpdateResult
import com.farsitel.bazaar.updater.NewVersionState
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

// Verified against github.com/cafebazaar/BazaarUpdater, tag 1.2.0 (the exact
// version this plugin depends on) -- these are the real package/class names,
// not a guess from the doc's usage snippets like the previous version.
class BazaarUpdaterPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var appContext: Context

    // BazaarUpdater.updateApplication()'s "Bazaar not installed" fallback opens a
    // browser Intent with no FLAG_ACTIVITY_NEW_TASK, which crashes if started from
    // applicationContext. An Activity context is used for that call whenever one is
    // currently attached; applicationContext is only a fallback (used in the
    // version-check call, which never launches UI, so it's always fine there).
    private var activity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        appContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, "bazaar_updater")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "checkForUpdate" -> checkForUpdate(result)
            "updateApplication" -> {
                val current = activity
                if (current == null) {
                    result.error(
                        "NO_ACTIVITY",
                        "updateApplication() needs a foreground activity; call it while a screen of the app is visible.",
                        null,
                    )
                } else {
                    BazaarUpdater.updateApplication(context = current)
                    result.success(null)
                }
            }
            "installDownloadedUpdate" -> installDownloadedUpdate(result)
            else -> result.notImplemented()
        }
    }

    /** Maps Bazaar's own four-state result onto a plain string the Dart side turns into an enum. */
    private fun checkForUpdate(result: Result) {
        BazaarUpdater.getNewVersionState(context = appContext) { versionResult ->
            val state = when (versionResult.getState()) {
                NewVersionState.NEW_VERSION_DOWNLOADED -> "newVersionDownloaded"
                NewVersionState.NEW_VERSION_AVAILABLE -> "newVersionAvailable"
                NewVersionState.NO_NEW_VERSION -> "noNewVersion"
                NewVersionState.UNKNOWN -> "unknown"
            }
            result.success(state)
        }
    }

    /**
     * Only meaningful after checkForUpdate() returned "newVersionDownloaded" --
     * resumes an install Bazaar already downloaded but couldn't auto-install
     * (common on some Xiaomi devices). Must be called while the app is in the
     * foreground; Android 10+ blocks background apps from launching the
     * install activity -- hence the same activity requirement as updateApplication.
     */
    private fun installDownloadedUpdate(result: Result) {
        val current = activity
        if (current == null) {
            result.error(
                "NO_ACTIVITY",
                "installDownloadedUpdate() needs a foreground activity; call it while a screen of the app is visible.",
                null,
            )
            return
        }
        BazaarUpdater.installDownloadedUpdate(context = current) { installResult ->
            when (installResult) {
                is InstallDownloadedUpdateResult.Success -> result.success(true)
                is InstallDownloadedUpdateResult.Error ->
                    result.error("INSTALL_FAILED", installResult.throwable.message, null)
            }
        }
    }
}
