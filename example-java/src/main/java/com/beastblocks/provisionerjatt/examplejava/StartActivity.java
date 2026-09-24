package com.beastblocks.provisionerjatt.examplejava;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import com.beastblocks.provisionerjattsdk.ProvisionerClient;
import com.beastblocks.provisionerjattsdk.ProvisionerJatt;

public class StartActivity extends ComponentActivity {
    static final String TEST_DPC_PACKAGE = "com.afwsamples.testdpc";
    static final String TEST_DPC_URL =
        "https://uatapi.aopay.co.in/api/V1/AopayFinance/download-DPC";

    private ProvisionerClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        client = ProvisionerJatt.get();
        setContentView(R.layout.activity_start);
        findViewById(R.id.btn_single_mode).setOnClickListener(v -> startSingleMode(false));
        findViewById(R.id.btn_single_mode_automation).setOnClickListener(v -> startSingleMode(true));
        findViewById(R.id.btn_clear_and_open_main).setOnClickListener(v -> {
            client.clearAutomationAndSerial();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void startSingleMode(boolean automation) {
        client.detach(this);
        client.clearAutomationAndSerial();
        if (automation) {
            client.scanThenAutomateThenAttach(this, this, TEST_DPC_PACKAGE, TEST_DPC_URL);
        } else {
            client.scanThenAttach(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.detach(this);
        }
        super.onDestroy();
    }
}
