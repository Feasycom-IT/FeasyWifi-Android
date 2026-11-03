package com.feasycom.feasywifi.logic.network

import com.feasycom.feasywifi.logic.model.ProtocolParams
import com.feasycom.feasywifi.logic.model.ProtocolResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AgreementService {

    @POST("agreement")
    suspend fun getProtocol(
        @Body protocolParams: ProtocolParams, @Header("local") local: String = "en"
    ): ProtocolResponse
}