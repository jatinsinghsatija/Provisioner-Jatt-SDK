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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.beastblocks.provisionerjatt.example.R
import com.beastblocks.provisionerjattsdk.domain.ConnectionState
import com.beastblocks.provisionerjattsdk.domain.DeviceId
import com.beastblocks.provisionerjattsdk.domain.DpcComponent
import com.beastblocks.provisionerjattsdk.domain.ProvisioningSettings
import com.beastblocks.provisionerjattsdk.ui.BrandBackdrop
import com.beastblocks.provisionerjattsdk.ui.DeviceListKind
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState
import com.beastblocks.provisionerjattsdk.ui.NeumorphicButton
import com.beastblocks.provisionerjattsdk.ui.NeumorphicSurface
import com.beastblocks.provisionerjattsdk.ui.NeumorphicTheme
import com.beastblocks.provisionerjattsdk.ui.ProvisionerJattDeviceList
import com.beastblocks.provisionerjattsdk.ui.ProvisionerUiState
import com.beastblocks.provisionerjattsdk.ui.neumorphicSurface

private enum class DeviceListTab { CONNECTED, DISCOVERED }

private val ListingWatermark = R.drawable.logo_provisioner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisionerScreen(
    state: ProvisionerUiState,
    onOpenQr: () -> Unit = {},
    onOpenPairing: () -> Unit = {},
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
                        IconButton(
                            onClick = onOpenQr,
                            modifier = Modifier.testTag("open_qr"),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_qr_code),
                                contentDescription = stringResource(R.string.action_qr),
                                tint = colors.accent,
                            )
                        }
                        IconButton(
                            onClick = onOpenPairing,
                            modifier = Modifier.testTag("open_pairing"),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_passcode),
                                contentDescription = stringResource(R.string.action_passcode),
                                tint = colors.accent,
                            )
                        }
                        TextButton(
                            onClick = { automationSheetOpen = true },
                            modifier = Modifier.testTag("automate_provisioning"),
                        ) {
                            Text(
                                stringResource(R.string.automate_provisioning),
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
                        item {
                            ProvisionerJattDeviceList(
                                kind = if (selectedTab == DeviceListTab.DISCOVERED) {
                                    DeviceListKind.DISCOVERABLE
                                } else {
                                    DeviceListKind.CONNECTED
                                },
                                devices = visible,
                                automationConfigured = state.settings.automationConfigured,
                                scanning = state.scanning,
                                scrollable = false,
                                onScan = onScan,
                                onMakeOwner = onMakeOwner,
                                onRemoveOwner = onRemoveOwner,
                                onRetryProvisioning = onRetryProvisioning,
                                onDisconnectWireless = onDisconnectWireless,
                            )
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
                title = stringResource(R.string.pane_discovered),
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
                title = stringResource(R.string.pane_connected),
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
        ProvisionerJattDeviceList(
            kind = if (emptyTab == DeviceListTab.DISCOVERED) {
                DeviceListKind.DISCOVERABLE
            } else {
                DeviceListKind.CONNECTED
            },
            devices = devices,
            modifier = Modifier.fillMaxSize(),
            automationConfigured = automationConfigured,
            scanning = scanning,
            scrollable = true,
            onScan = onScan,
            onMakeOwner = onMakeOwner,
            onRemoveOwner = onRemoveOwner,
            onRetryProvisioning = onRetryProvisioning,
            onDisconnectWireless = onDisconnectWireless,
        )
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
                val label = stringResource(
                    if (tab == DeviceListTab.DISCOVERED) R.string.pane_discovered else R.string.pane_connected,
                )
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
        Text(stringResource(R.string.automate_provisioning), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.automate_help),
            style = MaterialTheme.typography.bodySmall,
            color = NeumorphicTheme.colors.textSecondary,
        )
        OutlinedTextField(
            value = packageName,
            onValueChange = { packageName = it },
            modifier = Modifier.fillMaxWidth().testTag("package_name"),
            label = { Text(stringResource(R.string.package_name)) },
            singleLine = true,
            isError = packageFilled && !packageValid,
            supportingText = {
                Text(
                    stringResource(
                        if (packageFilled && !packageValid) R.string.package_error else R.string.package_hint,
                    ),
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        OutlinedTextField(
            value = apkUrl,
            onValueChange = { apkUrl = it },
            modifier = Modifier.fillMaxWidth().testTag("apk_url"),
            label = { Text(stringResource(R.string.apk_url)) },
            singleLine = true,
            isError = urlFilled && !urlValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            supportingText = {
                Text(
                    stringResource(
                        if (urlFilled && !urlValid) R.string.apk_error else R.string.apk_hint,
                    ),
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            modifier = Modifier.fillMaxWidth().testTag("serial_number"),
            label = { Text(stringResource(R.string.serial_no)) },
            singleLine = true,
            supportingText = {
                Text(stringResource(R.string.serial_hint))
            },
            shape = RoundedCornerShape(14.dp),
            colors = neumorphicTextFieldColors(),
        )
        NeumorphicButton(
            onClick = { onSave(packageName, apkUrl, serialNumber) },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().testTag("save_automation"),
        ) { Text(stringResource(R.string.save)) }
        if (settings.automationConfigured) {
            TextButton(
                onClick = onClearAutomation,
                modifier = Modifier.fillMaxWidth().testTag("clear_automation"),
            ) {
                Text(stringResource(R.string.turn_off_automation))
            }
            Text(
                if (settings.hasSerialFilter) {
                    stringResource(R.string.automation_on_serial, settings.serialNumber)
                } else {
                    stringResource(R.string.automation_on_all)
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
                Text(stringResource(R.string.clear_serial_lock))
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
                stringResource(R.string.serial_lock_title, settings.serialNumber),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = NeumorphicTheme.colors.accent,
            )
            Text(
                stringResource(R.string.serial_lock_body),
                style = MaterialTheme.typography.bodySmall,
                color = NeumorphicTheme.colors.textSecondary,
            )
        }
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
