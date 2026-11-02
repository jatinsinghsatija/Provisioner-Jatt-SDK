import 'package:flutter/services.dart';

const _channel = MethodChannel('com.beastblocks.provisionerjatt/sdk');
const stateChannel = EventChannel('com.beastblocks.provisionerjatt/state');

class Sdk {
  static Future<void> singleMode() => _channel.invokeMethod('singleMode');

  static Future<void> singleModeAutomation() =>
      _channel.invokeMethod('singleModeAutomation');

  static Future<String?> setAccountFrp() async =>
      _channel.invokeMethod<String>('setAccountFrp');

  static Future<String?> setOrganizationName() async =>
      _channel.invokeMethod<String>('setOrganizationName');

  static Future<bool> clearAutomationAndSerial() async =>
      (await _channel.invokeMethod<bool>('clearAutomationAndSerial')) ?? false;

  static Future<void> attach() => _channel.invokeMethod('attach');

  static Future<void> detach() => _channel.invokeMethod('detach');

  static Future<bool> openQr() async =>
      (await _channel.invokeMethod<bool>('openQr')) ?? false;

  static Future<bool> openPairing() async =>
      (await _channel.invokeMethod<bool>('openPairing')) ?? false;

  static Future<bool> setAutomation(String packageName, String apkUrl) async =>
      (await _channel.invokeMethod<bool>('setAutomation', {
        'packageName': packageName,
        'apkUrl': apkUrl,
      })) ??
      false;

  static Future<bool> setSerial(String serial) async =>
      (await _channel.invokeMethod<bool>('setSerial', {'serial': serial})) ??
      false;

  static Future<bool> clearAutomation() async =>
      (await _channel.invokeMethod<bool>('clearAutomation')) ?? false;

  static Future<bool> clearSerial() async =>
      (await _channel.invokeMethod<bool>('clearSerial')) ?? false;

  static Future<void> scan(String id) =>
      _channel.invokeMethod('scan', {'id': id});

  static Future<void> makeOwner(String id, DpcComponent component) =>
      _channel.invokeMethod('makeOwner', {
        'id': id,
        ...component.toMap(),
      });

  static Future<void> removeOwner(String id, DpcComponent component) =>
      _channel.invokeMethod('removeOwner', {
        'id': id,
        ...component.toMap(),
      });

  static Future<void> retryProvisioning(String id) =>
      _channel.invokeMethod('retryProvisioning', {'id': id});

  static Future<void> disconnectWireless(String id) =>
      _channel.invokeMethod('disconnectWireless', {'id': id});
}

class ProvisionerSnapshot {
  const ProvisionerSnapshot({
    required this.scanning,
    required this.settings,
    required this.devices,
  });

  final bool scanning;
  final ProvisionerSettings settings;
  final List<DeviceItem> devices;

  static const empty = ProvisionerSnapshot(
    scanning: true,
    settings: ProvisionerSettings.empty,
    devices: [],
  );

  factory ProvisionerSnapshot.from(Object? raw) {
    final map = _map(raw);
    return ProvisionerSnapshot(
      scanning: map['scanning'] == true,
      settings: ProvisionerSettings.from(map['settings']),
      devices: _list(map['devices']).map(DeviceItem.from).toList(),
    );
  }
}

class ProvisionerSettings {
  const ProvisionerSettings({
    required this.packageName,
    required this.apkUrl,
    required this.serialNumber,
    required this.automationConfigured,
    required this.hasSerialFilter,
  });

  final String packageName;
  final String apkUrl;
  final String serialNumber;
  final bool automationConfigured;
  final bool hasSerialFilter;

  static const empty = ProvisionerSettings(
    packageName: '',
    apkUrl: '',
    serialNumber: '',
    automationConfigured: false,
    hasSerialFilter: false,
  );

  factory ProvisionerSettings.from(Object? raw) {
    final map = _map(raw);
    return ProvisionerSettings(
      packageName: map['packageName']?.toString() ?? '',
      apkUrl: map['apkUrl']?.toString() ?? '',
      serialNumber: map['serialNumber']?.toString() ?? '',
      automationConfigured: map['automationConfigured'] == true,
      hasSerialFilter: map['hasSerialFilter'] == true,
    );
  }
}

class DpcComponent {
  const DpcComponent({
    required this.packageName,
    required this.receiverClass,
    required this.testOnly,
    required this.flattened,
  });

  final String packageName;
  final String receiverClass;
  final bool testOnly;
  final String flattened;

  Map<String, Object> toMap() => {
        'packageName': packageName,
        'receiverClass': receiverClass,
        'testOnly': testOnly,
      };

  factory DpcComponent.from(Object? raw) {
    final map = _map(raw);
    return DpcComponent(
      packageName: map['packageName']?.toString() ?? '',
      receiverClass: map['receiverClass']?.toString() ?? '',
      testOnly: map['testOnly'] == true,
      flattened: map['flattened']?.toString() ?? '',
    );
  }
}

class DeviceItem {
  const DeviceItem({
    required this.id,
    required this.name,
    required this.transport,
    required this.serial,
    required this.connection,
    required this.message,
    required this.previouslyPaired,
    required this.manualScanning,
    required this.manualBusy,
    required this.manualResult,
    required this.components,
    required this.stageKind,
    required this.stageReason,
    required this.stageFlattened,
  });

  final String id;
  final String name;
  final String transport;
  final String? serial;
  final String connection;
  final String? message;
  final bool previouslyPaired;
  final bool manualScanning;
  final bool manualBusy;
  final String? manualResult;
  final List<DpcComponent> components;
  final String stageKind;
  final String? stageReason;
  final String? stageFlattened;

  bool get ready => connection == 'READY';
  bool get wireless => transport == 'WIRELESS';

  factory DeviceItem.from(Object? raw) {
    final map = _map(raw);
    final manual = _map(map['manual']);
    final stage = _map(map['stage']);
    final serial = map['serial']?.toString();
    return DeviceItem(
      id: map['id']?.toString() ?? '',
      name: map['name']?.toString() ?? '',
      transport: map['transport']?.toString() ?? 'USB',
      serial: serial == null || serial.isEmpty ? null : serial,
      connection: map['connection']?.toString() ?? 'DISCOVERED',
      message: map['message']?.toString(),
      previouslyPaired: map['previouslyPaired'] == true,
      manualScanning: manual['scanning'] == true,
      manualBusy: manual['busy'] == true,
      manualResult: manual['result']?.toString(),
      components: _list(manual['components']).map(DpcComponent.from).toList(),
      stageKind: stage['kind']?.toString() ?? 'idle',
      stageReason: stage['reason']?.toString(),
      stageFlattened: stage['flattened']?.toString(),
    );
  }
}

Map<Object?, Object?> _map(Object? raw) {
  if (raw is Map) return raw;
  return const {};
}

List<Object?> _list(Object? raw) {
  if (raw is List) return raw;
  return const [];
}
