package com.beastblocks.provisionerjatt.example

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.beastblocks.provisionerjattsdk.data.adb.WirelessPairingOffer
import com.beastblocks.provisionerjattsdk.domain.ConnectionState
import com.beastblocks.provisionerjattsdk.domain.DeviceId
import com.beastblocks.provisionerjattsdk.domain.DeviceTarget
import com.beastblocks.provisionerjattsdk.domain.DpcComponent
import com.beastblocks.provisionerjattsdk.domain.ProvisioningSettings
import com.beastblocks.provisionerjattsdk.domain.ProvisioningStage
import com.beastblocks.provisionerjattsdk.domain.TransportKind
import com.beastblocks.provisionerjatt.example.ProvisionerScreen
import org.junit.Rule
import org.junit.Test

class ProvisionerScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyStateShowsScanningAndAutomateAction() {
        compose.setContent { TestScreen(ProvisionerUiState()) }

        compose.onNodeWithTag("connected_empty").assertIsDisplayed()
        compose.onNodeWithTag("automate_provisioning").assertIsDisplayed()
        compose.onNodeWithTag("tab_discovered").assertIsDisplayed()
        compose.onNodeWithTag("tab_connected").assertIsDisplayed()
        compose.onNodeWithTag("pair_wireless").assertDoesNotExist()
        compose.onNodeWithTag("mode_automated").assertDoesNotExist()
        compose.onNodeWithTag("connect_device").assertDoesNotExist()
    }

    @Test fun pairingDialogAsksOnlyForTheCode() {
        compose.setContent {
            ProvisionerJattTheme {
                DefaultPairingDialog(
                    offers = listOf(
                        WirelessPairingOffer(
                            host = "192.0.2.2",
                            pairingPort = 37123,
                            connectPort = 42123,
                            serial = "ABC",
                        ),
                    ),
                    selectedKey = "192.0.2.2:37123",
                    result = null,
                    watermarkResId = null,
                    onDismiss = {},
                    onPair = {},
                )
            }
        }

        compose.onNodeWithTag("pairing_code").assertIsDisplayed()
        compose.onNodeWithTag("pairing_summary").assertTextContains("192.0.2.2")
        compose.onNodeWithTag("pairing_summary").assertTextContains("37123")
        compose.onNodeWithTag("pairing_summary").assertTextContains("42123")
    }

    @Test fun automateSheetShowsPersistedSettings() {
        compose.setContent {
            TestScreen(
                ProvisionerUiState(
                    settings = ProvisioningSettings(
                        packageName = "com.example.dpc",
                        apkUrl = "https://example.test/dpc.apk",
                        serialNumber = "10BD4L11A5001N9",
                    ),
                ),
            )
        }

        compose.onNodeWithTag("automate_provisioning").performClick()
        compose.waitForIdle()

        compose.onNodeWithTag("package_name").assertTextContains("com.example.dpc")
        compose.onNodeWithTag("apk_url").assertTextContains("https://example.test/dpc.apk")
        compose.onNodeWithTag("serial_number").assertTextContains("10BD4L11A5001N9")
        compose.onNodeWithTag("save_automation").assertIsEnabled()
        compose.onNodeWithTag("clear_automation").assertIsDisplayed()
    }

    @Test fun automateSheetSaveStaysDisabledUntilBothFieldsAreValid() {
        compose.setContent { TestScreen(ProvisionerUiState()) }

        compose.onNodeWithTag("automate_provisioning").performClick()
        compose.waitForIdle()

        compose.onNodeWithTag("save_automation").assertIsNotEnabled()
        compose.onNodeWithTag("clear_automation").assertDoesNotExist()

        compose.onNodeWithTag("package_name").performTextInput("com.example.dpc")
        compose.waitForIdle()
        compose.onNodeWithTag("save_automation").assertIsNotEnabled()

        compose.onNodeWithTag("apk_url").performTextInput("http://example.test/dpc.apk")
        compose.waitForIdle()
        compose.onNodeWithTag("save_automation").assertIsNotEnabled()

        compose.onNodeWithTag("apk_url").performTextReplacement("https://example.test/dpc.apk")
        compose.waitForIdle()
        compose.onNodeWithTag("save_automation").assertIsEnabled()

        compose.onNodeWithTag("serial_number").assertIsDisplayed()
        compose.onNodeWithTag("serial_number").performTextInput("OPTIONALSERIAL")
        compose.waitForIdle()
        compose.onNodeWithTag("save_automation").assertIsEnabled()
    }

    @Test fun discoveredAndConnectedTabsSplitDevices() {
        val discovered = DeviceUiState(
            target = DeviceTarget(DeviceId("usb:1"), "Phone A", TransportKind.USB),
            connection = ConnectionState.DISCOVERED,
        )
        val connected = DeviceUiState(
            target = DeviceTarget(DeviceId("usb:2"), "Phone B", TransportKind.USB),
            connection = ConnectionState.READY,
        )
        compose.setContent {
            TestScreen(ProvisionerUiState(devices = listOf(discovered, connected)))
        }

        compose.onNodeWithTag("device_usb:2").assertIsDisplayed()
        compose.onNodeWithTag("device_usb:1").assertDoesNotExist()
        compose.onNodeWithTag("connect_device").assertDoesNotExist()

        compose.onNodeWithTag("tab_discovered").performClick()
        compose.waitForIdle()

        compose.onNodeWithTag("device_usb:1").assertIsDisplayed()
        compose.onNodeWithTag("device_usb:2").assertDoesNotExist()
    }

    @Test fun removeOwnerHiddenForPackagesThatAreNotTestOnly() {
        compose.setContent {
            TestScreen(manualDevice(testOnly = false))
        }

        compose.onNodeWithTag("remove_owner").assertDoesNotExist()
    }

    @Test fun removeOwnerShownForTestOnlyPackages() {
        compose.setContent {
            TestScreen(manualDevice(testOnly = true))
        }

        compose.onNodeWithTag("remove_owner").assertIsDisplayed()
    }

    @Test fun ownerRemovedShowsRedoButton() {
        val component = DpcComponent("com.example.dpc", "com.example.dpc.Admin")
        compose.setContent {
            TestScreen(
                ProvisionerUiState(
                    devices = listOf(
                        DeviceUiState(
                            target = DeviceTarget(DeviceId("usb:1"), "Phone", TransportKind.USB),
                            connection = ConnectionState.READY,
                            automatedStage = ProvisioningStage.OwnerRemoved(component),
                        ),
                    ),
                ),
            )
        }

        compose.onNodeWithTag("provision_removed").assertIsDisplayed()
        compose.onNodeWithTag("redo_provisioning").assertIsDisplayed()
        compose.onNodeWithTag("remove_owner").assertDoesNotExist()
    }

    @Test fun disconnectShownOnlyOnReadyWirelessDevice() {
        compose.setContent {
            TestScreen(
                ProvisionerUiState(
                    devices = listOf(
                        DeviceUiState(
                            target = DeviceTarget(
                                DeviceId("wifi:192.0.2.2:42123"),
                                "Wireless phone",
                                TransportKind.WIRELESS,
                            ),
                            connection = ConnectionState.READY,
                        ),
                    ),
                ),
            )
        }

        compose.onNodeWithTag("disconnect_wireless").assertIsDisplayed()
    }

    @Test fun disconnectHiddenOnUsbAndUnreadyWireless() {
        compose.setContent {
            TestScreen(
                ProvisionerUiState(
                    devices = listOf(
                        DeviceUiState(
                            target = DeviceTarget(DeviceId("usb:1"), "USB phone", TransportKind.USB),
                            connection = ConnectionState.READY,
                        ),
                    ),
                ),
            )
        }

        compose.onNodeWithTag("disconnect_wireless").assertDoesNotExist()

        compose.setContent {
            TestScreen(
                ProvisionerUiState(
                    devices = listOf(
                        DeviceUiState(
                            target = DeviceTarget(
                                DeviceId("wifi:192.0.2.2:42123"),
                                "Wireless phone",
                                TransportKind.WIRELESS,
                            ),
                            connection = ConnectionState.DISCOVERED,
                        ),
                    ),
                ),
            )
        }

        compose.onNodeWithTag("tab_discovered").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("device_wifi:192.0.2.2:42123").assertIsDisplayed()
        compose.onNodeWithTag("disconnect_wireless").assertDoesNotExist()
    }
}

private fun manualDevice(testOnly: Boolean) = ProvisionerUiState(
    devices = listOf(
        DeviceUiState(
            target = DeviceTarget(DeviceId("usb:1"), "Phone", TransportKind.USB),
            connection = ConnectionState.READY,
            manual = ManualDeviceState(
                components = listOf(
                    DpcComponent(
                        "com.afwsamples.testdpc",
                        "com.afwsamples.testdpc.DeviceAdminReceiver",
                        testOnly = testOnly,
                    ),
                ),
            ),
        ),
    ),
)

@androidx.compose.runtime.Composable
private fun TestScreen(state: ProvisionerUiState) {
    ProvisionerJattTheme {
        ProvisionerScreen(
            state = state,
            onSaveAutomation = { _, _, _ -> true },
            onScan = {},
            onMakeOwner = { _, _ -> },
            onRemoveOwner = { _, _ -> },
            onRetryProvisioning = {},
            onDisconnectWireless = {},
        )
    }
}
