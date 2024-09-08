import 'package:flutter/material.dart';

import 'version_upgrade_platform_interface.dart';

class VersionUpgrade {

  Stream<int> get progress => VersionUpgradePlatform.instance.progressStream;

  Future<dynamic> downloadApk(String url, String packageName) =>
      VersionUpgradePlatform.instance.downloadApk(url, packageName);
}
