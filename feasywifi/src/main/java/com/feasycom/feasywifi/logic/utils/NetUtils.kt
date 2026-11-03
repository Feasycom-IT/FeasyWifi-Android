package com.feasycom.feasywifi.logic.utils

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.lang.reflect.InvocationTargetException
import java.net.*

object NetUtils {

    fun isWifiConnected(wifiManager: WifiManager): Boolean {
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo != null && wifiInfo.networkId != -1 && "<unknown ssid>" != wifiInfo.ssid
    }

    fun getRawSsidBytes(info: WifiInfo): ByteArray? {
        try {
            var method = info.javaClass.getMethod("getWifiSsid")
            method.isAccessible = true
            val wifiSsid = method.invoke(info) ?: return null
            method = wifiSsid.javaClass.getMethod("getOctets")
            method.isAccessible = true
            return method.invoke(wifiSsid) as ByteArray
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return null
    }

    fun getRawSsidBytesOrElse(info: WifiInfo, orElse: ByteArray?): ByteArray? {
        val raw = getRawSsidBytes(info)
        return raw ?: orElse
    }

    fun getSsidString(info: WifiInfo): String? {
        var ssid = info.ssid
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length - 1)
        }
        return ssid
    }

    fun getBroadcastAddress(wifi: WifiManager): InetAddress? {
        val dhcp = wifi.dhcpInfo
        if (dhcp != null) {
            val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
            val quads = ByteArray(4)
            for (k in 0..3) {
                quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
            }
            try {
                return InetAddress.getByAddress(quads)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
        try {
            return InetAddress.getByName("255.255.255.255")
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
        // Impossible arrive here
        return null
    }

    fun is5G(frequency: Int): Boolean {
        return frequency in 4901..5899
    }

    fun getAddress(ipAddress: Int): InetAddress? {
        val ip = byteArrayOf(
            (ipAddress and 0xff).toByte(),
            (ipAddress shr 8 and 0xff).toByte(),
            (ipAddress shr 16 and 0xff).toByte(),
            (ipAddress shr 24 and 0xff).toByte()
        )
        return try {
            InetAddress.getByAddress(ip)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            // Impossible arrive here
            null
        }
    }

    private fun getAddress(isIPv4: Boolean): InetAddress? {
        try {
            val enums = NetworkInterface.getNetworkInterfaces()
            while (enums.hasMoreElements()) {
                val ni = enums.nextElement()
                val addrs = ni.inetAddresses
                while (addrs.hasMoreElements()) {
                    val address = addrs.nextElement()
                    if (!address.isLoopbackAddress) {
                        if (isIPv4 && address is Inet4Address) {
                            return address
                        }
                        if (!isIPv4 && address is Inet6Address) {
                            return address
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return null
    }

    fun getIPv4Address(): InetAddress? {
        return getAddress(true)
    }

    fun getIPv6Address(): InetAddress? {
        return getAddress(false)
    }

    /**
     * @param bssid the bssid like aa:bb:cc:dd:ee:ff
     * @return byte array converted from bssid
     */
    fun convertBssid2Bytes(bssid: String): ByteArray? {
        val bssidSplits = bssid.split(":".toRegex()).toTypedArray()
        require(bssidSplits.size == 6) { "Invalid bssid format" }
        val result = ByteArray(bssidSplits.size)
        for (i in bssidSplits.indices) {
            result[i] = bssidSplits[i].toInt(16).toByte()
        }
        return result
    }
}