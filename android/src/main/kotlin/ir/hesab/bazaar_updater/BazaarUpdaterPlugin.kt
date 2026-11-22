package ir.hesab.bazaar_updater

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import ir.cafebazaar.bazaarUpdater.BazaarUpdater
import ir.cafebazaar.bazaarUpdater.model.response.NewVersionState

// NOTE: the import paths for `BazaarUpdater` and `NewVersionState` above are
// my best read of the official doc's usage snippets -- the doc itself never
// shows an `import` line. Once you add the `com.github.cafebazaar:bazaarUpdater`
// dependency, let your IDE's autocomplete / "Fix imports" resolve the real
// package names if these two lines don't match.

class BazaarUpdaterPlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var appContext: Context

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        appContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, "bazaar_updater")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "checkForUpdate" -> checkForUpdate(result)
            "updateApplication" -> {
                BazaarUpdater.updateApplication(context = appContext)
                result.success(null)
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
                else -> "unknown"
            }
            result.success(state)
        }
    }

    /**
     * Only meaningful after checkForUpdate() returned "newVersionDownloaded" --
     * resumes an install Bazaar already downloaded but couldn't auto-install
     * (common on some Xiaomi devices). Must be called while the app is in the
     * foreground; Android 10+ blocks background apps from launching the
     * install activity.
     */
    private fun installDownloadedUpdate(result: Result) {
        BazaarUpdater.installDownloadedUpdate(context = appContext) { installResult ->
            installResult.doOnSuccess {
                result.success(true)
            }.doOnError { error ->
                result.error("INSTALL_FAILED", error.message, null)
            }
        }
    }
}
