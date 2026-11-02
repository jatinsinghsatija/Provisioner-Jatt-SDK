import 'package:flutter/material.dart';

import 'provisioner_screen.dart';
import 'sdk.dart';
import 'start_screen.dart';
import 'theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProvisionerApp());
}

class ProvisionerApp extends StatefulWidget {
  const ProvisionerApp({super.key});

  @override
  State<ProvisionerApp> createState() => _ProvisionerAppState();
}

class _ProvisionerAppState extends State<ProvisionerApp> {
  final _snapshot = ValueNotifier(ProvisionerSnapshot.empty);

  @override
  void initState() {
    super.initState();
    stateChannel.receiveBroadcastStream().listen((event) {
      _snapshot.value = ProvisionerSnapshot.from(event);
    });
  }

  @override
  void dispose() {
    _snapshot.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Provisioner Jatt',
      debugShowCheckedModeBanner: false,
      theme: provisionerTheme(),
      home: Builder(
        builder: (navContext) => StartScreen(
          onOpenMain: () {
            Navigator.of(navContext).push(
              MaterialPageRoute<void>(
                builder: (_) => ValueListenableBuilder<ProvisionerSnapshot>(
                  valueListenable: _snapshot,
                  builder: (_, snapshot, __) => ProvisionerScreen(snapshot: snapshot),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
