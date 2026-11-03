package com.feasycom.feasywifi.logic.model

import androidx.annotation.Keep

@Keep
data class ProtocolParams(val app: String, val type: Int)

@Keep
data class ProtocolResponse(
    val code: Int, val `data`: Data, val msg: String
)

@Keep
data class Data(
    val url: String
)