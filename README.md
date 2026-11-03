# FeasyWiFi Android App（SDK + Demo）

FeasyWiFi 是飞易通 WiFi 模块的配置与配网 App。蓝牙/WiFi 协议与设备操作能力由 **FeasyWiFiLibrary** 提供；本仓库**不包含 FeasyWiFiLibrary 源码**，只以预编译 AAR 形式放在应用模块：

```text
feasywifi/libs/FeasyWiFiLibrary-2.0.3.6.aar
```

> FeasyWiFi Android app source. The BLE/WiFi layer is provided by FeasyWiFiLibrary as a prebuilt AAR. The SDK source is not included in this repository.

---

## 文档 / Documentation

- 中文使用文档：[docs/FeasyWiFiLibrary使用文档.md](docs/FeasyWiFiLibrary使用文档.md)
- English integration guide: [docs/FeasyWiFiLibrary_Integration_Guide_EN.md](docs/FeasyWiFiLibrary_Integration_Guide_EN.md)

---

## 下载安装包 / Download APK

| 版本 | 文件 | 大小 | SHA-256 |
| --- | --- | --- | --- |
| `2.0.3.6` | [apk/FeasyWifi2.0.3.6_2026-11-03.apk](apk/FeasyWifi2.0.3.6_2026-11-03.apk) | 15,858,316 B（约 15.12 MiB） | `C3A8B64F7888F6FC0F9096BDD7FAF7379258D1CCC71CD66D49D04130BE5D1E7F` |

- 适用系统：Android 9 及以上（`minSdk 28`）；
- APK 为示例 App 的构建产物，正式发布前请核对签名与版本。

---

## 1. 引入 SDK / Add the SDK

### 1.1 仓库内的 SDK 文件

```text
feasywifi/libs/FeasyWiFiLibrary-2.0.3.6.aar   （预编译 SDK，含 arm64-v8a / armeabi-v7a 原生库）
```

| 文件 | 大小 | SHA-256 |
| --- | --- | --- |
| `FeasyWiFiLibrary-2.0.3.6.aar` | 146,529 B（约 143.1 KiB） | `A3F81899253EC1482005D1CA36506C7780BA2F1E2F61612505D9238E32656FA1` |

### 1.2 依赖方式

```gradle
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar", "*.jar"])
}
```

```gradle
android {
    defaultConfig {
        ndk { abiFilters "armeabi-v7a", "arm64-v8a" }
    }
}
```

---

## 2. 构建环境 / Build environment

| 项 | 版本 |
| --- | --- |
| AGP | 8.10.1 |
| Gradle | 8.11.1 |
| Kotlin | 1.9.20 |
| JDK | 17 |
| compileSdk / targetSdk | 35 / 35 |
| minSdk | 28 |

```bash
./gradlew :feasywifi:assembleRelease
```

---

## 3. 16 KB page size 支持 / 16 KB page size

Android 15 起支持 16 KB 内存页，Google Play 要求目标 API 35+ 的应用满足：

- `.so` 的 ELF `p_align` 为 `0x4000`；
- APK 内 `.so` **不压缩**且按 **16 KB** 对齐（`useLegacyPackaging = false` + `zipalign -P 16`）。

本仓库使用的 AGP 8.10.1 会自动完成 16 KB 对齐，可自行校验：

```bash
zipalign -c -P 16 -v 4 apk/FeasyWifi2.0.3.6_2026-11-03.apk
# 期望：lib/arm64-v8a/libFeasyWiFiLibrary.so (OK)
#       Verification succesful
```

---

## 4. 版本 / Version

- App：`2.0.3.6`（versionCode 2036）
- FeasyWiFiLibrary：`2.0.3.6`
