package com.beastblocks.provisionerjatt.example_flutter

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.beastblocks.provisionerjattsdk.ProvisionerJatt
import com.beastblocks.provisionerjattsdk.ProvisionerJattFrp
import com.beastblocks.provisionerjattsdk.domain.DeviceId
import com.beastblocks.provisionerjattsdk.domain.DpcComponent
import com.beastblocks.provisionerjattsdk.domain.ProvisioningStage
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState
import com.beastblocks.provisionerjattsdk.ui.ProvisionerUiState
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : FlutterFragmentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stateSink: EventChannel.EventSink? = null
    private var stateJob: Job? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val messenger = flutterEngine.dartExecutor.binaryMessenger
        MethodChannel(messenger, CHANNEL).setMethodCallHandler { call, result ->
            handle(call, result)
        }
        EventChannel(messenger, STATE).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                stateSink = events
                val client = ProvisionerJatt.get()
                stateJob?.cancel()
                stateJob = lifecycleScope.launch {
                    client.state.collect { state ->
                        val payload = state.toPayload()
                        mainHandler.post { stateSink?.success(payload) }
                    }
                }
            }

            override fun onCancel(arguments: Any?) {
                stateJob?.cancel()
                stateSink = null
            }
        })
    }

    override fun onDestroy() {
        if (isFinishing) {
            runCatching { ProvisionerJatt.get().detach(this) }
        }
        stateJob?.cancel()
        super.onDestroy()
    }

    private fun handle(call: MethodCall, result: MethodChannel.Result) {
        val client = ProvisionerJatt.get()
        when (call.method) {
            "singleMode" -> {
                client.detach(this)
                client.clearAutomationAndSerial()
                client.scanThenAttach(this, this)
                result.success(null)
            }
            "singleModeAutomation" -> {
                client.detach(this)
                client.clearAutomationAndSerial()
                client.scanThenAutomateThenAttach(
                    this,
                    this,
                    TEST_DPC_PACKAGE,
                    TEST_DPC_URL,
                )
                result.success(null)
            }
            "setAccountFrp" -> {
                var delivered = false
                ProvisionerJattFrp.addFRPAccount(
                    this,
                    { frp ->
                        if (delivered) return@addFRPAccount
                        delivered = true
                        val message = if (frp.success) {
                            "name: ${frp.name.orEmpty()}\nemail: ${frp.email.orEmpty()}\nfrpToken: ${frp.frpToken.orEmpty()}"
                        } else {
                            frp.reason.orEmpty()
                        }
                        mainHandler.post {
                            if (!licenseNotice(message)) {
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            }
                            result.success(message)
                        }
                    },
                    "",
                )
            }
            "setOrganizationName" -> {
                val org = getString(R.string.app_name)
                val set = ProvisionerJattFrp.setOrganizationName(org, this)
                val message = if (set.success) "Organization name set to $org" else set.reason.orEmpty()
                if (!licenseNotice(message)) Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                result.success(message)
            }
            "clearAutomationAndSerial" -> result.success(client.clearAutomationAndSerial())
            "attach" -> {
                client.attach(this, this)
                result.success(null)
            }
            "detach" -> {
                client.detach(this)
                result.success(null)
            }
            "openQr" -> result.success(client.openQRToScan())
            "openPairing" -> result.success(client.openPairingDialog())
            "setAutomation" -> {
                val packageName = call.argument<String>("packageName").orEmpty()
                val apkUrl = call.argument<String>("apkUrl").orEmpty()
                val ok = if (packageName.isBlank() && apkUrl.isBlank()) {
                    true
                } else {
                    client.setAutomation(packageName, apkUrl)
                }
                result.success(ok)
            }
            "setSerial" -> {
                val serial = call.argument<String>("serial").orEmpty()
                val ok = if (serial.isBlank()) client.clearSerial() else client.setSerial(serial)
                result.success(ok)
            }
            "clearAutomation" -> result.success(client.clearAutomation())
            "clearSerial" -> result.success(client.clearSerial())
            "scan" -> {
                client.scan(DeviceId(call.requireId()))
                result.success(null)
            }
            "makeOwner" -> {
                client.makeDeviceOwner(DeviceId(call.requireId()), call.component())
                result.success(null)
            }
            "removeOwner" -> {
                client.removeOwner(DeviceId(call.requireId()), call.component())
                result.success(null)
            }
            "retryProvisioning" -> {
                client.retryProvisioning(DeviceId(call.requireId()))
                result.success(null)
            }
            "disconnectWireless" -> {
                client.disconnectWireless(DeviceId(call.requireId()))
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun MethodCall.requireId(): String =
        argument<String>("id") ?: error("device id")

    private fun licenseNotice(message: String): Boolean =
        message.isEmpty() ||
            message == "SDK is not available" ||
            message == "Debug trials has been finished for today and will continue tomorrow"

    private fun MethodCall.component(): DpcComponent = DpcComponent(
        packageName = argument<String>("packageName").orEmpty(),
        receiverClass = argument<String>("receiverClass").orEmpty(),
        testOnly = argument<Boolean>("testOnly") == true,
    )

    private companion object {
        const val CHANNEL = "com.beastblocks.provisionerjatt/sdk"
        const val STATE = "com.beastblocks.provisionerjatt/state"
        const val TEST_DPC_PACKAGE = "com.afwsamples.testdpc"
        const val TEST_DPC_URL = "https://uatapi.aopay.co.in/api/V1/AopayFinance/download-DPC"
    }
}

private fun ProvisionerUiState.toPayload(): Map<String, Any?> = mapOf(
    "scanning" to scanning,
    "settings" to mapOf(
        "packageName" to settings.packageName,
        "apkUrl" to settings.apkUrl,
        "serialNumber" to settings.serialNumber,
        "automationConfigured" to settings.automationConfigured,
        "hasSerialFilter" to settings.hasSerialFilter,
    ),
    "devices" to devices.map { it.toPayload() },
)

private fun DeviceUiState.toPayload(): Map<String, Any?> = mapOf(
    "id" to target.id.value,
    "name" to target.displayName,
    "transport" to target.transport.name,
    "serial" to target.serial,
    "connection" to connection.name,
    "message" to message,
    "previouslyPaired" to previouslyPaired,
    "manual" to mapOf(
        "scanning" to manual.scanning,
        "busy" to (manual.actionInProgress != null),
        "result" to manual.result,
        "components" to manual.components.map { component ->
            mapOf(
                "packageName" to component.packageName,
                "receiverClass" to component.receiverClass,
                "testOnly" to component.testOnly,
                "flattened" to component.flattened,
            )
        },
    ),
    "stage" to automatedStage.toPayload(),
)

private fun ProvisioningStage.toPayload(): Map<String, Any?> = when (this) {
    ProvisioningStage.Idle -> mapOf("kind" to "idle")
    ProvisioningStage.Authenticating -> mapOf("kind" to "authenticating")
    ProvisioningStage.Scanning -> mapOf("kind" to "scanning")
    ProvisioningStage.Downloading -> mapOf("kind" to "downloading")
    ProvisioningStage.Pushing -> mapOf("kind" to "pushing")
    ProvisioningStage.Installing -> mapOf("kind" to "installing")
    ProvisioningStage.Updating -> mapOf("kind" to "updating")
    ProvisioningStage.SettingOwner -> mapOf("kind" to "settingOwner")
    ProvisioningStage.Launching -> mapOf("kind" to "launching")
    is ProvisioningStage.Retrying -> mapOf("kind" to "retrying", "reason" to reason)
    is ProvisioningStage.Succeeded -> mapOf(
        "kind" to "succeeded",
        "flattened" to component.flattened,
    )
    is ProvisioningStage.OwnerRemoved -> mapOf(
        "kind" to "ownerRemoved",
        "flattened" to component.flattened,
    )
    is ProvisioningStage.Failed -> mapOf("kind" to "failed", "reason" to reason)
}
