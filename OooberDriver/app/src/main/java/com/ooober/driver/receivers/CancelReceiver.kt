package com.ooober.driver.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ooober.driver.activities.MapActivity
import com.ooober.driver.activities.MapTripActivity
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.BookingProvider

class CancelReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()
    val authProvider = AuthProvider()

    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
        cancelBooking( idBooking!!)
    }

    private fun cancelBooking( idBooking: String) {
        bookingProvider.updateStatus(idBooking, "cancel").addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("RECEIVER","EL VIAJE HA SIDO CANCELADO")
            } else {
             Log.d("RECEIVER","NO SE PUDO ACTUALIZAR EL ESTADO DEL VIAJE")
            }
        }
    }

}