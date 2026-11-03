package com.feasycom.feasywifi.logic.network

import com.feasycom.feasywifi.logic.model.FeedbackParams
import com.feasycom.feasywifi.logic.model.ProtocolParams

object FeasyWiFiNetwork {

    private val splashService = ServiceCreator.create<SplashService>()

    suspend fun getSplash(parameter: Map<String, String>) = splashService.getLanch(parameter)

    private val feedbackService = ServiceCreator.create<FeedbackService>()

    suspend fun feedback(key: String, content: FeedbackParams) =
        feedbackService.feedback(key, content)

    private val agreementService = ServiceCreator.create<AgreementService>()

    suspend fun getProtocol(protocolParams: ProtocolParams, local: String = "en") =
        agreementService.getProtocol(protocolParams, local)

}