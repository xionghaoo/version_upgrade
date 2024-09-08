import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:version_upgrade/version_upgrade.dart';

void main() {
  runApp(const MaterialApp(home: MyApp(),));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  // final Permission _permission;
  // PermissionStatus _permissionStatus = PermissionStatus.denied;

  final _versionUpgradePlugin = VersionUpgrade();

  _versionUpgrade() async {
    // final status1 = await Permission.storage.request();
    // final status2 = await Permission.manageExternalStorage.request();
    // if (status1.isGranted && status2.isGranted) {
    //   var url = "https://roboland-deliv.ubtrobot.com/apks/cariabot/prod/36/com.ubt.cardplayer_prodRelease_v1.2.11_36_240709.apk";
    //   _versionUpgradePlugin.downloadApk(url, "xh.rabbit.version_upgrade_example");
    //   _showDialog();
    // }

    var url = "https://roboland-deliv.ubtrobot.com/apks/cariabot/prod/36/com.ubt.cardplayer_prodRelease_v1.2.11_36_240709.apk";
    _versionUpgradePlugin.downloadApk(url);
    _showDialog();
  }

  _showDialog() async {
    await showDialog<void>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          content: StreamBuilder(
            stream: _versionUpgradePlugin.progress,
            initialData: 0,
            builder: (ctx, snapshot) {
              return Text("下载进度: ${snapshot.data}");
            },
          ),
        );
      },
    );
  }

  Future<void> requestPermission(Permission permission) async {
    final status = await permission.request();

    // setState(() {
    //   print(status);
    //   _permissionStatus = status;
    //   print(_permissionStatus);
    // });
  }

  // void _listenForPermissionStatus() async {
  //   final status = await _permission.status;
  //   setState(() => _permissionStatus = status);
  // }

  @override
  void initState() {
    super.initState();
    // _listenForPermissionStatus();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // String platformVersion;
    // // Platform messages may fail, so we use a try/catch PlatformException.
    // // We also handle the message potentially returning null.
    // try {
    //   platformVersion =
    //       await _versionUpgradePlugin.getPlatformVersion() ?? 'Unknown platform version';
    // } on PlatformException {
    //   platformVersion = 'Failed to get platform version.';
    // }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      // _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            TextButton(onPressed: _versionUpgrade, child: Text("下载")),
          ],
        ),
      ),
    );
  }
}
