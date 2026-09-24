package com.beastblocks.provisionerjatt.examplejava;

import android.app.Application;
import com.beastblocks.provisionerjattsdk.PairingDialogColors;
import com.beastblocks.provisionerjattsdk.ProvisionerJatt;
import com.beastblocks.provisionerjattsdk.ProvisionerOptions;

public class ExampleJavaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProvisionerOptions defaults = ProvisionerOptions.from(this);
        ProvisionerJatt.initialize(
            this,
            new ProvisionerOptions(
                PairingDialogColors.from(this),
                R.drawable.logo_provisioner,
                null,
                defaults.getShowProvisionerDialog(),
                defaults.getEnableVibrationFeedback(),
                defaults.getEnableConfirmation(),
                defaults.getEnableToastAlerts(),
                defaults.getEnableSingleModeAutomationDialog(),
                defaults.getEnableRememberAndReconnect(),
                defaults.getEnableReconnectProgressToast()
            )
        );
    }
}
