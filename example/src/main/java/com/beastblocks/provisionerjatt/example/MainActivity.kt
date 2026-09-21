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
        client = ProvisionerJatt.attach(this)
        setContent {
            val state by client.state.collectAsStateWithLifecycle()
            ProvisionerJattTheme {
                ProvisionerScreen(
                    state = state,
                    onSaveAutomation = { packageName, apkUrl, serial ->
                        if (packageName.isBlank() && apkUrl.isBlank()) {
                            client.clearAutomation()
                        } else {
                            client.setAutomation(packageName, apkUrl, serial)
                        }
                    },
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
