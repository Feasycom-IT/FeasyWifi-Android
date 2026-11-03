package com.feasycom.feasywifi.logic

import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.App
import com.feasycom.feasywifi.logic.interfaces.MQTTCallbacks
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

object MQTTRepository : MqttCallback, IMqttActionListener {

    private const val TAG = "MQTTRepository"
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var mMqttConnectOptions: MqttConnectOptions? = null


    private val mCallbacks = mutableListOf<MQTTCallbacks>()

    private var mIMqttToken: IMqttToken? = null


    fun registerViewCallback(callback: MQTTCallbacks) {
        if (mCallbacks.contains(callback)) return
        mCallbacks.add(callback)
    }

    fun unRegisterViewCallback(callback: MQTTCallbacks) {
        mCallbacks.remove(callback)
    }


    fun connect(
        broker: String = "192.168.0.140",
        port: String = "61613",
        clientId: String = "55555555555",
        username: String = "admin",
        password: String = "password"
    ) {
        val serverURI = "tcp://$broker:$port"
        mqttAndroidClient = MqttAndroidClient(App.sContext, serverURI, clientId)
        mqttAndroidClient!!.setCallback(this) //设置监听订阅消息的回调
        mMqttConnectOptions = MqttConnectOptions()
        mMqttConnectOptions!!.isCleanSession = true //设置是否清除缓存
        mMqttConnectOptions!!.connectionTimeout = 10 //设置超时时间，单位：秒
        mMqttConnectOptions!!.keepAliveInterval = 20 //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions!!.userName = username //设置用户名
        mMqttConnectOptions!!.password = password.toCharArray() //设置密码

        mIMqttToken = mqttAndroidClient!!.connect(mMqttConnectOptions, null, this)
    }

    fun publish(topic: String = "tourist_enter", message: String) {
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient!!.publish(
                topic, message.toByteArray(), qos, retained
            )
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String = "tourist_enter") {
        try {
            mIMqttToken?.topics?.forEach {
                mqttAndroidClient?.unsubscribe(it)
            }
            mqttAndroidClient!!.subscribe(topic, 2) //订阅主题，参数：主题、服务质量
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        mqttAndroidClient?.disconnect()
    }

    override fun connectionLost(cause: Throwable?) {
        LogUtils.e(TAG, "FeasyWiFi APP connectionLost: 断开连接")
        mCallbacks.forEach {
            it.onDisconnect()
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        LogUtils.d(TAG, "FeasyWiFi APP 收到消息： " + String(message!!.payload))
        mCallbacks.forEach {
            it.onMessageArrived(topic, message)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        LogUtils.d(TAG, "FeasyWiFi APP onSuccess: 连接成功")
        mCallbacks.forEach {
            it.onConnect()
        }
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        LogUtils.e(TAG, "FeasyWiFi APP onFailure: 连接失败")
        mqttAndroidClient?.unregisterResources()
        mCallbacks.forEach {
            it.onFailure(asyncActionToken, exception)
        }
    }
}