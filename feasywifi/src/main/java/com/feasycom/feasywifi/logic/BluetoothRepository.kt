package com.feasycom.feasywifi.logic

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BluetoothDeviceBwWrapper
import com.feasycom.ble.FscBleCentralBwApiBwImp
import com.feasycom.ble.FscBleCentralBwCallbacks
import com.feasycom.ble.FscNetworkCentralBwCallbacks
import com.feasycom.ble.FscOtaCentralBwCallbacks
import com.feasycom.feasywifi.App
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.logic.utils.getBoolean
import com.feasycom.feasywifi.logic.utils.getInt
import com.feasycom.feasywifi.logic.utils.getStr

object BluetoothRepository : FscBleCentralBwCallbacks, FscNetworkCentralBwCallbacks,
    FscOtaCentralBwCallbacks {

    private const val TAG = "BluetoothRepository"

    private var rssiValue: Int = -100
    private var nameSwitch: Boolean = false
    var isScanner: Boolean = false
    private var nameValue: String = ""
    var mGoneDNS = true
    var lostAtCommand = AtCommand.NONE
    private val mCallbacks = mutableListOf<FeasyWiFiCallbacks>()

    private const val connectTimeOutTime = 30000L
    private val mHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val mConnectTimeoutRunnable = Runnable {
        try {
            mCallbacks.forEach {
                it.onConnectTimeout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        disconnect()
    }

    private val mFscBleCentralApi by lazy {
        FscBleCentralBwApiBwImp.getInstance(App.sContext).apply {
            initialize()
            setCallbacks(this@BluetoothRepository)
        }
    }

    fun isShowLog(showLog: Boolean) = mFscBleCentralApi.isShowLog(showLog)

    fun isBtEnabled() = mFscBleCentralApi.isBtEnabled

    fun enabled() {
        mFscBleCentralApi.enabled()
    }

    fun registerViewCallback(callback: FeasyWiFiCallbacks) {
        if (mCallbacks.contains(callback)) {
            return
        }
        try {
            mCallbacks.add(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unRegisterViewCallback(callback: FeasyWiFiCallbacks) {
        try {
            mCallbacks.remove(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun startScan(time: Long = 0) {
        isScanner = true
        App.sContext?.let {
            rssiValue = it.getInt("rssiValue", -100)
            nameSwitch = it.getBoolean("nameSwitch", false)
            nameValue = it.getStr("nameValue", "")
        }
        mFscBleCentralApi.startScan(time)
    }

    fun stopScan() {
        isScanner = false
        mFscBleCentralApi.stopScan()
    }

    fun connect(address: String) {
        mHandler.postDelayed(mConnectTimeoutRunnable, connectTimeOutTime)
        mFscBleCentralApi.connect(address)
    }

    fun disconnect() {
        mFscBleCentralApi.disconnect()
    }

    fun getDhcp() {
        lostAtCommand = AtCommand.GET_DHCP
        mFscBleCentralApi.getDhcp()
    }

    fun setDhcp(b: Boolean) {
        lostAtCommand = AtCommand.SET_DHCP
        mFscBleCentralApi.setDhcp(b)
    }

    fun getIp() {
        lostAtCommand = AtCommand.GET_IP
        mFscBleCentralApi.getIp()
    }

    fun getNetName() {
        lostAtCommand = AtCommand.GET_APNAME
        mFscBleCentralApi.getNetName()
    }

    fun getVersion() {
        lostAtCommand = AtCommand.GET_VERSION
        mFscBleCentralApi.getVersion()
    }

    fun getDNS() {
        lostAtCommand = AtCommand.GET_DNS
        mFscBleCentralApi.send("AT+DNS\r\n".toByteArray())
    }


    fun reset() {
        lostAtCommand = AtCommand.RESET
        mFscBleCentralApi.reset()
    }

    fun setIp(ip: String) {
        lostAtCommand = AtCommand.SET_IP
        mFscBleCentralApi.setIp(ip)
    }

    fun setGw(ip: String) {
        lostAtCommand = AtCommand.SET_GW
        mFscBleCentralApi.setGw(ip)
    }

    fun setMask(ip: String) {
        lostAtCommand = AtCommand.SET_MASK
        mFscBleCentralApi.setMask(ip)
    }

    fun setDns(ip: String) {
        lostAtCommand = AtCommand.SET_DNS
        mFscBleCentralApi.setDns(ip)
    }

    fun setNetwork(ssid: String, password: String) {
        lostAtCommand = AtCommand.SET_NETWORK
        mFscBleCentralApi.setNetwork(ssid, password, this)
    }

    fun startOTA(version: String) {
        lostAtCommand = AtCommand.OTA
        mFscBleCentralApi.startOTA(version, this)
    }

    fun send(byteArray: ByteArray) {
        mFscBleCentralApi.send(byteArray)
    }

    fun isConnect() = mFscBleCentralApi.isConnect

    override fun blePeripheralFound(// 把从FscBleCentralApiImp.jva里面扫描到的设备回调到BluetoothRepository.kt里面
        device: BluetoothDeviceBwWrapper?, rssi: Int, record: ByteArray?
    ) {
        if (rssiValue > rssi) return
        if (nameSwitch) {
            device?.name?.let {
                if (!it.contains(nameValue)) {
                    return
                }
            } ?: let {
                return
            }
        }
        device?.let {
            try {
                mCallbacks.forEach {
                    it.blePeripheralFound(device, record)// 把rssi信号值和名称过滤处理后，再回调给DeviceFragment.kt
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
        LogUtils.d(TAG, "FeasyWiFi APP blePeripheralConnected: 连接成功")
        mHandler.removeCallbacks(mConnectTimeoutRunnable)
        try {
            mCallbacks.forEach {
                it.blePeripheralConnected()// 把连接成功的消息回调给DeviceFragment.kt
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun blePeripheralDisconnected(
        gatt: BluetoothGatt?, device: BluetoothDevice?, isPinError: Boolean
    ) {
        LogUtils.e(TAG,"FeasyWiFi APP blePeripheralDisconnected isPinError => $isPinError")
        lostAtCommand = AtCommand.NONE
        try {
            mCallbacks.forEach {
                it.blePeripheralDisconnected()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun packetReceived(
        device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?
    ) {
        LogUtils.d(TAG, "FeasyWiFi APP packetReceived strValue => $strValue  hexString => $hexString")
        try {
            mCallbacks.forEach {
                it.packetReceived(strValue)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun packetSend(
        device: BluetoothDevice?, strValue: String?, hexString: String?, rawValue: ByteArray?
    ) {
        LogUtils.d(TAG, "FeasyWiFi APP packetSend strValue => $strValue  hexString => $hexString")
        try {
            mCallbacks.forEach {
                it.packetSend(strValue)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun ipInformation(ip: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP ipInformation: ip  ->  $ip")
        try {
            mCallbacks.forEach {
                it.ipInformation(ip)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun NetInformation(apName: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP NetInformation: apName  ->  $apName")
        try {
            mCallbacks.forEach {
                it.NetInformation(apName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun versionInformation(version: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP versionInformation: version ->  $version")
        try {
            mCallbacks.forEach {
                it.versionInformation(version)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun scanstatecallback(isscan: Boolean) {
        isScanner = isscan
    }

    override fun success(ip: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP success: 配网成功")

        ip?.let {
            try {
                mCallbacks.forEach {
                    it.success(ip)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun failure() {
        LogUtils.e(TAG, "FeasyWiFi APP failure: 配网失败")
        try {
            mCallbacks.forEach {
                it.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOtaSuccess() {
        try {
            mCallbacks.forEach {
                it.onOtaSuccess()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOtaFailure() {
        try {
            mCallbacks.forEach {
                it.onOtaFailure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOtaProgress(progress: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP onOtaProgress progress => $progress")
        try {
            mCallbacks.forEach {
                it.onOtaProgress(progress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOtaNetworkIsNotConfigured() {
        try {
            mCallbacks.forEach {
                it.onOtaNetworkIsNotConfigured()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOtaStartConfig() {
        try {
            mCallbacks.forEach {
                it.onOtaStartConfig()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}