import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'bazaar_updater.dart';
import 'bazaar_updater_method_channel.dart';

abstract class BazaarUpdaterPlatform extends PlatformInterface {
  BazaarUpdaterPlatform() : super(token: _token);

  static final Object _token = Object();

  static BazaarUpdaterPlatform _instance = MethodChannelBazaarUpdater();

  static BazaarUpdaterPlatform get instance => _instance;

  static set instance(BazaarUpdaterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<BazaarUpdateState> checkForUpdate() {
    throw UnimplementedError('checkForUpdate() has not been implemented.');
  }

  Future<void> updateApplication() {
    throw UnimplementedError('updateApplication() has not been implemented.');
  }

  Future<void> installDownloadedUpdate() {
    throw UnimplementedError('installDownloadedUpdate() has not been implemented.');
  }
}
