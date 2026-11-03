package com.feasycom.feasywifi.logic.utils

import android.content.Context
import android.content.pm.PackageManager

fun Context.packageCode(): String? {
    val manager = packageManager
    var code: String? = null
    try {
        val info = manager.getPackageInfo(packageName, 0)
        code = info.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return code
}