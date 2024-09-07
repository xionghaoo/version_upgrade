
import 'version_upgrade_platform_interface.dart';

class VersionUpgrade {
  Future<String?> getPlatformVersion() {
    return VersionUpgradePlatform.instance.getPlatformVersion();
  }
}
