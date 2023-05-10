package com.ooober.driver.providers

import com.ooober.driver.api.IFCMApi
import com.ooober.driver.api.RetrofitClient
import com.ooober.driver.models.FCMBody
import com.ooober.driver.models.FCMResponse
import retrofit2.Call
import retrofit2.create

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body:FCMBody):Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }
}