# FeasyWiFiLibrary 使用文档

> 适用版本：**2.0.3.6**  
> 平台：Android（`minSdk 28`）  
> 原生库：`libFeasyWiFiLibrary.so`（`arm64-v8a` / `armeabi-v7a`）

FeasyWiFiLibrary 是飞易通 WiFi 模块的 Android 端 SDK，封装了 **BLE 连接与 AT 指令交互、WiFi 配网、云端（MQTT）参数配置、OTA 升级、TCP 透传**以及 PIN 加密校验能力。应用层只需面对一个单例入口 `FscBleCentralBwApiBwImp` 和若干回调接口。

---

## 1. 环境要求

| 项 | 要求 |
| --- | --- |
| `minSdk` | 28（Android 9） |
| `compileSdk` / `targetSdk` | 35（推荐） |
| JDK | 17 |
| ABI | `arm64-v8a`、`armeabi-v7a` |
| Kotlin | 1.9.20（使用 TCP API 时需要） |
| 额外依赖 | `org.wso2.orbit.io.netty:netty-all`（仅 TCP 透传功能需要） |

> 使用 AGP 8.5.1 及以上版本构建时，SDK 内的 `.so` 会自动按 16 KB 页对齐打包，满足 Google Play 对 Android 15+ 的要求。

---

## 2. 引入 SDK

### 2.1 放入 AAR

把 `FeasyWiFiLibrary-2.0.3.6.aar` 复制到应用模块的 `libs/` 目录：

```text
app/libs/FeasyWiFiLibrary-2.0.3.6.aar
```

### 2.2 配置 build.gradle

```gradle
android {
    defaultConfig {
        minSdk 28
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a"
        }
    }
    // .so 不压缩，配合 16 KB 对齐
    packagingOptions {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
    // 仅使用 TCP 透传功能时需要
    implementation 'org.wso2.orbit.io.netty:netty-all:4.0.39.wso2v1'
}
```

### 2.3 声明权限

SDK 的 manifest 只声明了 `BLUETOOTH`、`BLUETOOTH_ADMIN`，**运行时权限需要宿主 App 自行申请**。推荐声明：

```xml
<!-- Android 11 及以下 -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Android 12 及以上 -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- 配网 / OTA / TCP 相关 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

| 权限 | 用途 |
| --- | --- |
| `BLUETOOTH_SCAN` | Android 12+ 扫描 BLE 设备 |
| `BLUETOOTH_CONNECT` | Android 12+ 连接设备、读写特征值 |
| `ACCESS_FINE_LOCATION` | Android 11 及以下扫描 BLE 必需 |
| `INTERNET` / `ACCESS_WIFI_STATE` | 配网、OTA、TCP 透传 |

---

## 3. 快速开始

### 3.1 初始化

```kotlin
val api = FscBleCentralBwApiBwImp.getInstance(applicationContext)
api.isShowLog(true)        // 打开 SDK 日志（发布版建议关闭）
api.initialize()           // 初始化蓝牙适配器
api.setCallbacks(callbacks) // 注册回调，见 3.2
```

> `getInstance()` 是全局单例；重复调用返回同一实例。`initialize()` 需要在使用连接/扫描前调用一次。

### 3.2 回调

实现 `FscBleCentralBwCallbacks`：

```kotlin
val callbacks = object : FscBleCentralBwCallbacks {
    // 扫描到设备
    override fun blePeripheralFound(device: BluetoothDeviceBwWrapper?, rssi: Int, record: ByteArray?) {
        // device.address / device.name / rssi
    }

    // 连接成功
    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
        // 可在此发送第一条 AT 指令
    }

    // 连接断开；isPinError 为 true 表示 PIN 校验失败
    override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?, isPinError: Boolean) {
    }

    // 收到设备数据：字符串 / 十六进制 / 原始字节
    override fun packetReceived(device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?) {
    }

    // 发送成功回调
    override fun packetSend(device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?) {
    }

    // 设备当前 IP
    override fun ipInformation(ip: String?) {
    }

    // 设备当前 WiFi 名称
    override fun NetInformation(apName: String?) {
    }

    // 设备版本号
    override fun versionInformation(version: String?) {
    }

    // 扫描状态变化
    override fun scanstatecallback(isscan: Boolean) {
    }
}
```

### 3.3 扫描

```kotlin
api.startScan(10_000L)   // 扫描 10 秒；传 0 表示一直扫描直到 stopScan()
api.stopScan()
api.isBtEnabled          // 蓝牙是否开启
api.enabled()            // 主动请求打开蓝牙
```

扫描结果通过 `blePeripheralFound(device, rssi, record)` 回调，`device` 为 `BluetoothDeviceBwWrapper`：

| 方法 | 说明 |
| --- | --- |
| `getDevice()` | 原始 `BluetoothDevice` |
| `getName()` / `getAddress()` / `getRssi()` | 名称 / MAC / 信号强度 |
| `getBeacon()` / `getLocationName()` | 扩展信息（如有） |

### 3.4 连接与断开

```kotlin
api.connect(address)            // 普通连接
api.connect(address, "000000")  // 带 PIN 连接（PIN 默认 000000）
api.isConnect                   // 当前是否已连接
api.disconnect()
```

- PIN 用于设备鉴权：SDK 会用 `MAC + PIN` 派生密钥并自动与设备完成校验；
- 校验失败时 `blePeripheralDisconnected(..., isPinError = true)` 回调；
- 连接状态以 `blePeripheralConnected` / `blePeripheralDisconnected` 回调为准，不要以 `connect()` 的返回值为准。

### 3.5 收发数据

```kotlin
// 发送：返回是否成功入队
api.send("AT+PROFILE=1\r\n".toByteArray(Charsets.UTF_8))
```

收到的数据在 `packetReceived(...)` 中给出三种形式：

| 参数 | 说明 |
| --- | --- |
| `strValue` | UTF-8 字符串（适合 AT 文本） |
| `hexString` | 十六进制字符串（适合二进制） |
| `rawValue` | 原始字节数组 |

---

## 4. 设备控制指令

| 方法 | 说明 |
| --- | --- |
| `getMac()` | 读取设备 MAC |
| `getVer()` / `getVersion()` | 读取固件版本 |
| `getIp()` | 读取设备当前 IP（异步回调 `ipInformation`） |
| `getNetName()` | 读取当前 WiFi 名称（异步回调 `NetInformation`） |
| `getDhcp()` / `setDhcp(boolean)` | 查询 / 设置 DHCP 开关 |
| `setIp(String)` / `setGw(String)` / `setMask(String)` / `setDns(String)` | 设置静态 IP / 网关 / 掩码 / DNS |
| `reset()` / `setReboot()` | 恢复出厂 / 重启设备 |

### 4.1 WiFi 配网

```kotlin
api.setNetwork(ssid, password, object : FscNetworkCentralBwCallbacks {
    override fun success(ip: String?) {
        // 配网成功，ip 为设备获取到的地址
    }

    override fun failure() {
        // 配网失败
    }
})
```

| 失败码（`BwCommont`） | 含义 |
| --- | --- |
| `PORT_OCCUPATION` = 4 | 端口被占用 |
| `FAILED_TO_CONFIGURE_NETWORK` = 5 | 配网失败 |

模块型号常量：`BwCommont.BW_226`、`BW_236`、`BW_246`。

### 4.2 MQTT / 云端参数

```kotlin
api.setBroker("broker.example.com")
api.setPort("1883")
api.setTopic("device/topic")
api.setCert(certString)   // 双向认证证书（如需要）
api.setKey(keyString)     // 私钥（如需要）
```

### 4.3 OTA 升级

```kotlin
api.startOTA(otaFileName, object : FscOtaCentralBwCallbacks {
    override fun onOtaStartConfig() { /* 需要先配网 */ }
    override fun onOtaNetworkIsNotConfigured() { /* 设备未联网 */ }
    override fun onOtaProgress(progress: String?) { /* 进度，如 "45" */ }
    override fun onOtaSuccess() { }
    override fun onOtaFailure() { }
})
```

> OTA 前设备需要已配网成功，否则会回调 `onOtaNetworkIsNotConfigured()`。

---

## 5. TCP 透传（可选）

SDK 内置基于 Netty 的 TCP 客户端 / 服务端，需额外依赖 `org.wso2.orbit.io.netty:netty-all`。

### 5.1 客户端

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

### 5.2 服务端

```kotlin
val server = FscServerBwBwApiImp()
server.mCallback = object : FscServerBwBwCallback() { /* 同上 */ }
server.connect(8080)
server.send(byteArrayOf(0x03))
server.disconnect()
```

| 回调方法 | 说明 |
| --- | --- |
| `connected()` | 连接建立 |
| `disconnected()` | 连接断开 |
| `packetReceived(...)` | 收到数据 |
| `readResponse(...)` | 读取响应 |

---

## 6. 加密与校验

SDK 内置 PIN 派生密钥与校验能力，`connect(address, pin)` 内部已完成，无需手动调用；如需单独使用：

```kotlin
val encryptApi = FscEncryptApiImp.getInstance()

// 通用随机密钥
val payload: ByteArray = encryptApi.getEncrypted()

// 基于 MAC + PIN 派生（信标模式）
val beaconPayload: ByteArray = encryptApi.getBeaconEncrypted(address, pin)

// 校验设备返回的数据
val ok: Boolean = encryptApi.verifyEncrypted(rawValue)
```

相关原生类：

| 类 | 方法 | 说明 |
| --- | --- | --- |
| `EncryptInfo` | `create(String, String, String)`、`createRandom(String)` | 构造密钥信息（native） |
| `FACP` | `cacChecksum(byte[], int)` | 校验和计算（native） |
| `EncryptedUtil` | `covAssicAndByte(...)`、`hexToByte(...)` | 字节/十六进制转换 |
| `FileUtil` | `sdkVersion` | 当前 SDK 版本（`BuildConfig.VERSION_NAME`） |

> 上述 native 方法由 `libFeasyWiFiLibrary.so` 提供，AAR 已包含两个 ABI 的 `.so`。

---

## 7. 常见问题

### 7.1 `UnsatisfiedLinkError: dlopen failed: library "libFeasyWiFiLibrary.so" not found`

- 确认 AAR 已正确引入（`implementation fileTree(dir: "libs", include: ["*.aar"])`）；
- 确认 `abiFilters` 包含 `arm64-v8a` 或 `armeabi-v7a`；
- 打包后用 `unzip -l app.apk | grep libFeasyWiFiLibrary` 确认 `.so` 已进包。

### 7.2 Android 12+ 扫描不到设备

Android 12 起 `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` 是**运行时权限**，且 `BLUETOOTH_SCAN` 需要声明 `android:usesPermissionFlags="neverForLocation"` 才能不依赖定位权限；如果保留定位语义，则仍需 `ACCESS_FINE_LOCATION`。

### 7.3 16 KB 页对齐（Android 15+ / Google Play）

- 要求 `.so` 的 ELF `p_align = 0x4000`，且 APK 内 `.so` 不压缩、按 16 KB 对齐；
- 使用 **AGP ≥ 8.5.1** 并在 `packagingOptions.jniLibs` 中设置 `useLegacyPackaging = false`；
- 校验：`zipalign -c -P 16 -v 4 app.apk`，期望 `lib/arm64-v8a/libFeasyWiFiLibrary.so (OK)` 与 `Verification succesful`。

### 7.4 TCP 功能编译/运行报 Netty 相关错误

TCP 透传依赖 Netty，需显式引入 `org.wso2.orbit.io.netty:netty-all:4.0.39.wso2v1`；不使用 TCP 功能可不加。

### 7.5 PIN 校验失败

- 确认 PIN 为 6 位数字（默认 `000000`）；
- PIN 错误时 `blePeripheralDisconnected(..., isPinError = true)`，设备侧会在校验失败后断开。

### 7.6 ProGuard / R8

SDK 内部已使用 `@Keep` 标注对外 API；宿主混淆时建议保留：

```proguard
-keep class com.feasycom.** { *; }
-keep interface com.feasycom.** { *; }
```

---

## 8. 版本记录

| 版本 | 说明 |
| --- | --- |
| 2.0.3.6 | 当前版本；16 KB 页对齐；配网、MQTT、OTA、TCP 透传 |

---

## 9. English

English documentation: [FeasyWiFiLibrary_Integration_Guide_EN.md](FeasyWiFiLibrary_Integration_Guide_EN.md)
