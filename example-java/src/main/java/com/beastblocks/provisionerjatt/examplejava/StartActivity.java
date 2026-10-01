package com.beastblocks.provisionerjatt.examplejava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import com.beastblocks.provisionerjattsdk.FrpSetResult;
import com.beastblocks.provisionerjattsdk.ProvisionerClient;
import com.beastblocks.provisionerjattsdk.ProvisionerJatt;
import com.beastblocks.provisionerjattsdk.ProvisionerJattFrp;

public class StartActivity extends ComponentActivity {
    private ProvisionerClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        client = ProvisionerJatt.get();
        setContentView(R.layout.activity_start);
        findViewById(R.id.btn_single_mode).setOnClickListener(v -> startSingleMode(false));
        findViewById(R.id.btn_single_mode_automation).setOnClickListener(v -> startSingleMode(true));
        findViewById(R.id.btn_set_account_frp).setOnClickListener(v -> setAccountFrp());
        findViewById(R.id.btn_set_organization_name).setOnClickListener(v -> setOrganizationName());
        findViewById(R.id.btn_clear_and_open_main).setOnClickListener(v -> {
            client.clearAutomationAndSerial();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void setAccountFrp() {
        ProvisionerJattFrp.addFRPAccount(this, result -> {
            String message;
            if (result.getSuccess()) {
                message = getString(
                    R.string.frp_account_success,
                    result.getName() == null ? "" : result.getName(),
                    result.getEmail() == null ? "" : result.getEmail(),
                    result.getFrpToken() == null ? "" : result.getFrpToken()
                );
            } else {
                message = getString(
                    R.string.frp_account_failed,
                    result.getReason() == null ? "" : result.getReason()
                );
            }
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        }, getString(R.string.default_web_client_id));
    }

    private void setOrganizationName() {
        FrpSetResult result =
            ProvisionerJattFrp.setOrganizationName(getString(R.string.app_name), this);
        String message;
        if (result.getSuccess()) {
            message = getString(R.string.org_name_success, getString(R.string.app_name));
        } else {
            message = getString(
                R.string.org_name_failed,
                result.getReason() == null ? "" : result.getReason()
            );
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void startSingleMode(boolean automation) {
        client.detach(this);
        client.clearAutomationAndSerial();
        if (automation) {
            client.scanThenAutomateThenAttach(
                this,
                this,
                getString(R.string.test_dpc_package),
                getString(R.string.test_dpc_url)
            );
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
