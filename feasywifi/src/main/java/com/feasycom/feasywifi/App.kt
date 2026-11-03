package com.feasycom.feasywifi

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.tencent.bugly.crashreport.CrashReport

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        sContext = applicationContext
        CrashReport.initCrashReport(applicationContext, "3d80a716ea", false);
        BluetoothRepository.isShowLog(true)

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var sContext: Context? = null
    }
}