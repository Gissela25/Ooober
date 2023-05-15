package com.ooober.user.providers

import com.ooober.user.api.IFCMApi
import com.ooober.user.api.RetrofitClient
import com.ooober.user.models.FCMBody
import com.ooober.user.models.FCMResponse
import retrofit2.Call
import retrofit2.create

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body:FCMBody):Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }
}