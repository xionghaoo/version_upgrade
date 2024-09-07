import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'version_upgrade_platform_interface.dart';

/// An implementation of [VersionUpgradePlatform] that uses method channels.
class MethodChannelVersionUpgrade extends VersionUpgradePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('version_upgrade');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
