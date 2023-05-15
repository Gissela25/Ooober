package com.ooober.user.api


import com.ooober.user.models.FCMBody
import com.ooober.user.models.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAyjW0oCI:APA91bH_UCpDdLMKkM0IZDnmnj-rzWtX2Q8hyAAtvK8KIHboV9bJn-unSlNkdQObK_4wB1tSYiqTQM6pn4RzJaVcoyfdd21byakQ0LhrUWfKuFNFfxXPyX4y0uIqcXXeZw4EEFPPHkC0"
    )
    @POST("fcm/send")
     fun send(@Body  body: FCMBody): Call<FCMResponse>
}