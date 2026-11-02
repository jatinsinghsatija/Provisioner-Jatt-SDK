package com.beastblocks.provisionerjatt.example_flutter

import android.app.Application
import com.beastblocks.provisionerjattsdk.ProvisionerJatt
import com.beastblocks.provisionerjattsdk.ProvisionerOptions

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ProvisionerJatt.initialize(
            this,
            ProvisionerOptions.from(this).copy(
                pairingWatermarkResId = R.drawable.logo_provisioner,
                enableSingleModeQRPairCodeLauncher = true,
                enableSingleModeProvisioningFloating = true,
            ),
        )
    }
}
