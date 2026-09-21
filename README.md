<p align="center">
  <img src="example/src/main/res/drawable/logo_provisioner.png" alt="Provisioner Jatt SDK" width="320"/>
</p>

<p align="center">
  <strong>PROVISIONER JATT SDK</strong><br/>
  <em>USB + wireless ADB provisioning, packed as one drop-in AAR.</em>
</p>

<p align="center">
  <img alt="Version 1.0.0" src="https://img.shields.io/badge/version-1.0.0-FF8A00?style=for-the-badge&labelColor=000000"/>
  <img alt="Min SDK 26" src="https://img.shields.io/badge/minSdk-26-FFCC00?style=for-the-badge&labelColor=000000"/>
  <img alt="Package" src="https://img.shields.io/badge/package-com.beastblocks.provisionerjattsdk-white?style=for-the-badge&labelColor=000000"/>
</p>

<p align="center">
  Plug a device in. Pair over Wi-Fi. Push a DPC.<br/>
  Your app keeps its screens. The SDK owns the hard parts.
</p>

---

## Artifact

| | |
| --- | --- |
| File | `provisioner-jatt-sdk-1.0.0.aar` |
| Version | **1.0.0** |
| Namespace | `com.beastblocks.provisionerjattsdk` |
| Entry point | `ProvisionerJatt` |

This folder is the distribution kit: the AAR, this guide, and the brand mark.

---

## What the SDK already does for you

You do **not** implement these in the host app:

| Built in | You never write |
| --- | --- |
| USB ADB + Android 11+ wireless TLS ADB | Connection state machines |
| `UsbAttachedReceiver` + device filter | `USB_DEVICE_ATTACHED` on your Activity |
| USB permission + nearby Wi-Fi / location prompts | Permission plumbing |
| Six-digit wireless pairing overlay | Pairing Activity |
| Auto-connect USB and remembered wireless | Reconnect loops |
| Serial lock, scan, make owner, automate DPC | ADB shell scripts |

Your job is three verbs: **drop the AAR → `initialize` → `attach`.**

```mermaid
flowchart LR
  A[Application.onCreate] -->|initialize| B[SDK ready]
  B --> C[Host Activity]
  C -->|attach| D[USB / Wi-Fi / pairing live]
  C -->|detach| E[This screen stops]
  E -->|last host gone| F[Session reset]
```

---

# Integration — six beats

### 1. Drop the AAR

Copy `provisioner-jatt-sdk-1.0.0.aar` into your app module:

```
app/
  libs/
    provisioner-jatt-sdk-1.0.0.aar
```

Optional: copy `example/src/main/res/drawable/logo_provisioner.png` into your `app/src/main/res/drawable/` as `logo_provisioner.png` if you want it on the pairing dialog.

---

### 2. Unlock the repositories

The AAR is a binary. Gradle still needs Google, Maven Central, and JitPack so its companion libraries can resolve (Compose, Firebase Database, SPAKE2, Conscrypt).

`settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Do **not** add `google-services.json` or the Google Services plugin for this SDK. Firebase is initialized inside the AAR.

---

### 3. Depend on the AAR + its companions

`app/build.gradle.kts`

```kotlin
android {
    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(files("libs/provisioner-jatt-sdk-1.0.0.aar"))

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    implementation("com.google.dagger:dagger:2.52")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.github.MuntashirAkon.spake2-java:spake2-android:2.2.1")
    implementation("org.conscrypt:conscrypt-android:2.5.3")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.81")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.81")
}
```

A raw AAR has no Maven POM. Those companions must be declared on the host, or the app will crash when USB, pairing, or the access check runs.

`INTERNET`, USB host, and nearby-network permissions **merge from the AAR**. Do not put `USB_DEVICE_ATTACHED` on your Activity — the library receiver owns that filter.

---

### 4. Wake the SDK in `Application`

```kotlin
import com.beastblocks.provisionerjattsdk.ProvisionerJatt
import com.beastblocks.provisionerjattsdk.ProvisionerOptions

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ProvisionerJatt.initialize(
            this,
            ProvisionerOptions(
                pairingWatermarkResId = R.drawable.logo_provisioner, // omit for none
            ),
        )
    }
}
```

`AndroidManifest.xml`

```xml
<application
    android:name=".App"
    ... >
```

`initialize` stores options and starts the engine. It does **not** scan, prompt, or pair until a screen calls `attach`.

On first launch the SDK registers your host `applicationId` for access. If that package is later set to `false`, every feature stops and the user sees:

> This app has been revoked from using Provisioner Jat SDK.

---

### 5. Attach every provisioning screen

Pairing, permissions, and auto-connect run only on Activities you attach. Use the same window the user is looking at — the SDK never starts its own Activity.

```kotlin
import com.beastblocks.provisionerjattsdk.ProvisionerClient
import com.beastblocks.provisionerjattsdk.ProvisionerJatt

class ProvisionerActivity : ComponentActivity() {
    private lateinit var client: ProvisionerClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = ProvisionerJatt.attach(this)
        // optional: collect discovered / connected lists
    }

    override fun onDestroy() {
        if (::client.isInitialized) client.detach(this)
        super.onDestroy()
    }
}
```

| Call | When |
| --- | --- |
| `attach(activity)` | `onCreate` of a host screen |
| `detach(activity)` | `onDestroy` of that same screen |
| `resetSession()` | Drop live ADB and start clean without leaving |
| `isSessionActive()` | Whether a host is currently live |

Switching between attached screens does **not** re-`initialize`. Detach removes only that screen. When the last host is gone, the live session resets.

After `initialize` / `attach`, anywhere on a host screen:

```kotlin
val client = ProvisionerJatt.get()
```

---

### 6. Automate — or just listen

**Hands-off provisioning** (package + HTTPS APK URL required, serial optional):

```kotlin
client.setAutomation(
    packageName = "com.example.dpc",
    downloadUrl = "https://example.com/dpc.apk",
    serialNumber = null, // or "SERIAL"
)
```

`setAutomation` returns `false` if package or URL is missing/invalid. `clearAutomation()` turns it off.

When a serial is set, the library — not you — will list, pair, auto-connect, and disconnect everyone else to match that serial.

**Optional lists** (skip these if the host has no device UI):

```kotlin
lifecycleScope.launch {
    client.discoveredDevices.collect { /* show pending devices */ }
}
lifecycleScope.launch {
    client.connectedDevices.collect { /* show ready devices */ }
}
```

Automation and pairing still run if you never collect a single flow.

**Optional manual actions** (only if you draw device cards):

```kotlin
client.scan(deviceId)
client.makeDeviceOwner(deviceId, component)
client.removeOwner(deviceId, component)
client.retryProvisioning(deviceId)
client.disconnectWireless(deviceId)
```

---

## Pairing overlay

**Default:** the library draws the six-digit dialog on the attached host Activity. Pass `pairingWatermarkResId` for the Provisioner mark. Override `pairingColors` if you want different orange / yellow / black.

```kotlin
ProvisionerJatt.initialize(
    this,
    ProvisionerOptions(
        pairingWatermarkResId = R.drawable.logo_provisioner,
        pairingColors = PairingDialogColors(
            accent = 0xFFFF8A00.toInt(),
            background = 0xFFFFFFFF.toInt(),
        ),
    ),
)
```

**Your own UI:** set `pairingCodeHandler`. The library will not show its dialog. Submit from yours:

```kotlin
session.submit("123456")
session.dismiss()
```

Or call `client.submitPairingCode("123456")` / `client.dismissPairing()`.

---

## Host checklist

- [ ] `minSdk` 26+
- [ ] AAR in `app/libs/` plus companion dependencies
- [ ] `google()`, `mavenCentral()`, `jitpack.io`
- [ ] `Application` registered, `initialize` in `onCreate`
- [ ] `attach` / `detach` on every provisioning Activity
- [ ] **No** `USB_DEVICE_ATTACHED` on your Activity
- [ ] **No** `google-services.json` required for this SDK
- [ ] Physical USB host and/or Android 11+ wireless debugging on the target device

The first USB attach still needs the user to tap **Allow** on the device. Wireless still needs Wireless debugging + the six-digit code. The SDK does not bypass ADB authorization or Android enterprise policy.

---

<p align="center">
  <img src="example/src/main/res/drawable/logo_provisioner.png" alt="Provisioner Jatt" width="96"/>
  <br/>
  <sub>Provisioner Jatt SDK · 1.0.0 · <code>com.beastblocks.provisionerjattsdk</code></sub>
</p>
