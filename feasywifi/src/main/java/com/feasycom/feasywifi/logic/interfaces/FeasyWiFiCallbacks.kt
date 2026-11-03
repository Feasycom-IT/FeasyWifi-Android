package com.feasycom.feasywifi.logic.interfaces

import com.feasycom.bean.BluetoothDeviceBwWrapper

interface FeasyWiFiCallbacks {

    fun blePeripheralFound(
        device: BluetoothDeviceBwWrapper, record: ByteArray?
    ) {
    }

    fun blePeripheralConnected() {
    }

    fun onConnectTimeout() {

    }

    fun blePeripheralDisconnected() {
    }

    fun packetReceived(strValue: String?) {
    }

    fun packetSend(strValue: String?) {
    }

    fun ipInformation(ip: String?) {
    }

    fun NetInformation(apName: String?) {

    }

    fun versionInformation(version: String?) {
    }

    fun success(ip: String) {
    }

    fun failure() {
    }


    fun onOtaSuccess() {

    }

    fun onOtaFailure() {

    }

    fun onOtaProgress(progress: String?) {
    }

    fun onOtaNetworkIsNotConfigured() {
    }

    fun onOtaStartConfig() {
    }

}