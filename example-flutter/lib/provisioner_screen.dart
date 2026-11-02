import 'package:flutter/material.dart';

import 'sdk.dart';
import 'theme.dart';

class ProvisionerScreen extends StatefulWidget {
  const ProvisionerScreen({super.key, required this.snapshot});

  final ProvisionerSnapshot snapshot;

  @override
  State<ProvisionerScreen> createState() => _ProvisionerScreenState();
}

class _ProvisionerScreenState extends State<ProvisionerScreen> {
  var _discoveredTab = false;

  @override
  void initState() {
    super.initState();
    Sdk.attach();
  }

  @override
  Widget build(BuildContext context) {
    final wide = MediaQuery.sizeOf(context).width >= 600;
    final discovered = widget.snapshot.devices.where((device) => !device.ready).toList();
    final connected = widget.snapshot.devices.where((device) => device.ready).toList();
    return PopScope(
      onPopInvokedWithResult: (didPop, _) {
        if (didPop) Sdk.detach();
      },
      child: Scaffold(
        backgroundColor: brandWhite,
        appBar: AppBar(
          backgroundColor: brandWhite,
          foregroundColor: brandBlack,
          elevation: 2,
          title: const Row(
            children: [
              ClipOval(
                child: Image(
                  image: AssetImage('assets/logo_provisioner.png'),
                  width: 32,
                  height: 32,
                  fit: BoxFit.cover,
                ),
              ),
              SizedBox(width: 10),
              Text(
                'Provisioner Jatt',
                style: TextStyle(fontWeight: FontWeight.bold, color: brandBlack),
              ),
            ],
          ),
          actions: [
            IconButton(
              key: const Key('open_qr'),
              tooltip: 'QR',
              onPressed: Sdk.openQr,
              icon: const Icon(Icons.qr_code, color: brandOrange),
            ),
            IconButton(
              key: const Key('open_pairing'),
              tooltip: 'Pairing code',
              onPressed: Sdk.openPairing,
              icon: const Icon(Icons.password, color: brandOrange),
            ),
            TextButton(
              key: const Key('automate_provisioning'),
              onPressed: () => _openAutomation(context),
              child: const Text(
                'Automate provisioning',
                style: TextStyle(color: brandOrange),
              ),
            ),
          ],
        ),
        body: wide
            ? _WideLists(
                snapshot: widget.snapshot,
                discovered: discovered,
                connected: connected,
              )
            : ListView(
                key: const Key('provisioner_screen'),
                padding: const EdgeInsets.fromLTRB(18, 18, 18, 18),
                children: [
                  _Tabs(
                    discovered: _discoveredTab,
                    discoveredCount: discovered.length,
                    connectedCount: connected.length,
                    onSelect: (discovered) => setState(() => _discoveredTab = discovered),
                  ),
                  const SizedBox(height: 18),
                  if (widget.snapshot.settings.hasSerialFilter) ...[
                    _SerialBanner(settings: widget.snapshot.settings),
                    const SizedBox(height: 18),
                  ],
                  _DeviceList(
                    kind: _discoveredTab ? DeviceListKind.discovered : DeviceListKind.connected,
                    devices: _discoveredTab ? discovered : connected,
                    scanning: widget.snapshot.scanning,
                    automationConfigured: widget.snapshot.settings.automationConfigured,
                  ),
                ],
              ),
      ),
    );
  }

  Future<void> _openAutomation(BuildContext context) async {
    final saved = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      backgroundColor: brandWhite,
      builder: (context) => _AutomationSheet(settings: widget.snapshot.settings),
    );
    if (saved == true && context.mounted) {
      FocusScope.of(context).unfocus();
    }
  }
}

enum DeviceListKind { discovered, connected }

class _WideLists extends StatelessWidget {
  const _WideLists({
    required this.snapshot,
    required this.discovered,
    required this.connected,
  });

  final ProvisionerSnapshot snapshot;
  final List<DeviceItem> discovered;
  final List<DeviceItem> connected;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(18),
      child: Column(
        children: [
          if (snapshot.settings.hasSerialFilter) ...[
            _SerialBanner(settings: snapshot.settings),
            const SizedBox(height: 16),
          ],
          Expanded(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: _Pane(
                    title: 'Discovered',
                    count: discovered.length,
                    kind: DeviceListKind.discovered,
                    devices: discovered,
                    scanning: snapshot.scanning,
                    automationConfigured: snapshot.settings.automationConfigured,
                  ),
                ),
                const SizedBox(width: 24),
                Expanded(
                  child: _Pane(
                    title: 'Connected',
                    count: connected.length,
                    kind: DeviceListKind.connected,
                    devices: connected,
                    scanning: snapshot.scanning,
                    automationConfigured: snapshot.settings.automationConfigured,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Pane extends StatelessWidget {
  const _Pane({
    required this.title,
    required this.count,
    required this.kind,
    required this.devices,
    required this.scanning,
    required this.automationConfigured,
  });

  final String title;
  final int count;
  final DeviceListKind kind;
  final List<DeviceItem> devices;
  final bool scanning;
  final bool automationConfigured;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '$title  $count',
          style: const TextStyle(
            color: brandOrange,
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 14),
        Expanded(
          child: ListView(
            children: [
              _DeviceList(
                kind: kind,
                devices: devices,
                scanning: scanning,
                automationConfigured: automationConfigured,
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _Tabs extends StatelessWidget {
  const _Tabs({
    required this.discovered,
    required this.discoveredCount,
    required this.connectedCount,
    required this.onSelect,
  });

  final bool discovered;
  final int discoveredCount;
  final int connectedCount;
  final ValueChanged<bool> onSelect;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: surfacePressed,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          _tab('Discovered', discoveredCount, true, const Key('tab_discovered')),
          const SizedBox(width: 6),
          _tab('Connected', connectedCount, false, const Key('tab_connected')),
        ],
      ),
    );
  }

  Widget _tab(String label, int count, bool isDiscovered, Key key) {
    final selected = discovered == isDiscovered;
    return Expanded(
      child: Material(
        color: selected ? brandWhite : Colors.transparent,
        borderRadius: BorderRadius.circular(14),
        child: InkWell(
          key: key,
          borderRadius: BorderRadius.circular(14),
          onTap: () => onSelect(isDiscovered),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
            child: Text(
              '$label  $count',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: selected ? brandOrange : textSecondary,
                fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _SerialBanner extends StatelessWidget {
  const _SerialBanner({required this.settings});

  final ProvisionerSettings settings;

  @override
  Widget build(BuildContext context) {
    return Container(
      key: const Key('serial_lock_banner'),
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: brandWhite,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: brandOrange),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Serial lock  ${settings.serialNumber}',
            style: const TextStyle(
              color: brandOrange,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 4),
          const Text(
            'Only this device can be discovered, paired, and connected. After pairing and authorization, the SDK connected-device dialog tracks provisioning or DPC actions.',
            style: TextStyle(color: textSecondary, fontSize: 12),
          ),
        ],
      ),
    );
  }
}

class _DeviceList extends StatelessWidget {
  const _DeviceList({
    required this.kind,
    required this.devices,
    required this.scanning,
    required this.automationConfigured,
  });

  final DeviceListKind kind;
  final List<DeviceItem> devices;
  final bool scanning;
  final bool automationConfigured;

  @override
  Widget build(BuildContext context) {
    if (devices.isEmpty) return _Empty(kind: kind, scanning: scanning);
    return Column(
      children: [
        for (final device in devices) ...[
          _DeviceCard(
            kind: kind,
            device: device,
            automationConfigured: automationConfigured,
          ),
          const SizedBox(height: 12),
        ],
      ],
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty({required this.kind, required this.scanning});

  final DeviceListKind kind;
  final bool scanning;

  @override
  Widget build(BuildContext context) {
    final connected = kind == DeviceListKind.connected;
    final title = connected
        ? 'No connected devices'
        : scanning
            ? 'Scanning for devices…'
            : 'No discovered devices';
    final body = connected
        ? 'USB devices connect after permission. Wireless devices show up here after pairing.'
        : 'Connect a phone over USB, or enable Wireless debugging. Pairing is only asked for devices that are not already connected.';
    return Container(
      key: Key(connected ? 'connected_empty' : 'devices_empty'),
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: brandWhite,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: outline),
      ),
      child: Column(
        children: [
          if (scanning) const CircularProgressIndicator(color: brandOrange),
          if (scanning) const SizedBox(height: 12),
          Text(title, style: const TextStyle(fontSize: 18, color: brandBlack)),
          const SizedBox(height: 8),
          Text(body, textAlign: TextAlign.center, style: const TextStyle(color: textSecondary)),
        ],
      ),
    );
  }
}

class _DeviceCard extends StatelessWidget {
  const _DeviceCard({
    required this.kind,
    required this.device,
    required this.automationConfigured,
  });

  final DeviceListKind kind;
  final DeviceItem device;
  final bool automationConfigured;

  @override
  Widget build(BuildContext context) {
    final wirelessReady = kind == DeviceListKind.connected && device.wireless && device.ready;
    return Container(
      key: Key('device_${device.id}'),
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: brandWhite,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: outline),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(device.name, style: const TextStyle(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 4),
                    Text(
                      [
                        device.wireless ? 'Wireless' : 'USB',
                        if (device.serial != null) device.serial,
                        _connectionLabel(device.connection),
                      ].join(' • '),
                      style: const TextStyle(fontSize: 12, color: textSecondary),
                    ),
                  ],
                ),
              ),
              if (device.connection == 'CONNECTING')
                const SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2, color: brandOrange),
                ),
              if (wirelessReady)
                TextButton(
                  key: Key('disconnect_${device.id}'),
                  onPressed: () => Sdk.disconnectWireless(device.id),
                  child: const Text('Disconnect', style: TextStyle(color: brandOrange)),
                ),
            ],
          ),
          if (_statusText(device) != null) ...[
            const SizedBox(height: 10),
            Text(_statusText(device)!),
          ],
          if (device.message != null) ...[
            const SizedBox(height: 10),
            _Result(device.message!),
          ],
          if (kind == DeviceListKind.connected && device.ready) ...[
            const SizedBox(height: 10),
            const Divider(height: 1),
            const SizedBox(height: 10),
            if (device.stageKind == 'ownerRemoved' || automationConfigured)
              _StageBlock(device: device)
            else
              _ManualBlock(device: device),
          ],
        ],
      ),
    );
  }
}

class _ManualBlock extends StatelessWidget {
  const _ManualBlock({required this.device});

  final DeviceItem device;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Expanded(
              child: Text('DPC components', style: TextStyle(fontWeight: FontWeight.w600)),
            ),
            TextButton(
              onPressed: device.manualScanning ? null : () => Sdk.scan(device.id),
              child: const Text('Scan', style: TextStyle(color: brandOrange)),
            ),
          ],
        ),
        if (device.manualScanning)
          const Row(
            children: [
              SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(strokeWidth: 2, color: brandOrange),
              ),
              SizedBox(width: 8),
              Text('Scanning device policy components…'),
            ],
          ),
        for (final component in device.components) ...[
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: outline),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(component.flattened, style: const TextStyle(fontFamily: 'monospace')),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  children: [
                    BrandButton(
                      label: 'Make Device Owner',
                      onPressed: device.manualBusy
                          ? null
                          : () => _confirm(context, component, makeOwner: true),
                    ),
                    if (component.testOnly)
                      BrandButton(
                        key: Key('remove_owner_${component.flattened}'),
                        label: 'Remove Owner',
                        filled: false,
                        onPressed: device.manualBusy
                            ? null
                            : () => _confirm(context, component, makeOwner: false),
                      ),
                  ],
                ),
                if (!component.testOnly) ...[
                  const SizedBox(height: 8),
                  const Text(
                    'Remove Owner is hidden because this package is not test-only. Android will reject dpm remove-active-admin.',
                    style: TextStyle(fontSize: 12, color: textSecondary),
                  ),
                ],
              ],
            ),
          ),
        ],
        if (device.manualBusy) ...[
          const SizedBox(height: 8),
          const Row(
            children: [
              SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(strokeWidth: 2, color: brandOrange),
              ),
              SizedBox(width: 8),
              Text('Applying command…'),
            ],
          ),
        ],
        if (device.manualResult != null) ...[
          const SizedBox(height: 8),
          _Result(device.manualResult!),
        ],
      ],
    );
  }

  Future<void> _confirm(
    BuildContext context,
    DpcComponent component, {
    required bool makeOwner,
  }) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: brandWhite,
        title: Text(makeOwner ? 'Make Device Owner?' : 'Remove Owner?'),
        content: Text(
          makeOwner
              ? 'Set ${component.flattened}? This changes device administration state.'
              : 'Remove ${component.flattened}? This changes device administration state.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel', style: TextStyle(color: brandOrange)),
          ),
          BrandButton(
            label: 'Confirm',
            onPressed: () => Navigator.pop(context, true),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    if (makeOwner) {
      await Sdk.makeOwner(device.id, component);
    } else {
      await Sdk.removeOwner(device.id, component);
    }
  }
}

class _StageBlock extends StatelessWidget {
  const _StageBlock({required this.device});

  final DeviceItem device;

  @override
  Widget build(BuildContext context) {
    if (device.stageKind == 'ownerRemoved') {
      return Column(
        key: const Key('provision_removed'),
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const _Result('Provision Removed'),
          const SizedBox(height: 8),
          Text(
            'Device owner ${device.stageFlattened ?? ''} is no longer set. Redo the full provisioning process.',
            style: const TextStyle(fontSize: 12, color: textSecondary),
          ),
          const SizedBox(height: 10),
          BrandButton(
            key: const Key('redo_provisioning'),
            label: 'Redo',
            onPressed: () => Sdk.retryProvisioning(device.id),
          ),
        ],
      );
    }
    final working = const {
      'authenticating',
      'scanning',
      'downloading',
      'pushing',
      'installing',
      'updating',
      'settingOwner',
      'launching',
      'retrying',
    }.contains(device.stageKind);
    return Row(
      children: [
        if (working) ...[
          const SizedBox(
            width: 18,
            height: 18,
            child: CircularProgressIndicator(strokeWidth: 2, color: brandOrange),
          ),
          const SizedBox(width: 8),
        ],
        Expanded(child: _Result(_stageLabel(device))),
      ],
    );
  }
}

class _Result extends StatelessWidget {
  const _Result(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: surfacePressed,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(text, style: const TextStyle(fontSize: 12, color: brandBlack)),
    );
  }
}

class _AutomationSheet extends StatefulWidget {
  const _AutomationSheet({required this.settings});

  final ProvisionerSettings settings;

  @override
  State<_AutomationSheet> createState() => _AutomationSheetState();
}

class _AutomationSheetState extends State<_AutomationSheet> {
  late final TextEditingController _package =
      TextEditingController(text: widget.settings.packageName);
  late final TextEditingController _url =
      TextEditingController(text: widget.settings.apkUrl);
  late final TextEditingController _serial =
      TextEditingController(text: widget.settings.serialNumber);

  static final _packagePattern =
      RegExp(r'^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z][A-Za-z0-9_]*)+$');

  @override
  void dispose() {
    _package.dispose();
    _url.dispose();
    _serial.dispose();
    super.dispose();
  }

  bool _validUrl(String value) {
    final uri = Uri.tryParse(value.trim());
    return uri != null && uri.scheme.toLowerCase() == 'https' && uri.host.isNotEmpty;
  }

  @override
  Widget build(BuildContext context) {
    final packageName = _package.text;
    final apkUrl = _url.text;
    final serial = _serial.text;
    final packageFilled = packageName.trim().isNotEmpty;
    final urlFilled = apkUrl.trim().isNotEmpty;
    final packageValid = _packagePattern.hasMatch(packageName.trim());
    final urlValid = _validUrl(apkUrl);
    final canSave = (packageValid && urlValid && packageFilled && urlFilled) ||
        (serial.trim().isNotEmpty && !packageFilled && !urlFilled);
    final bottom = MediaQuery.viewInsetsOf(context).bottom;
    return Padding(
      padding: EdgeInsets.fromLTRB(18, 12, 18, 28 + bottom),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Automate provisioning',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text(
              'Package + https URL automate DPC install. Serial is independent: when set, only that device is discovered, paired, and connected. After pairing, the SDK shows a connected-device dialog with live steps, or the DPC list if automation is off.',
              style: TextStyle(fontSize: 12, color: textSecondary),
            ),
            const SizedBox(height: 12),
            TextField(
              key: const Key('package_name'),
              controller: _package,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                labelText: 'Package name',
                helperText: packageFilled && !packageValid ? null : 'Example: com.example.dpc',
                errorText: packageFilled && !packageValid
                    ? 'Enter a valid package name, such as com.example.dpc'
                    : null,
              ),
            ),
            TextField(
              key: const Key('apk_url'),
              controller: _url,
              keyboardType: TextInputType.url,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                labelText: 'App download URL',
                helperText: urlFilled && !urlValid ? null : 'Must be a valid https:// URL',
                errorText: urlFilled && !urlValid ? 'Enter a valid https:// download URL' : null,
              ),
            ),
            TextField(
              key: const Key('serial_number'),
              controller: _serial,
              onChanged: (_) => setState(() {}),
              decoration: const InputDecoration(
                labelText: 'Serial No',
                helperText:
                    'Optional. Leave blank to allow any device. Required for the SDK connected-device dialog.',
              ),
            ),
            const SizedBox(height: 12),
            BrandButton(
              key: const Key('save_automation'),
              label: 'Save',
              onPressed: canSave
                  ? () async {
                      final autoOk = await Sdk.setAutomation(packageName, apkUrl);
                      final serialOk = await Sdk.setSerial(serial);
                      if (autoOk && serialOk && context.mounted) {
                        Navigator.pop(context, true);
                      }
                    }
                  : null,
            ),
            if (widget.settings.automationConfigured) ...[
              TextButton(
                key: const Key('clear_automation'),
                onPressed: () async {
                  if (await Sdk.clearAutomation() && context.mounted) {
                    Navigator.pop(context, true);
                  }
                },
                child: const Text('Turn off automation', style: TextStyle(color: brandOrange)),
              ),
              Text(
                widget.settings.hasSerialFilter
                    ? 'Automation is on for the device with serial ${widget.settings.serialNumber}.'
                    : 'Automation is on for every newly connected device.',
                style: const TextStyle(fontSize: 12, color: brandOrange),
              ),
            ],
            if (widget.settings.hasSerialFilter)
              TextButton(
                key: const Key('clear_serial'),
                onPressed: () async {
                  if (await Sdk.clearSerial() && context.mounted) {
                    Navigator.pop(context, true);
                  }
                },
                child: const Text('Clear serial lock', style: TextStyle(color: brandOrange)),
              ),
          ],
        ),
      ),
    );
  }
}

String _connectionLabel(String connection) {
  switch (connection) {
    case 'DISCOVERED':
      return 'Discovered';
    case 'PERMISSION_REQUIRED':
      return 'USB permission required';
    case 'CONNECTING':
      return 'Connecting';
    case 'UNAUTHORIZED':
      return 'Authorization required';
    case 'READY':
      return 'Connected and authorized';
    case 'DISCONNECTED':
      return 'Disconnected';
    case 'FAILED':
      return 'Connection failed';
    default:
      return connection;
  }
}

String? _statusText(DeviceItem device) {
  switch (device.connection) {
    case 'DISCOVERED':
      if (!device.wireless) return 'USB device discovered';
      if (device.previouslyPaired) return 'Previously paired';
      return 'Waiting for wireless pairing…';
    case 'PERMISSION_REQUIRED':
      return 'Waiting for USB permission…';
    case 'CONNECTING':
      if (device.previouslyPaired && device.wireless) {
        return 'Reconnecting to previously paired device…';
      }
      return 'Opening ADB connection…';
    case 'UNAUTHORIZED':
      return 'Authorize this computer on the device.';
    case 'READY':
      return null;
    case 'DISCONNECTED':
      return 'Disconnected';
    case 'FAILED':
      return 'Connection failed';
    default:
      return null;
  }
}

String _stageLabel(DeviceItem device) {
  switch (device.stageKind) {
    case 'authenticating':
      return 'Checking authorization';
    case 'scanning':
      return 'Checking device owner and DPC packages';
    case 'downloading':
      return 'Downloading APK';
    case 'pushing':
      return 'Sending APK to the device';
    case 'installing':
      return 'Installing APK';
    case 'updating':
      return 'Updating app';
    case 'settingOwner':
      return 'Setting device owner';
    case 'launching':
      return 'Launching the device owner app';
    case 'retrying':
      return 'Retrying: ${device.stageReason ?? ''}';
    case 'succeeded':
      return 'Provisioned: ${device.stageFlattened ?? ''}';
    case 'failed':
      return 'Provisioning Failed, try again after factory reset.';
    case 'ownerRemoved':
      return 'Provision Removed';
    default:
      return 'Waiting for an authorized connection';
  }
}
