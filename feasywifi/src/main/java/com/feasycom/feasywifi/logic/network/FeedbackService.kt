package com.feasycom.feasywifi.logic.network

import com.feasycom.feasywifi.logic.model.FeedbackParams
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface FeedbackService {

    @POST("https://qyapi.weixin.qq.com/cgi-bin/webhook/send")
    suspend fun feedback(@Query("key") key: String, @Body content: FeedbackParams): ResponseBody
}