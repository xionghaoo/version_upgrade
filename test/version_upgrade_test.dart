import 'package:flutter_test/flutter_test.dart';
import 'package:version_upgrade/version_upgrade.dart';
import 'package:version_upgrade/version_upgrade_platform_interface.dart';
import 'package:version_upgrade/version_upgrade_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVersionUpgradePlatform
    with MockPlatformInterfaceMixin
    implements VersionUpgradePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final VersionUpgradePlatform initialPlatform = VersionUpgradePlatform.instance;

  test('$MethodChannelVersionUpgrade is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelVersionUpgrade>());
  });

  test('getPlatformVersion', () async {
    VersionUpgrade versionUpgradePlugin = VersionUpgrade();
    MockVersionUpgradePlatform fakePlatform = MockVersionUpgradePlatform();
    VersionUpgradePlatform.instance = fakePlatform;

    expect(await versionUpgradePlugin.getPlatformVersion(), '42');
  });
}
