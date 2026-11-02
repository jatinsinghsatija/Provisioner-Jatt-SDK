import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:example_flutter/start_screen.dart';
import 'package:example_flutter/theme.dart';

void main() {
  testWidgets('start screen lists the example actions', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        theme: provisionerTheme(),
        home: StartScreen(onOpenMain: () {}),
      ),
    );

    expect(find.text('Provisioner Jatt'), findsOneWidget);
    expect(find.text('Start Single Mode'), findsOneWidget);
    expect(find.text('Start Single Mode automation'), findsOneWidget);
    expect(find.text('Set account FRP'), findsOneWidget);
    expect(find.text('Set organization name'), findsOneWidget);
    expect(find.text('Clear automation and serial'), findsOneWidget);
  });
}
