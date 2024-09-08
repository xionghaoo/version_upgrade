import 'dart:ffi';

import 'package:flutter/material.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'version_upgrade_method_channel.dart';

abstract class VersionUpgradePlatform extends PlatformInterface {
  /// Constructs a VersionUpgradePlatform.
  VersionUpgradePlatform() : super(token: _token);

  static final Object _token = Object();

  static VersionUpgradePlatform _instance = MethodChannelVersionUpgrade();

  /// The default instance of [VersionUpgradePlatform] to use.
  ///
  /// Defaults to [MethodChannelVersionUpgrade].
  static VersionUpgradePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VersionUpgradePlatform] when
  /// they register themselves.
  static set instance(VersionUpgradePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  abstract Stream<int> progressStream;

  Future<dynamic> downloadApk(String url) {
    throw UnimplementedError('downloadApk() has not been implemented.');
  }
}
