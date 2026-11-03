package com.feasycom.feasywifi.logic

import android.os.Build
import androidx.lifecycle.liveData
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.App
import com.feasycom.feasywifi.logic.model.FeedbackParams
import com.feasycom.feasywifi.logic.model.ProtocolParams
import com.feasycom.feasywifi.logic.model.ProtocolResponse
import com.feasycom.feasywifi.logic.network.FeasyWiFiNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object NetworkRepository {

    var FEASYWIFI_KEY = "24de55ae-9201-460d-a19d-65ace7d4d385"
    const val FEEDBACK_ADVICE = 0
    const val FEEDBACK_BUG = 1
    const val FEEDBACK_UI = 2
    const val FEEDBACK_COOPERATION = 3

    fun downloadSplash(app: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val map = mapOf("app" to app)
            val splashInfo = FeasyWiFiNetwork.getSplash(map)
            if (splashInfo.code == 200) {
                Result.success(splashInfo.data.image)
            } else {
                Result.failure(RuntimeException("请求首页图时发生错误 ${splashInfo.code}"))
            }
        }
    }

    fun feedback(content: String, feedbackType: Int) = fire(Dispatchers.IO) {
        coroutineScope {
            val fbType = when (feedbackType) {
                FEEDBACK_ADVICE -> "建议"
                FEEDBACK_BUG -> "功能异常"
                FEEDBACK_UI -> "界面异常"
                FEEDBACK_COOPERATION -> "合作"
                else -> "建议"
            }

            val packageManager = App.sContext!!.packageManager
            val packageInfo = packageManager.getPackageInfo(
                App.sContext!!.packageName, 0
            )
            // applicationInfo 在 API 33+ 是 nullable，取不到 labelRes 时退化为空字符串
            val labelRes = packageInfo.applicationInfo?.labelRes ?: 0
            // app名称
            val appName = if (labelRes != 0) App.sContext!!.resources.getString(labelRes) else ""
            // app版本
            val appVersion = packageInfo.versionName
            // 系统版本
            val systemVersion = Build.VERSION.RELEASE
            // 手机型号
            val phoneType = Build.BRAND + "(" + Build.MODEL + ")"

            val s =
                "<font color=\"warning\">Android </font>《$appName》用户反馈\n>类型：<font color=\"comment\">$fbType</font>\n>APP版本：<font color=\"comment\">$appVersion</font>\n>系统版本：<font color=\"comment\">$systemVersion</font>\n>手机型号：<font color=\"comment\">$phoneType</font>\n>反馈内容：<font color=\"comment\">$content</font>"
            val feedback =
                FeasyWiFiNetwork.feedback(FEASYWIFI_KEY, FeedbackParams(FeedbackParams.Markdown(s)))
            Result.success(feedback)
        }

    }

    fun getAgreementUrl(type: Int) = fire(Dispatchers.IO) {
        coroutineScope {
            // 判断语言
            val isChinese = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                App.sContext!!.resources.configuration.locales[0].language.endsWith("zh")
            } else {
                App.sContext!!.resources.configuration.locale.language.endsWith("zh")
            }
            val language = if (isChinese) "cn" else "en"

            // 执行网络请求
            val protocolParams = ProtocolParams("wifi", type)
            val response = FeasyWiFiNetwork.getProtocol(protocolParams, language)
            Result.success(response.data.url)
        }
    }

    const val TAG: String = "NetworkRepository"

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                LogUtils.e(TAG, "FeasyWiFi APP fire: $e")
                Result.failure<T>(e)
            }
            emit(result)
        }

}