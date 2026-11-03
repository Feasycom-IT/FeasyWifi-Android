package com.feasycom.feasywifi.logic.network

import android.annotation.SuppressLint
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object ServiceCreator {
    private const val BASE_URL = "https://api.feasycom.com/"
    const val TAG: String = "ServiceCreator"

    /*
   *打印retrofit信息部分
   */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        LogUtils.d(TAG, "FeasyWiFi APP http: $message")
    }

    private val httpClient by lazy {
        OkHttpClient().newBuilder().apply {
            this.addNetworkInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            this.connectTimeout(30, TimeUnit.SECONDS)
            this.readTimeout(30, TimeUnit.SECONDS)
            // 添加下面的代码，跳过https验证机制
            val trustAllCerts: Array<TrustManager> = arrayOf(ZTrustManager())
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            hostnameVerifier { _: String?, _: SSLSession? -> true }
        }.build()
    }

    //这是之前的代码
//    private val httpClients = OkHttpClient.Builder()
//        .addNetworkInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .build()


    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(httpClient)
        .addConverterFactory(GsonConverterFactory.create()).build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

    @SuppressLint("CustomX509TrustManager")
    private class ZTrustManager : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

    }

}