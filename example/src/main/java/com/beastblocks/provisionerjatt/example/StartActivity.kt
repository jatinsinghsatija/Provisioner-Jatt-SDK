package com.beastblocks.provisionerjatt.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beastblocks.provisionerjattsdk.ProvisionerClient
import com.beastblocks.provisionerjattsdk.ProvisionerJatt
import com.beastblocks.provisionerjattsdk.ProvisionerJattFrp
import com.beastblocks.provisionerjattsdk.ui.NeumorphicButton
import com.beastblocks.provisionerjattsdk.ui.NeumorphicTheme
import com.beastblocks.provisionerjattsdk.ui.ProvisionerJattTheme

class StartActivity : ComponentActivity() {
    private lateinit var client: ProvisionerClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        client = ProvisionerJatt.get()
        setContent {
            ProvisionerJattTheme {
                StartScreen(
                    onSingleMode = { startSingleMode(automation = false) },
                    onSingleModeAutomation = { startSingleMode(automation = true) },
                    onSetAccountFrp = { setAccountFrp() },
                    onSetOrganizationName = { setOrganizationName() },
                    onClearAndOpenMain = {
                        client.clearAutomationAndSerial()
                        openMain()
                    },
                )
            }
        }
    }

    private fun setAccountFrp() {
        ProvisionerJattFrp.addFRPAccount(
            this,
            { result ->
                val message = if (result.success) {
                    getString(
                        R.string.frp_account_success,
                        result.name.orEmpty(),
                        result.email.orEmpty(),
                        result.frpToken.orEmpty(),
                    )
                } else {
                    getString(R.string.frp_account_failed, result.reason.orEmpty())
                }
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            },
            getString(R.string.default_web_client_id),
        )
    }

    private fun setOrganizationName() {
        val result = ProvisionerJattFrp.setOrganizationName(getString(R.string.app_name), this)
        val message = if (result.success) {
            getString(R.string.org_name_success, getString(R.string.app_name))
        } else {
            getString(R.string.org_name_failed, result.reason.orEmpty())
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun startSingleMode(automation: Boolean) {
        if (::client.isInitialized) client.detach(this)
        client.clearAutomationAndSerial()
        if (automation) {
            client.scanThenAutomateThenAttach(
                this,
                this,
                getString(R.string.test_dpc_package),
                getString(R.string.test_dpc_url),
            )
        } else {
            client.scanThenAttach(this, this)
        }
    }

    private fun openMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
        finish()
    }
}

@Composable
private fun StartScreen(
    onSingleMode: () -> Unit,
    onSingleModeAutomation: () -> Unit,
    onSetAccountFrp: () -> Unit,
    onSetOrganizationName: () -> Unit,
    onClearAndOpenMain: () -> Unit,
) {
    val colors = NeumorphicTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 32.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_provisioner),
            contentDescription = null,
            modifier = Modifier.size(88.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        NeumorphicButton(
            onClick = onSingleMode,
            modifier = Modifier.fillMaxWidth().testTag("start_single_mode"),
        ) {
            Text(stringResource(R.string.start_single_mode))
        }
        NeumorphicButton(
            onClick = onSingleModeAutomation,
            modifier = Modifier.fillMaxWidth().testTag("start_single_mode_automation"),
        ) {
            Text(stringResource(R.string.start_single_mode_automation))
        }
        NeumorphicButton(
            onClick = onSetAccountFrp,
            modifier = Modifier.fillMaxWidth().testTag("set_account_frp"),
        ) {
            Text(stringResource(R.string.set_account_frp))
        }
        NeumorphicButton(
            onClick = onSetOrganizationName,
            modifier = Modifier.fillMaxWidth().testTag("set_organization_name"),
        ) {
            Text(stringResource(R.string.set_organization_name))
        }
        NeumorphicButton(
            onClick = onClearAndOpenMain,
            accent = false,
            modifier = Modifier.fillMaxWidth().testTag("clear_and_open_main"),
        ) {
            Text(stringResource(R.string.clear_automation_and_serial))
        }
    }
}
