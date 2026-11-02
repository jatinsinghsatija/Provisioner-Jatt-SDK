import 'package:flutter/material.dart';

import 'sdk.dart';
import 'theme.dart';

class StartScreen extends StatelessWidget {
  const StartScreen({super.key, required this.onOpenMain});

  final VoidCallback onOpenMain;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: brandWhite,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const ClipOval(
                child: Image(
                  image: AssetImage('assets/logo_provisioner.png'),
                  width: 88,
                  height: 88,
                  fit: BoxFit.cover,
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'Provisioner Jatt',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: brandBlack,
                ),
              ),
              const SizedBox(height: 28),
              BrandButton(
                key: const Key('start_single_mode'),
                label: 'Start Single Mode',
                onPressed: Sdk.singleMode,
              ),
              const SizedBox(height: 16),
              BrandButton(
                key: const Key('start_single_mode_automation'),
                label: 'Start Single Mode automation',
                onPressed: Sdk.singleModeAutomation,
              ),
              const SizedBox(height: 16),
              BrandButton(
                key: const Key('set_account_frp'),
                label: 'Set account FRP',
                onPressed: Sdk.setAccountFrp,
              ),
              const SizedBox(height: 16),
              BrandButton(
                key: const Key('set_organization_name'),
                label: 'Set organization name',
                onPressed: Sdk.setOrganizationName,
              ),
              const SizedBox(height: 16),
              BrandButton(
                key: const Key('clear_and_open_main'),
                label: 'Clear automation and serial',
                filled: false,
                onPressed: () async {
                  await Sdk.clearAutomationAndSerial();
                  onOpenMain();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
