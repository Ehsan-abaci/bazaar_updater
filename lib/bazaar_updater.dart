import 'bazaar_updater_platform_interface.dart';

/// Mirrors Cafe Bazaar's own `NewVersionState` from the BazaarUpdater SDK.
///
/// Note: Bazaar's SDK has no concept of a *mandatory* update -- the FAQ in
/// their docs says so explicitly. If you need to force an update, that
/// decision still has to come from your own backend; this only tells you
/// whether Bazaar has something newer to offer.
enum BazaarUpdateState {
  /// A newer build was already downloaded by Bazaar; only installing is left.
  /// Requires Bazaar app >= 30.1.0 to be reported at all -- older Bazaar
  /// versions collapse this into [newVersionAvailable] instead.
  newVersionDownloaded,

  /// A newer build is published but not yet downloaded.
  newVersionAvailable,

  /// The installed build is already the latest Bazaar knows about.
  noNewVersion,

  /// Bazaar isn't installed, the check failed, or the result couldn't be
  /// read. Treat this the same as "can't confirm" -- never as "no update".
  unknown;

  static BazaarUpdateState fromWire(String? value) {
    switch (value) {
      case 'newVersionDownloaded':
        return BazaarUpdateState.newVersionDownloaded;
      case 'newVersionAvailable':
        return BazaarUpdateState.newVersionAvailable;
      case 'noNewVersion':
        return BazaarUpdateState.noNewVersion;
      default:
        return BazaarUpdateState.unknown;
    }
  }
}

/// Thin wrapper around Cafe Bazaar's official `BazaarUpdater` SDK.
class BazaarUpdater {
  /// Asks Bazaar whether a newer build of this app is published.
  ///
  /// Requires this build's package name and signing certificate to match
  /// the one published on Bazaar -- a debug-signed build checking against
  /// a release-signed Bazaar listing will always report [BazaarUpdateState.unknown]
  /// or [BazaarUpdateState.noNewVersion], never a real match.
  static Future<BazaarUpdateState> checkForUpdate() {
    return BazaarUpdaterPlatform.instance.checkForUpdate();
  }

  /// Starts Bazaar's in-app download+install flow for the new version.
  /// Call this after [checkForUpdate] reports [BazaarUpdateState.newVersionAvailable]
  /// (or [BazaarUpdateState.newVersionDownloaded]).
  static Future<void> updateApplication() {
    return BazaarUpdaterPlatform.instance.updateApplication();
  }

  /// Resumes installing a build Bazaar already downloaded but couldn't
  /// auto-install (seen on some Xiaomi devices). Only call this after
  /// [checkForUpdate] reports [BazaarUpdateState.newVersionDownloaded], and
  /// only while the app is in the foreground.
  static Future<void> installDownloadedUpdate() {
    return BazaarUpdaterPlatform.instance.installDownloadedUpdate();
  }
}
