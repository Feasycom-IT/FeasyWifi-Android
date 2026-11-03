package com.feasycom.feasywifi

import com.blankj.utilcode.util.LogUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun a(){
        val a = "FSC-BW236-LE"
        println(a.length)
        println(a.substring(0,10))
    }

    lateinit var bo: String
    @Test
    fun b(){
        LogUtils.d("TAG", "FeasyWiFi APP b: ${::bo.isLateinit}", )
    }
}
/*
托皮克*/
