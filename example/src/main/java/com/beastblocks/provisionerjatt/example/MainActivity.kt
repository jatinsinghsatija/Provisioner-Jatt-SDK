package com.beastblocks.provisionerjatt.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beastblocks.provisionerjattsdk.ProvisionerClient
import com.beastblocks.provisionerjattsdk.ProvisionerJatt
import com.beastblocks.provisionerjattsdk.ui.ProvisionerJattTheme

class MainActivity : ComponentActivity() {
    private lateinit var client: ProvisionerClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        client = ProvisionerJatt.get()
        client.attach(this, this)
        setContent {
            val state by client.state.collectAsStateWithLifecycle()
            ProvisionerJattTheme {
                ProvisionerScreen(
                    state = state,
                    onScanThenAttach = { client.scanThenAttach(this@MainActivity, this@MainActivity) },
                    onSaveAutomation = { packageName, apkUrl ->
                        if (packageName.isBlank() && apkUrl.isBlank()) true
                        else client.setAutomation(packageName, apkUrl)
                    },
                    onSetSerial = { serial ->
                        if (serial.isBlank()) client.clearSerial() else client.setSerial(serial)
                    },
                    onClearAutomation = { client.clearAutomation() },
                    onClearSerial = { client.clearSerial() },
                    onScan = client::scan,
                    onMakeOwner = client::makeDeviceOwner,
                    onRemoveOwner = client::removeOwner,
                    onRetryProvisioning = client::retryProvisioning,
                    onDisconnectWireless = client::disconnectWireless,
                )
            }
        }
    }

    override fun onDestroy() {
        if (::client.isInitialized) client.detach(this)
        super.onDestroy()
    }
}
