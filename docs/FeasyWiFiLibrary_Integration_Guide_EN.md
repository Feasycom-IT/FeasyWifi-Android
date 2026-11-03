# FeasyWiFiLibrary Integration Guide (English)

> Version: **2.0.3.6**
> Platform: Android (`minSdk 28`)
> Native library: `libFeasyWiFiLibrary.so` (`arm64-v8a` / `armeabi-v7a`)

FeasyWiFiLibrary is the Android SDK for Feasycom WiFi modules. It wraps **BLE connection and AT command exchange, WiFi provisioning, cloud (MQTT) settings, OTA upgrade, TCP passthrough** and PIN-based encryption/verification. The application only deals with one singleton entry point, `FscBleCentralBwApiBwImp`, plus a few callback interfaces.

---

## 1. Requirements

| Item | Requirement |
| --- | --- |
| `minSdk` | 28 (Android 9) |
| `compileSdk` / `targetSdk` | 35 (recommended) |
| JDK | 17 |
| ABI | `arm64-v8a`, `armeabi-v7a` |
| Kotlin | 1.9.20 (only needed for the TCP APIs) |
| Extra dependency | `org.wso2.orbit.io.netty:netty-all` (TCP passthrough only) |

> When building with AGP 8.5.1 or newer, the `.so` files inside the SDK are packaged 16 KB page aligned automatically, which satisfies the Google Play requirement for Android 15+.

---

## 2. Add the SDK

### 2.1 Copy the AAR

```text
app/libs/FeasyWiFiLibrary-2.0.3.6.aar
```

### 2.2 build.gradle

```gradle
android {
    defaultConfig {
        minSdk 28
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a"
        }
    }
    // keep .so uncompressed for 16 KB alignment
    packagingOptions {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
    // only required for the TCP passthrough feature
    implementation 'org.wso2.orbit.io.netty:netty-all:4.0.39.wso2v1'
}
```

### 2.3 Permissions

The SDK manifest declares only `BLUETOOTH` and `BLUETOOTH_ADMIN`; **runtime permissions must be requested by the host app**. Recommended declarations:

```xml
<!-- Android 11 and below -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Android 12 and above -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- provisioning / OTA / TCP -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

| Permission | Purpose |
| --- | --- |
| `BLUETOOTH_SCAN` | Scan BLE devices on Android 12+ |
| `BLUETOOTH_CONNECT` | Connect devices and read/write characteristics on Android 12+ |
| `ACCESS_FINE_LOCATION` | Required for BLE scanning on Android 11 and below |
| `INTERNET` / `ACCESS_WIFI_STATE` | Provisioning, OTA, TCP passthrough |

---

## 3. Quick start

### 3.1 Initialize

```kotlin
val api = FscBleCentralBwApiBwImp.getInstance(applicationContext)
api.isShowLog(true)         // enable SDK logs (disable in release builds)
api.initialize()            // initialize the Bluetooth adapter
api.setCallbacks(callbacks) // see 3.2
```

> `getInstance()` returns a process-wide singleton. Call `initialize()` once before scanning or connecting.

### 3.2 Callbacks

Implement `FscBleCentralBwCallbacks`:

```kotlin
val callbacks = object : FscBleCentralBwCallbacks {
    // a device was found while scanning
    override fun blePeripheralFound(device: BluetoothDeviceBwWrapper?, rssi: Int, record: ByteArray?) { }

    // connected
    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) { }

    // disconnected; isPinError == true means the PIN check failed
    override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?, isPinError: Boolean) { }

    // data received: string / hex string / raw bytes
    override fun packetReceived(device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?) { }

    // data sent
    override fun packetSend(device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?) { }

    override fun ipInformation(ip: String?) { }
    override fun NetInformation(apName: String?) { }
    override fun versionInformation(version: String?) { }
    override fun scanstatecallback(isscan: Boolean) { }
}
```

### 3.3 Scanning

```kotlin
api.startScan(10_000L)   // scan for 10 s; pass 0 to scan until stopScan()
api.stopScan()
api.isBtEnabled          // is Bluetooth on
api.enabled()            // ask the user to enable Bluetooth
```

Each result arrives through `blePeripheralFound(device, rssi, record)`. `device` is a `BluetoothDeviceBwWrapper`:

| Method | Description |
| --- | --- |
| `getDevice()` | underlying `BluetoothDevice` |
| `getName()` / `getAddress()` / `getRssi()` | name / MAC / RSSI |
| `getBeacon()` / `getLocationName()` | optional extended info |

### 3.4 Connect / disconnect

```kotlin
api.connect(address)            // plain connection
api.connect(address, "000000")  // connection with PIN (default PIN 000000)
api.isConnect                   // current connection state
api.disconnect()
```

- The PIN is used for device authentication: the SDK derives a key from `MAC + PIN` and completes the handshake automatically.
- On failure, `blePeripheralDisconnected(..., isPinError = true)` is called.
- Rely on the callbacks, not the return value of `connect()`.

### 3.5 Send and receive

```kotlin
api.send("AT+PROFILE=1\r\n".toByteArray(Charsets.UTF_8))
```

`packetReceived(...)` delivers the payload in three forms:

| Parameter | Description |
| --- | --- |
| `strValue` | UTF-8 string (good for AT text) |
| `hexString` | hex string (good for binary) |
| `rawValue` | raw byte array |

---

## 4. Device control

| Method | Description |
| --- | --- |
| `getMac()` | read device MAC |
| `getVer()` / `getVersion()` | read firmware version |
| `getIp()` | read current IP (async: `ipInformation`) |
| `getNetName()` | read current WiFi SSID (async: `NetInformation`) |
| `getDhcp()` / `setDhcp(boolean)` | get / set DHCP |
| `setIp(String)` / `setGw(String)` / `setMask(String)` / `setDns(String)` | static IP / gateway / netmask / DNS |
| `reset()` / `setReboot()` | factory reset / reboot |

### 4.1 WiFi provisioning

```kotlin
api.setNetwork(ssid, password, object : FscNetworkCentralBwCallbacks {
    override fun success(ip: String?) {
        // provisioning succeeded; ip is the address assigned to the device
    }

    override fun failure() {
        // provisioning failed
    }
})
```

| Error code (`BwCommont`) | Meaning |
| --- | --- |
| `PORT_OCCUPATION` = 4 | port occupied |
| `FAILED_TO_CONFIGURE_NETWORK` = 5 | provisioning failed |

Module constants: `BwCommont.BW_226`, `BW_236`, `BW_246`.

### 4.2 MQTT / cloud settings

```kotlin
api.setBroker("broker.example.com")
api.setPort("1883")
api.setTopic("device/topic")
api.setCert(certString)   // client certificate, if required
api.setKey(keyString)     // private key, if required
```

### 4.3 OTA

```kotlin
api.startOTA(otaFileName, object : FscOtaCentralBwCallbacks {
    override fun onOtaStartConfig() { /* device needs provisioning first */ }
    override fun onOtaNetworkIsNotConfigured() { /* device is offline */ }
    override fun onOtaProgress(progress: String?) { /* e.g. "45" */ }
    override fun onOtaSuccess() { }
    override fun onOtaFailure() { }
})
```

> The device must be provisioned and online before starting an OTA, otherwise `onOtaNetworkIsNotConfigured()` is called.

---

## 5. TCP passthrough (optional)

The SDK ships a Netty-based TCP client and server. Add `org.wso2.orbit.io.netty:netty-all` to use them.

### 5.1 Client

```kotlin
val client = FscClientBwBwApiImp()
client.mCallback = object : FscClientBwBwCallback() {
    override fun connected() { }
    override fun disconnected() { }
    override fun packetReceived(strValue: String?, hexString: String?, rawValue: ByteArray?, timestamp: String?) { }
    override fun readResponse(strValue: String?, hexString: String?, rawValue: ByteArray?, timestamp: String?) { }
}

client.connect(host = "192.168.1.10", servicePort = 8080, localPort = 0)
client.send(byteArrayOf(0x01, 0x02))
client.disconnect()
```

### 5.2 Server

```kotlin
val server = FscServerBwBwApiImp()
server.mCallback = object : FscServerBwBwCallback() { /* same as above */ }
server.connect(8080)
server.send(byteArrayOf(0x03))
server.disconnect()
```

| Callback | Description |
| --- | --- |
| `connected()` | connection established |
| `disconnected()` | connection closed |
| `packetReceived(...)` | data received |
| `readResponse(...)` | read response |

---

## 6. Encryption and verification

PIN-based key derivation and verification are performed inside `connect(address, pin)`; no manual call is needed. The API can also be used directly:

```kotlin
val encryptApi = FscEncryptApiImp.getInstance()

val payload: ByteArray = encryptApi.getEncrypted()                       // random key
val beaconPayload: ByteArray = encryptApi.getBeaconEncrypted(address, pin) // MAC + PIN
val ok: Boolean = encryptApi.verifyEncrypted(rawValue)                   // verify device response
```

| Class | Method | Description |
| --- | --- | --- |
| `EncryptInfo` | `create(String, String, String)`, `createRandom(String)` | build key info (native) |
| `FACP` | `cacChecksum(byte[], int)` | checksum (native) |
| `EncryptedUtil` | `covAssicAndByte(...)`, `hexToByte(...)` | byte/hex helpers |
| `FileUtil` | `sdkVersion` | current SDK version (`BuildConfig.VERSION_NAME`) |

> These native methods are provided by `libFeasyWiFiLibrary.so`, which is bundled for both ABIs in the AAR.

---

## 7. FAQ

### 7.1 `UnsatisfiedLinkError: dlopen failed: library "libFeasyWiFiLibrary.so" not found`

- make sure the AAR is added: `implementation fileTree(dir: "libs", include: ["*.aar"])`;
- make sure `abiFilters` contains `arm64-v8a` or `armeabi-v7a`;
- after building, verify with `unzip -l app.apk | grep libFeasyWiFiLibrary`.

### 7.2 Cannot find devices on Android 12+

`BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` are runtime permissions on Android 12+. Declare `BLUETOOTH_SCAN` with `android:usesPermissionFlags="neverForLocation"` if you do not need location semantics; otherwise `ACCESS_FINE_LOCATION` is still required.

### 7.3 16 KB page size (Android 15+ / Google Play)

- ELF `p_align` must be `0x4000`, and `.so` files must be stored uncompressed and 16 KB aligned in the APK;
- use **AGP ≥ 8.5.1** with `packagingOptions.jniLibs.useLegacyPackaging = false`;
- verify with `zipalign -c -P 16 -v 4 app.apk`; expect `lib/arm64-v8a/libFeasyWiFiLibrary.so (OK)` and `Verification succesful`.

### 7.4 Netty errors when using TCP

TCP passthrough requires `org.wso2.orbit.io.netty:netty-all:4.0.39.wso2v1`. Skip it if you do not use TCP.

### 7.5 PIN verification failure

- the PIN must be 6 digits (default `000000`);
- on a wrong PIN the SDK reports `blePeripheralDisconnected(..., isPinError = true)`; the device disconnects itself after the failed check.

### 7.6 ProGuard / R8

Public APIs are annotated with `@Keep`. If you obfuscate the host app, keep:

```proguard
-keep class com.feasycom.** { *; }
-keep interface com.feasycom.** { *; }
```

---

## 8. Changelog

| Version | Notes |
| --- | --- |
| 2.0.3.6 | current release; 16 KB page alignment; provisioning, MQTT, OTA, TCP passthrough |

---

## 9. Chinese

中文文档：[FeasyWiFiLibrary使用文档.md](FeasyWiFiLibrary使用文档.md)
