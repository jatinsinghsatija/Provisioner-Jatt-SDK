package com.beastblocks.provisionerjatt.example

import android.content.Intent
import android.os.Bundle
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
                    onClearAndOpenMain = {
                        client.clearAutomationAndSerial()
                        openMain(scanThenAttach = false)
                    },
                )
            }
        }
    }

    private fun startSingleMode(automation: Boolean) {
        client.detach(this)
        client.clearAutomationAndSerial()
        if (automation) {
            client.scanThenAutomateThenAttach(this, this, TEST_DPC_PACKAGE, TEST_DPC_URL)
        } else {
            client.scanThenAttach(this, this)
        }
    }

    private fun openMain(scanThenAttach: Boolean) {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_SCAN_THEN_ATTACH, scanThenAttach),
        )
        finish()
    }

    companion object {
        const val EXTRA_SCAN_THEN_ATTACH = "scan_then_attach"
        const val TEST_DPC_PACKAGE = "com.afwsamples.testdpc"
        const val TEST_DPC_URL =
            "https://uatapi.aopay.co.in/api/V1/AopayFinance/download-DPC"
    }
}

@Composable
private fun StartScreen(
    onSingleMode: () -> Unit,
    onSingleModeAutomation: () -> Unit,
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
            onClick = onClearAndOpenMain,
            accent = false,
            modifier = Modifier.fillMaxWidth().testTag("clear_and_open_main"),
        ) {
            Text(stringResource(R.string.clear_automation_and_serial))
        }
    }
}
