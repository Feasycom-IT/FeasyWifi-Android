package com.feasycom.feasywifi.ui.bluetooth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.feasycom.feasywifi.App
import com.feasycom.feasywifi.logic.utils.NetUtils

class BluetoothViewModel : ViewModel() {

    private val _broadcastData = MutableLiveData<String>()

    private val mWifiManager by lazy {
        App.sContext?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    val mBroadcastData = Transformations.switchMap(_broadcastData) {
        MutableLiveData<String>().apply {
            value = if (NetUtils.isWifiConnected(mWifiManager)) {
                NetUtils.getSsidString(mWifiManager.connectionInfo)
            } else {
                ""
            }
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context, intent: Intent
        ) {
            val action = intent.action ?: return
            when (action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION, LocationManager.PROVIDERS_CHANGED_ACTION -> _broadcastData.setValue(
                    action
                )
            }
        }
    }

    private val filter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION).apply {
            this.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
    } else {
        IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
    }

    fun unregisterReceiver() {
        App.sContext?.applicationContext?.unregisterReceiver(mReceiver)
    }


    fun registerReceiver() {
        App.sContext?.applicationContext?.registerReceiver(mReceiver, filter)
    }

}