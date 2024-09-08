import 'dart:ffi';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'version_upgrade_platform_interface.dart';

/// An implementation of [VersionUpgradePlatform] that uses method channels.
class MethodChannelVersionUpgrade extends VersionUpgradePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('xh.rabbit/update');
  final EventChannel _eventChannel = const EventChannel("xh.rabbit/update_stream");
  late Stream<int> _updateStream = _eventChannel.receiveBroadcastStream().map((dynamic data) => data);

  @override
  Future<dynamic> downloadApk(String url, String packageName) async {
    return await methodChannel.invokeListMethod<String>('downloadApk', {
      "url": url,
      "packageName": packageName
    });
  }

  @override
  Stream<int> get progressStream => _updateStream;

  @override
  set progressStream(Stream<int> stream) {
    _updateStream = stream;
  }




}
