package com.beastblocks.provisionerjatt.example

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
            ),
        )
    }
}
