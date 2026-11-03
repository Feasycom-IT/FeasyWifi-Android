package com.feasycom.feasywifi.logic.network

import com.feasycom.feasywifi.logic.model.SplashInfo
import okhttp3.ResponseBody
import retrofit2.http.*

interface SplashService {

    @POST("lanch")
    suspend fun getLanch(@Body parameter: Map<String, String>): SplashInfo

    @Streaming //大文件时要加不然会OOM
    @GET("https://image.feasycom.com/lanchImage/{parameter}/{fileName}")
    suspend fun downImg(
        @Path("parameter") path: String = "wifi", @Path("fileName") fileName: String = "lanch.png"
    ): ResponseBody


}