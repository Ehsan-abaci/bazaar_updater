import 'package:flutter/services.dart';

import 'bazaar_updater.dart';
import 'bazaar_updater_platform_interface.dart';

class MethodChannelBazaarUpdater extends BazaarUpdaterPlatform {
  final MethodChannel _channel = const MethodChannel('bazaar_updater');

  @override
  Future<BazaarUpdateState> checkForUpdate() async {
    try {
      final raw = await _channel.invokeMethod<String>('checkForUpdate');
      return BazaarUpdateState.fromWire(raw);
    } on PlatformException {
      return BazaarUpdateState.unknown;
    }
  }

  @override
  Future<void> updateApplication() {
    return _channel.invokeMethod('updateApplication');
  }

  @override
  Future<void> installDownloadedUpdate() {
    return _channel.invokeMethod('installDownloadedUpdate');
  }
}
