package com.beastblocks.provisionerjatt.examplejava;

import android.app.Application;
import com.beastblocks.provisionerjattsdk.PairingDialogColors;
import com.beastblocks.provisionerjattsdk.ProvisionerJatt;
import com.beastblocks.provisionerjattsdk.ProvisionerOptions;

public class ExampleJavaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProvisionerJatt.initialize(
            this,
            new ProvisionerOptions(
                new PairingDialogColors(
                    PairingDialogColors.DEFAULT_BACKGROUND,
                    PairingDialogColors.DEFAULT_SURFACE,
                    PairingDialogColors.DEFAULT_SURFACE_PRESSED,
                    PairingDialogColors.DEFAULT_ACCENT,
                    PairingDialogColors.DEFAULT_ON_ACCENT,
                    PairingDialogColors.DEFAULT_YELLOW,
                    PairingDialogColors.DEFAULT_TEXT_PRIMARY,
                    PairingDialogColors.DEFAULT_TEXT_SECONDARY,
                    PairingDialogColors.DEFAULT_OUTLINE
                ),
                R.drawable.logo_provisioner,
                null,
                true
            )
        );
    }
}
