package com.beastblocks.provisionerjatt.example

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.beastblocks.provisionerjatt.example.R
import com.beastblocks.provisionerjattsdk.domain.ConnectionState
import com.beastblocks.provisionerjattsdk.domain.DeviceId
import com.beastblocks.provisionerjattsdk.domain.DpcComponent
import com.beastblocks.provisionerjattsdk.domain.ProvisioningSettings
import com.beastblocks.provisionerjattsdk.domain.ProvisioningStage
import com.beastblocks.provisionerjattsdk.domain.TransportKind
import com.beastblocks.provisionerjattsdk.ui.BrandBackdrop
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState
import com.beastblocks.provisionerjattsdk.ui.NeumorphicButton
import com.beastblocks.provisionerjattsdk.ui.NeumorphicSurface
import com.beastblocks.provisionerjattsdk.ui.NeumorphicTheme
import com.beastblocks.provisionerjattsdk.ui.ProvisionerUiState
import com.beastblocks.provisionerjattsdk.ui.neumorphicSurface

private enum class DeviceListTab { CONNECTED, DISCOVERED }

private val ListingWatermark = R.drawable.logo_provisioner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisionerScreen(
    state: ProvisionerUiState,
    onScanThenAttach: () -> Unit = {},
    onSaveAutomation: (String, String) -> Boolean,
    onSetSerial: (String) -> Boolean,
    onClearAutomation: () -> Boolean,
    onClearSerial: () -> Boolean,
    onScan: (DeviceId) -> Unit,
    onMakeOwner: (DeviceId, DpcComponent) -> Unit,
    onRemoveOwner: (DeviceId, DpcComponent) -> Unit,
    onRetryProvisioning: (DeviceId) -> Unit,
    onDisconnectWireless: (DeviceId) -> Unit,
) {
    val colors = NeumorphicTheme.colors
    var automationSheetOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(DeviceListTab.CONNECTED) }
    val configuration = LocalConfiguration.current
    val wideLayout = configuration.screenWidthDp >= 600
    val horizontalPadding = if (wideLayout) 32.dp else 18.dp
    val discovered = state.devices.filter { it.connection != ConnectionState.READY }
    val connected = state.devices.filter { it.connection == ConnectionState.READY }
    val visible = if (selectedTab == DeviceListTab.DISCOVERED) discovered else connected
    BrandBackdrop(
        modifier = Modifier.fillMaxSize().background(colors.background),
        watermarkResId = ListingWatermark,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    modifier = Modifier.neumorphicSurface(
                        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                        elevation = 5.dp,
                    ),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface,
                        titleContentColor = colors.textPrimary,
                        actionIconContentColor = colors.accent,
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_provisioner),
                                contentDescription = null,
                                modifier = Modifier.size(if (wideLayout) 40.dp else 32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                            Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = onScanThenAttach,
                            modifier = Modifier.testTag("scan_then_attach"),
                        ) {
                            Text(
                                "Nearby/USB",
                                color = colors.accent,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        }
                        TextButton(
                            onClick = { automationSheetOpen = true },
                            modifier = Modifier.testTag("automate_provisioning"),
                        ) {
                            Text(
                                "Automate provisioning",
                                color = colors.accent,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
                val contentModifier = Modifier
                    .widthIn(max = if (wideLayout) 1080.dp else 720.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .testTag("provisioner_screen")
                if (wideLayout) {
                    WideDeviceLists(
                        modifier = contentModifier.padding(horizontal = horizontalPadding, vertical = 18.dp),
                        discovered = discovered,
                        connected = connected,
                        scanning = state.scanning,
                        settings = state.settings,
                        onScan = onScan,
                        onMakeOwner = onMakeOwner,
                        onRemoveOwner = onRemoveOwner,
                        onRetryProvisioning = onRetryProvisioning,
                        onDisconnectWireless = onDisconnectWireless,
                    )
                } else {
                    LazyColumn(
                        modifier = contentModifier,
                        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        item {
                            DeviceListTabs(
                                selected = selectedTab,
                                discoveredCount = discovered.size,
                                connectedCount = connected.size,
                                onSelect = { selectedTab = it },
                            )
                        }
                        if (state.settings.hasSerialFilter) {
                            item { SerialLockBanner(settings = state.settings) }
                        }
                        if (visible.isEmpty()) {
                            item {
                                EmptyDevices(
                                    scanning = state.scanning,
                                    tab = selectedTab,
                                )
                            }
                        } else {
                            items(visible, key = { it.target.id.value }) { device ->
                                DeviceCard(
                                    device = device,
                                    automationConfigured = state.settings.automationConfigured,
                                    onScan = { onScan(device.target.id) },
                                    onMakeOwner = { onMakeOwner(device.target.id, it) },
                                    onRemoveOwner = { onRemoveOwner(device.target.id, it) },
                                    onRetryProvisioning = { onRetryProvisioning(device.target.id) },
                                    onDisconnectWireless = { onDisconnectWireless(device.target.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (automationSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { automationSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            modifier = Modifier.testTag("automate_sheet"),
        ) {
            BrandBackdrop(watermarkResId = ListingWatermark) {
                AutomationSheet(
                    settings = state.settings,
                    onSave = { packageName, apkUrl, serialNumber ->
                        val autoOk = onSaveAutomation(packageName, apkUrl)
                        val serialOk = onSetSerial(serialNumber)
                        if (autoOk && serialOk) {
                            automationSheetOpen = false
                            true
                        } else {
                            false
                        }
                    },
                    onClearAutomation = {
                        if (onClearAutomation()) automationSheetOpen = false
                    },
                    onClearSerial = {
                        if (onClearSerial()) automationSheetOpen = false
                    },
                )
            }
        }
    }
}

@Composable
private fun WideDeviceLists(
    modifier: Modifier,
    discovered: List<DeviceUiState>,
    connected: List<DeviceUiState>,
    scanning: Boolean,
    settings: ProvisioningSettings,
    onScan: (DeviceId) -> Unit,
    onMakeOwner: (DeviceId, DpcComponent) -> Unit,
    onRemoveOwner: (DeviceId, DpcComponent) -> Unit,
    onRetryProvisioning: (DeviceId) -> Unit,
    onDisconnectWireless: (DeviceId) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (settings.hasSerialFilter) {
            SerialLockBanner(settings = settings)
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DevicePane(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "Discovered",
                count = discovered.size,
                devices = discovered,
                emptyTab = DeviceListTab.DISCOVERED,
                scanning = scanning,
                automationConfigured = settings.automationConfigured,
                onScan = onScan,
                onMakeOwner = onMakeOwner,
                onRemoveOwner = onRemoveOwner,
                onRetryProvisioning = onRetryProvisioning,
                onDisconnectWireless = onDisconnectWireless,
            )
            DevicePane(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "Connected",
                count = connected.size,
                devices = connected,
                emptyTab = DeviceListTab.CONNECTED,
                scanning = scanning,
                automationConfigured = settings.automationConfigured,
                onScan = onScan,
                onMakeOwner = onMakeOwner,
                onRemoveOwner = onRemoveOwner,
                onRetryProvisioning = onRetryProvisioning,
                onDisconnectWireless = onDisconnectWireless,
            )
        }
    }
}

@Composable
private fun DevicePane(
    modifier: Modifier,
    title: String,
    count: Int,
    devices: List<DeviceUiState>,
    emptyTab: DeviceListTab,
    scanning: Boolean,
    automationConfigured: Boolean,
    onScan: (DeviceId) -> Unit,
    onMakeOwner: (DeviceId, DpcComponent) -> Unit,
    onRemoveOwner: (DeviceId, DpcComponent) -> Unit,
    onRetryProvisioning: (DeviceId) -> Unit,
    onDisconnectWireless: (DeviceId) -> Unit,
) {
    val colors = NeumorphicTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "$title  $count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (devices.isEmpty()) {
                item { EmptyDevices(scanning = scanning, tab = emptyTab) }
            } else {
                items(devices, key = { it.target.id.value }) { device ->
                    DeviceCard(
                        device = device,
                        automationConfigured = automationConfigured,
                        onScan = { onScan(device.target.id) },
                        onMakeOwner = { onMakeOwner(device.target.id, it) },
                        onRemoveOwner = { onRemoveOwner(device.target.id, it) },
                        onRetryProvisioning = { onRetryProvisioning(device.target.id) },
                        onDisconnectWireless = { onDisconnectWireless(device.target.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceListTabs(
    selected: DeviceListTab,
    discoveredCount: Int,
    connectedCount: Int,
    onSelect: (DeviceListTab) -> Unit,
) {
    val colors = NeumorphicTheme.colors
    NeumorphicSurface(
        modifier = Modifier.fillMaxWidth(),
        pressed = true,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DeviceListTab.entries.forEach { tab ->
                val count = if (tab == DeviceListTab.DISCOVERED) discoveredCount else connectedCount
                val label = if (tab == DeviceListTab.DISCOVERED) "Discovered" else "Connected"
                val isSelected = selected == tab
                val interactionSource = remember(tab) { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag(
                            if (tab == DeviceListTab.DISCOVERED) "tab_discovered" else "tab_connected",
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (isSelected) {
                                Modifier.neumorphicSurface(
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = 5.dp,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(tab) },
                        )
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$label  $count",
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun AutomationSheet(
    settings: ProvisioningSettings,
    onSave: (String, String, String) -> Boolean,
    onClearAutomation: () -> Unit,
    onClearSerial: () -> Unit,
) {
    var packageName by remember { mutableStateOf(settings.packageName) }
    var apkUrl by remember { mutableStateOf(settings.apkUrl) }
    var serialNumber by remember { mutableStateOf(settings.serialNumber) }
    val packageValid = ProvisioningSettings.isValidPackageName(packageName)
    val urlValid = ProvisioningSettings.isValidDownloadUrl(apkUrl)
    val packageFilled = packageName.isNotBlank()
    val urlFilled = apkUrl.isNotBlank()
    val canSave = (packageValid && urlValid && packageFilled && urlFilled) ||
        (serialNumber.isNotBlank() && !packageFilled && !urlFilled)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Automate provisioning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Package + https URL automate DPC install. Serial is independent: when set, only that device is discovered, paired, and connected. After pairing, the SDK shows a connected-device dialog with live steps, or the DPC list if automation is off.",
            style = MaterialTheme.typography.bodySmall,
            color = NeumorphicTheme.colors.textSecondary,
        )
        OutlinedTextField(
            value = packageName,
            onValueChange = { packageName = it },
            modifier = Modifier.fillMaxWidth().testTag("package_name"),
            label = { Text("Package name") },
            singleLine = true,
            isError = packageFilled && !packageValid,
            supportingText = {
                Text(
                    if (packageFilled && !packageValid) {
                        "Enter a valid package name, such as com.example.dpc"
                    } else {
                        "Example: com.example.dpc"
                    },
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        OutlinedTextField(
            value = apkUrl,
            onValueChange = { apkUrl = it },
            modifier = Modifier.fillMaxWidth().testTag("apk_url"),
            label = { Text("App download URL") },
            singleLine = true,
            isError = urlFilled && !urlValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            supportingText = {
                Text(
                    if (urlFilled && !urlValid) {
                        "Enter a valid https:// download URL"
                    } else {
                        "Must be a valid https:// URL"
                    },
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            modifier = Modifier.fillMaxWidth().testTag("serial_number"),
            label = { Text("Serial No") },
            singleLine = true,
            supportingText = {
                Text("Optional. Leave blank to allow any device. Required for the SDK connected-device dialog.")
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        NeumorphicButton(
            onClick = { onSave(packageName, apkUrl, serialNumber) },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().testTag("save_automation"),
        ) { Text("Save") }
        if (settings.automationConfigured) {
            TextButton(
                onClick = onClearAutomation,
                modifier = Modifier.fillMaxWidth().testTag("clear_automation"),
            ) {
                Text("Turn off automation")
            }
            Text(
                if (settings.hasSerialFilter) {
                    "Automation is on for the device with serial ${settings.serialNumber}."
                } else {
                    "Automation is on for every newly connected device."
                },
                style = MaterialTheme.typography.bodySmall,
                color = NeumorphicTheme.colors.success,
            )
        }
        if (settings.hasSerialFilter) {
            TextButton(
                onClick = onClearSerial,
                modifier = Modifier.fillMaxWidth().testTag("clear_serial"),
            ) {
                Text("Clear serial lock")
            }
        }
    }
}

@Composable
private fun SerialLockBanner(settings: ProvisioningSettings) {
    NeumorphicSurface(
        modifier = Modifier.fillMaxWidth().testTag("serial_lock_banner"),
        shape = RoundedCornerShape(16.dp),
        selected = true,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Serial lock  ${settings.serialNumber}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = NeumorphicTheme.colors.accent,
            )
            Text(
                "Only this device can be discovered, paired, and connected. After pairing and authorization, the SDK connected-device dialog tracks provisioning or DPC actions.",
                style = MaterialTheme.typography.bodySmall,
                color = NeumorphicTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun EmptyDevices(scanning: Boolean, tab: DeviceListTab) {
    val tag = if (tab == DeviceListTab.DISCOVERED) "devices_empty" else "connected_empty"
    NeumorphicSurface(modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (scanning) CircularProgressIndicator(color = NeumorphicTheme.colors.accent)
            if (tab == DeviceListTab.CONNECTED) {
                Text("No connected devices", style = MaterialTheme.typography.titleMedium)
                Text(
                    "USB devices connect after permission. Wireless devices show up here after pairing.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    if (scanning) "Scanning for devices…" else "No discovered devices",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Connect a phone over USB, or enable Wireless debugging. Pairing is only asked for devices that are not already connected.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DeviceUiState,
    automationConfigured: Boolean,
    onScan: () -> Unit,
    onMakeOwner: (DpcComponent) -> Unit,
    onRemoveOwner: (DpcComponent) -> Unit,
    onRetryProvisioning: () -> Unit,
    onDisconnectWireless: () -> Unit,
) {
    val wirelessConnected =
        device.target.transport == TransportKind.WIRELESS &&
            device.connection == ConnectionState.READY
    NeumorphicSurface(modifier = Modifier.fillMaxWidth().testTag("device_${device.target.id.value}")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.target.displayName, fontWeight = FontWeight.SemiBold)
                    Text(
                        listOfNotNull(
                            device.target.transport.name.lowercase().replaceFirstChar(Char::uppercase),
                            device.target.serial,
                            connectionLabel(device.connection),
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (device.connection == ConnectionState.CONNECTING) {
                        CircularProgressIndicator(color = NeumorphicTheme.colors.accent)
                    }
                    if (wirelessConnected) {
                        TextButton(
                            onClick = onDisconnectWireless,
                            modifier = Modifier.testTag("disconnect_wireless"),
                        ) {
                            Text("Disconnect", color = NeumorphicTheme.colors.accent)
                        }
                    }
                }
            }
            ConnectionStatus(device)
            device.message?.let { ResultText(it) }
            if (device.connection == ConnectionState.READY) {
                when {
                    device.automatedStage is ProvisioningStage.OwnerRemoved ->
                        AutomatedStage(device.automatedStage, onRetryProvisioning)
                    automationConfigured -> AutomatedStage(device.automatedStage, onRetryProvisioning)
                    else -> ManualContent(device, onScan, onMakeOwner, onRemoveOwner)
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatus(device: DeviceUiState) {
    val text: String? = when (device.connection) {
        ConnectionState.DISCOVERED ->
            if (device.target.transport == TransportKind.USB) {
                "USB device discovered"
            } else if (device.previouslyPaired) {
                "Previously paired"
            } else {
                "Waiting for wireless pairing…"
            }
        ConnectionState.PERMISSION_REQUIRED -> "Waiting for USB permission…"
        ConnectionState.CONNECTING ->
            if (device.previouslyPaired && device.target.transport == TransportKind.WIRELESS) {
                "Reconnecting to previously paired device…"
            } else {
                "Opening ADB connection…"
            }
        ConnectionState.UNAUTHORIZED -> "Authorize this computer on the device."
        ConnectionState.READY -> null
        ConnectionState.DISCONNECTED -> "Disconnected"
        ConnectionState.FAILED -> "Connection failed"
    }
    if (text != null) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ManualContent(
    device: DeviceUiState,
    onScan: () -> Unit,
    onMakeOwner: (DpcComponent) -> Unit,
    onRemoveOwner: (DpcComponent) -> Unit,
) {
    var confirmation by remember { mutableStateOf<Pair<Boolean, DpcComponent>?>(null) }
    HorizontalDivider(color = NeumorphicTheme.colors.darkShadow.copy(alpha = 0.25f))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("DPC components", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        TextButton(onClick = onScan, enabled = !device.manual.scanning) { Text("Scan") }
    }
    if (device.manual.scanning) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = NeumorphicTheme.colors.accent)
            Text("Scanning device policy components…")
        }
    }
    device.manual.components.forEach { component ->
        NeumorphicSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(component.flattened, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeumorphicButton(
                        onClick = { confirmation = true to component },
                        enabled = device.manual.actionInProgress == null,
                    ) { Text("Make Device Owner") }
                    if (component.testOnly) {
                        NeumorphicButton(
                            onClick = { confirmation = false to component },
                            enabled = device.manual.actionInProgress == null,
                            accent = false,
                            modifier = Modifier.testTag("remove_owner"),
                        ) { Text("Remove Owner") }
                    }
                }
                if (!component.testOnly) {
                    Text(
                        "Remove Owner is hidden because this package is not test-only. Android will reject dpm remove-active-admin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeumorphicTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
    if (device.manual.actionInProgress != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = NeumorphicTheme.colors.accent)
            Text("Applying command…")
        }
    }
    device.manual.result?.let { ResultText(it) }
    confirmation?.let { (makeOwner, component) ->
        AlertDialog(
            modifier = Modifier.neumorphicSurface(shape = RoundedCornerShape(24.dp)),
            containerColor = NeumorphicTheme.colors.surface,
            onDismissRequest = { confirmation = null },
            title = { Text(if (makeOwner) "Make Device Owner?" else "Remove Owner?") },
            text = {
                BrandBackdrop(modifier = Modifier.fillMaxWidth(), watermarkResId = ListingWatermark) {
                    Text(
                        (if (makeOwner) "Set" else "Remove") +
                            " ${component.flattened}? This changes device administration state.",
                    )
                }
            },
            confirmButton = {
                NeumorphicButton(onClick = {
                    confirmation = null
                    if (makeOwner) onMakeOwner(component) else onRemoveOwner(component)
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AutomatedStage(stage: ProvisioningStage, onRetry: () -> Unit) {
    HorizontalDivider(color = NeumorphicTheme.colors.darkShadow.copy(alpha = 0.25f))
    if (stage is ProvisioningStage.OwnerRemoved) {
        Column(
            modifier = Modifier.testTag("provision_removed"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultText("Provision Removed")
            Text(
                "Device owner ${stage.component.flattened} is no longer set. Redo the full provisioning process.",
                style = MaterialTheme.typography.bodySmall,
                color = NeumorphicTheme.colors.textSecondary,
            )
            NeumorphicButton(
                onClick = onRetry,
                modifier = Modifier.testTag("redo_provisioning"),
            ) { Text("Redo") }
        }
        return
    }
    val (label, working) = when (stage) {
        ProvisioningStage.Idle -> "Waiting for an authorized connection" to false
        ProvisioningStage.Authenticating -> "Checking authorization" to true
        ProvisioningStage.Scanning -> "Checking device owner and DPC packages" to true
        ProvisioningStage.Downloading -> "Downloading APK" to true
        ProvisioningStage.Pushing -> "Sending APK to the device" to true
        ProvisioningStage.Installing -> "Installing APK" to true
        ProvisioningStage.SettingOwner -> "Setting device owner" to true
        ProvisioningStage.Launching -> "Launching the device owner app" to true
        is ProvisioningStage.Retrying -> "Retrying: ${stage.reason}" to true
        is ProvisioningStage.Succeeded -> "Provisioned: ${stage.component.flattened}" to false
        is ProvisioningStage.Failed -> {
            val text = if (stage.factoryResetGuidance) {
                if (stage.reason.contains("factory reset", ignoreCase = true)) stage.reason
                else "Factory reset required. ${stage.reason}"
            } else {
                "Failed: ${stage.reason}"
            }
            text to false
        }
        is ProvisioningStage.OwnerRemoved -> "Provision Removed" to false
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (working) CircularProgressIndicator(color = NeumorphicTheme.colors.accent)
        ResultText(label)
    }
}

@Composable
private fun ResultText(text: String) {
    NeumorphicSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        pressed = true,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = NeumorphicTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun neumorphicTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NeumorphicTheme.colors.surfacePressed,
    unfocusedContainerColor = NeumorphicTheme.colors.surfacePressed,
    disabledContainerColor = NeumorphicTheme.colors.surfacePressed,
    focusedBorderColor = NeumorphicTheme.colors.accent,
    unfocusedBorderColor = NeumorphicTheme.colors.outline,
    focusedTextColor = NeumorphicTheme.colors.textPrimary,
    unfocusedTextColor = NeumorphicTheme.colors.textPrimary,
    focusedLabelColor = NeumorphicTheme.colors.accent,
    unfocusedLabelColor = NeumorphicTheme.colors.textSecondary,
    errorBorderColor = NeumorphicTheme.colors.accent,
    errorLabelColor = NeumorphicTheme.colors.accent,
    errorSupportingTextColor = NeumorphicTheme.colors.accent,
    cursorColor = NeumorphicTheme.colors.accent,
    errorCursorColor = NeumorphicTheme.colors.accent,
)

private fun connectionLabel(state: ConnectionState): String = when (state) {
    ConnectionState.DISCOVERED -> "Discovered"
    ConnectionState.PERMISSION_REQUIRED -> "USB permission required"
    ConnectionState.CONNECTING -> "Connecting"
    ConnectionState.UNAUTHORIZED -> "Authorization required"
    ConnectionState.READY -> "Connected and authorized"
    ConnectionState.DISCONNECTED -> "Disconnected"
    ConnectionState.FAILED -> "Connection failed"
}
