package com.feasycom.feasywifi.logic.interfaces

import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

interface MQTTCallbacks {

    fun onMessageArrived(topic: String?, message: MqttMessage?){

    }

    fun onConnect(){

    }

    fun onDisconnect(){

    }

    fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?){

    }
}